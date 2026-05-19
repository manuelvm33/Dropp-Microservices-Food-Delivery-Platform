package com.dropp.order.controller;
import com.dropp.order.config.GlobalExceptionHandler;
import com.dropp.order.dto.FoodItemDto;
import com.dropp.order.dto.OrderDto;
import com.dropp.order.dto.OrderFrontDto;
import com.dropp.order.dto.RestaurantDto;
import com.dropp.order.dto.UserDto;
import com.dropp.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OrderController.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class OrderControllerTest {

    private static final Long ORDER_ID = 100L;
    private static final Integer USER_ID = 1;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
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
    private OrderDto buildOrderDto() {
        UserDto userDto = buildUserDto();
        return new OrderDto(ORDER_ID, buildFoodItemList(), buildRestaurant(), userDto);
    }

    @Test
    void saveOrder_shouldReturn201WithOrderDTO() throws Exception {
        // Arrange
        OrderFrontDto frontDto = buildOrderFrontDto();
        OrderDto responseDto = buildOrderDto();
        when(orderService.saveOrder(any(OrderFrontDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/order/saveOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frontDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.userDto.userName").value("Manuel Pineda"))
                .andExpect(jsonPath("$.foodItemsList").isArray())
                .andExpect(jsonPath("$.foodItemsList.length()").value(2));
    }

    @Test
    void saveOrder_whenUserServiceIsDown_shouldReturn503() throws Exception {
        // Arrange
        OrderFrontDto frontDto = buildOrderFrontDto();
        when(orderService.saveOrder(any(OrderFrontDto.class)))
                .thenThrow(new ResourceAccessException("Connection refused: USER-SERVICE"));

        // Act & Assert
        mockMvc.perform(post("/order/saveOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frontDto)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value(
                        "User service is currently unavailable. Please try again later."));
    }
    @Test
    void saveOrder_whenUnexpectedErrorOccurs_shouldReturn500() throws Exception {
        // Arrange
        OrderFrontDto frontDto = buildOrderFrontDto();
        when(orderService.saveOrder(any(OrderFrontDto.class)))
                .thenThrow(new RuntimeException("Unexpected database failure"));

        // Act & Assert
        mockMvc.perform(post("/order/saveOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(frontDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void saveOrder_whenRequestBodyIsMissing_shouldReturn400() throws Exception {
        // Act & Assert — sin body, Spring lanza HttpMessageNotReadableException
        mockMvc.perform(post("/order/saveOrder")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}