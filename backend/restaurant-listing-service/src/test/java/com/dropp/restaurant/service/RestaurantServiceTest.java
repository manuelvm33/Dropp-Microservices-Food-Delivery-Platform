package com.dropp.restaurant.service;

import com.dropp.restaurant.dto.RestaurantDTO;
import com.dropp.restaurant.entity.Restaurant;
import com.dropp.restaurant.exception.RestaurantNotFoundException;
import com.dropp.restaurant.mapper.RestaurantMapper;
import com.dropp.restaurant.repo.RestaurantRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    private static final Long RESTAURANT_ID = 1L;

    @Mock
    RestaurantRepo restaurantRepo;

    @InjectMocks
    RestaurantService restaurantService;

    private List<Restaurant> buildListOfRestaurant() {
        return Arrays.asList(
                new Restaurant(1L, "Restaurant 1", "Address 1", "City 1", "Description Restaurant 1", "/images/restaurant1.png", 4.5),
                new Restaurant(2L, "Restaurant 2", "Address 2", "City 2", "Description Restaurant 2", "/images/restaurant2.png", 4.0)
        );
    }

    private RestaurantDTO buildRestaurantDTO() {
        return new RestaurantDTO(RESTAURANT_ID, "Restaurant 1", "Address 1", "City 1",
                "Description Restaurant 1", "/images/restaurant1.png", 4.5);
    }

    private Restaurant buildRestaurant() {
        return new Restaurant(RESTAURANT_ID, "Restaurant 1", "Address 1", "City 1",
                "Description Restaurant 1", "/images/restaurant1.png", 4.5);
    }

    @Test
    void fetchRestaurants_shouldReturnMappedDtoPage() {
        // Arrange
        List<Restaurant> mockRestaurants = buildListOfRestaurant();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Restaurant> mockPage = new PageImpl<>(mockRestaurants);
        when(restaurantRepo.findAll(pageRequest)).thenReturn(mockPage);
        // Act
        Page<RestaurantDTO> result = restaurantService.fetchRestaurants(0, 10);
        // Assert: total elements
        assertEquals(mockPage.getTotalElements(), result.getTotalElements());
        // Assert: every dto mapped
        List<RestaurantDTO> expectedDtos = mockRestaurants.stream().
                map(RestaurantMapper.INSTANCE::mapRestaurantToRestaurantDTO).toList();
        assertEquals(expectedDtos, result.getContent());
        verify(restaurantRepo, times(1)).findAll(pageRequest);
    }

    @Test
    void fetchRestaurantById_whenExists_shouldReturnDTO() {
        // Arrange
        Restaurant entity = buildRestaurant();
        when(restaurantRepo.findById(RESTAURANT_ID)).thenReturn(Optional.of(entity));
        // Act
        RestaurantDTO result = restaurantService.fetchRestaurantById(RESTAURANT_ID);
        // Assert
        assertNotNull(result);
        assertEquals(RESTAURANT_ID, result.getId());
        verify(restaurantRepo, times(1)).findById(RESTAURANT_ID);
    }

    @Test
    void fetchRestaurantById_whenNotExists_shouldThrowRestaurantNotFoundException() {
        when(restaurantRepo.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        RestaurantNotFoundException ex = assertThrows(RestaurantNotFoundException.class,
                () -> restaurantService.fetchRestaurantById(RESTAURANT_ID));
        assertEquals("Restaurant not found with id: " + RESTAURANT_ID, ex.getMessage());
    }

    @Test
    void addRestaurant_shouldPersistAndReturnDTO() {
        // Arrange
        RestaurantDTO inputDTO = buildRestaurantDTO();
        Restaurant entity = RestaurantMapper.INSTANCE.mapRestaurantDTOToRestaurant(inputDTO);
        when(restaurantRepo.save(any(Restaurant.class))).thenReturn(entity);
        // Act
        RestaurantDTO result = restaurantService.addRestaurant(inputDTO);
        // Assert
        assertEquals(inputDTO.getName(), result.getName());
        verify(restaurantRepo, times(1)).save(any(Restaurant.class));
    }

}
