package com.dropp.order.service;

import com.dropp.order.dto.FoodItemDto;
import com.dropp.order.dto.OrderDto;
import com.dropp.order.dto.OrderFrontDto;
import com.dropp.order.dto.RestaurantDto;
import com.dropp.order.dto.UserDto;
import com.dropp.order.entity.Order;
import com.dropp.order.repo.OrderRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    private static final String USER_SERVICE_URL = "http://USER-SERVICE/user/";
    private static final Integer USER_ID = 1;
    private static final Long ORDER_ID = 100L;

    @Mock
    OrderRepo orderRepo;

    @Mock
    SequenceGenerator sequenceGenerator;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService;

    private UserDto buildUserDto() {
        return new UserDto(1L, "Manuel Pineda", "manuelpineda@gmail.com", "3101234567",
                "Address 1", "Compton");
    }

    private RestaurantDto buildRestaurant() {
        return new RestaurantDto(1L, "Restaurant 1", "Address 1", "City 1",
                "Description Restaurant 1", "/images/restaurant1.png", 4.5);
    }

    private List<FoodItemDto> buildFoodItemList() {
        return Arrays.asList(
                new FoodItemDto(1L, "Pizza Margherita", "Classic tomato and mozzarella", 25_900.0, false,  1L,5),
                new FoodItemDto(2L, "Pasta Carbonara", "Creamy Roman pasta", 22_900.0, false, 2L,4)
        );
    }

    private OrderFrontDto buildOrderFrontDto() {
        return new OrderFrontDto(buildFoodItemList(), USER_ID, buildRestaurant());
    }

    @Test
    void saveOrder_shouldFetchUserGenerateIdPersistAndReturnDTO() {
        // Arrange
        OrderFrontDto frontDto = buildOrderFrontDto();
        UserDto userDto = buildUserDto();

        when(sequenceGenerator.generateNextOrderId()).thenReturn(ORDER_ID);
        when(restTemplate.getForObject(USER_SERVICE_URL + USER_ID, UserDto.class)).thenReturn(userDto);
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderDto result = orderService.saveOrder(frontDto);

        // Assert
        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(userDto.getUserName(), result.getUserDto().getUserName());
        assertEquals(frontDto.getFoodItemsList().size(), result.getFoodItemsList().size());

        verify(sequenceGenerator, times(1)).generateNextOrderId();
        verify(restTemplate, times(1)).getForObject(USER_SERVICE_URL + USER_ID, UserDto.class);
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    void saveOrder_whenUserServiceIsDown_shouldPropagateResourceAccessException() {
        // Arrange
        OrderFrontDto frontDto = buildOrderFrontDto();
        when(sequenceGenerator.generateNextOrderId()).thenReturn(ORDER_ID);
        when(restTemplate.getForObject(USER_SERVICE_URL + USER_ID, UserDto.class))
                .thenThrow(new ResourceAccessException("Connection refused: USER-SERVICE"));

        // Act & Assert
        ResourceAccessException ex = assertThrows(ResourceAccessException.class,
                () -> orderService.saveOrder(frontDto));

        assertTrue(ex.getMessage().contains("USER-SERVICE"));

        // if  RestTemplate fails, the repo never should be called
        verify(orderRepo, never()).save(any(Order.class));
    }
}