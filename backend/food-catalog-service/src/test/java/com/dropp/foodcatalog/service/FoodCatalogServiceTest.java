package com.dropp.foodcatalog.service;

import com.dropp.foodcatalog.dto.FoodCatalogPage;
import com.dropp.foodcatalog.dto.FoodItemDTO;
import com.dropp.foodcatalog.dto.Restaurant;
import com.dropp.foodcatalog.entity.FoodItem;
import com.dropp.foodcatalog.repo.FoodItemRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FoodCatalogServiceTest {

    private static final Integer RESTAURANT_ID = 1;
    private static final String RESTAURANT_SERVICE_URL = "https://RESTAURANT-SERVICE/restaurant/fetchRestaurantById/";

    @Mock
    FoodItemRepo foodItemRepo;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    FoodCatalogService foodCatalogService;

    private FoodItemDTO buildFoodItemDTO() {
        return new FoodItemDTO(1L, "Pizza Margherita", "Classic tomato and mozzarella", 25_900.0, false,  1L,5);
    }

    private FoodItem buildFoodItem() {
        return new FoodItem(1L, "Pizza Margherita", "Classic tomato and mozzarella", 25_900.0, false,  1L,5);
    }

    private List<FoodItem> buildFoodItemList() {
        return Arrays.asList(
                new FoodItem(1L, "Pizza Margherita", "Classic tomato and mozzarella", 25_900.0, false,  1L,5),
                new FoodItem(2L, "Pasta Carbonara", "Creamy Roman pasta", 22_900.0, false, 2L,4)
        );
    }

    private Restaurant buildRestaurant() {
        return new Restaurant(1L, "Restaurant 1", "Address 1", "City 1",
                "Description Restaurant 1", "/images/restaurant1.png", 4.5);
    }

    @Test
    void addFoodItem_shouldPersistAndReturnDTO() {
        // Arrange
        FoodItemDTO inputDTO = buildFoodItemDTO();
        FoodItem savedEntity = buildFoodItem();
        when(foodItemRepo.save(any(FoodItem.class))).thenReturn(savedEntity);

        // Act
        FoodItemDTO result = foodCatalogService.addFoodItem(inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals(inputDTO.getName(), result.getName());
        assertEquals(inputDTO.getRestaurantId(), result.getRestaurantId());
        verify(foodItemRepo, times(1)).save(any(FoodItem.class));
    }

    @Test
    void fetchFoodCataloguePageDetails_whenRestaurantExists_shouldReturnPageWithItemsAndRestaurant() {
        // Arrange
        List<FoodItem> foodItems = buildFoodItemList();
        Restaurant restaurant = buildRestaurant();

        when(foodItemRepo.findByRestaurantId(RESTAURANT_ID)).thenReturn(foodItems);
        when(restTemplate.getForObject(RESTAURANT_SERVICE_URL + RESTAURANT_ID, Restaurant.class))
                .thenReturn(restaurant);

        // Act
        FoodCatalogPage result = foodCatalogService.fetchFoodCataloguePageDetails(RESTAURANT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(restaurant, result.getRestaurant());
        assertEquals(foodItems.size(), result.getItems().size());
        assertEquals(foodItems, result.getItems());

        verify(foodItemRepo, times(1)).findByRestaurantId(RESTAURANT_ID);
        verify(restTemplate, times(1))
                .getForObject(RESTAURANT_SERVICE_URL + RESTAURANT_ID, Restaurant.class);
    }

    @Test
    void fetchFoodCataloguePageDetails_whenRestaurantServiceIsDown_shouldPropagateResourceAccessException() {
        // Arrange
        when(foodItemRepo.findByRestaurantId(RESTAURANT_ID)).thenReturn(buildFoodItemList());
        when(restTemplate.getForObject(RESTAURANT_SERVICE_URL + RESTAURANT_ID, Restaurant.class))
                .thenThrow(new ResourceAccessException("Connection refused: RESTAURANT-SERVICE"));

        // Act & Assert
        ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                () -> foodCatalogService.fetchFoodCataloguePageDetails(RESTAURANT_ID));

        assertTrue(ex.getMessage().contains("RESTAURANT-SERVICE"));
        verify(restTemplate, times(1))
                .getForObject(RESTAURANT_SERVICE_URL + RESTAURANT_ID, Restaurant.class);
    }

    @Test
    void fetchFoodCataloguePageDetails_whenNoFoodItems_shouldReturnPageWithEmptyList() {
        // Arrange
        Restaurant restaurant = buildRestaurant();

        when(foodItemRepo.findByRestaurantId(RESTAURANT_ID)).thenReturn(Collections.emptyList());
        when(restTemplate.getForObject(RESTAURANT_SERVICE_URL + RESTAURANT_ID, Restaurant.class))
                .thenReturn(restaurant);

        // Act
        FoodCatalogPage result = foodCatalogService.fetchFoodCataloguePageDetails(RESTAURANT_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(restaurant, result.getRestaurant());
    }
}