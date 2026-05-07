package com.dropp.foodcatalog.controller;

import com.dropp.foodcatalog.config.GlobalExceptionHandler;
import com.dropp.foodcatalog.dto.FoodCatalogPage;
import com.dropp.foodcatalog.dto.FoodItemDTO;
import com.dropp.foodcatalog.dto.Restaurant;
import com.dropp.foodcatalog.entity.FoodItem;
import com.dropp.foodcatalog.service.FoodCatalogService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FoodCatalogController.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
public class FoodCatalogControllerTest {

    private static final Integer RESTAURANT_ID = 1;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FoodCatalogService foodCatalogService;


    private FoodItemDTO buildFoodItemDTO() {
        return new FoodItemDTO(1L, "Pizza Margherita", "Classic tomato and mozzarella", 25_900.0, false,  1L,5);
    }
    private FoodCatalogPage buildFoodCatalogPage() {
        List<FoodItem> foodItems = Arrays.asList(
                new FoodItem(1L, "Pizza Margherita", "Classic tomato and mozzarella", 25_900.0, false,  1L,5),
                new FoodItem(2L, "Pasta Carbonara", "Creamy Roman pasta", 22_900.0, false, 2L,4)
        );
        Restaurant restaurant = new Restaurant(1L, "Restaurant 1", "Address 1", "City 1",
                "Description Restaurant 1", "/images/restaurant1.png", 4.5);
        return new FoodCatalogPage(foodItems, restaurant);
    }

    @Test
    void addFoodItem_shouldReturn201WithSavedDTO() throws Exception {
        // Arrange
        FoodItemDTO inputDTO = buildFoodItemDTO();
        when(foodCatalogService.addFoodItem(any(FoodItemDTO.class))).thenReturn(inputDTO);

        // Act & Assert
        mockMvc.perform(post("/foodCatalog/addFoodItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pizza Margherita"))
                .andExpect(jsonPath("$.restaurantId").value(RESTAURANT_ID));
    }


    @Test
    void fetchRestaurantWithFoodMenu_whenRestaurantExists_shouldReturn200() throws Exception {
        // Arrange
        FoodCatalogPage page = buildFoodCatalogPage();
        when(foodCatalogService.fetchFoodCataloguePageDetails(RESTAURANT_ID)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/foodCatalog/fetchRestaurantAndFoodItemsById/{restaurantId}", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurant.id").value(1))
                .andExpect(jsonPath("$.restaurant.name").value("Restaurant 1"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Pizza Margherita"));
    }

    @Test
    void fetchRestaurantWithFoodMenu_whenRestaurantServiceIsDown_shouldReturn503() throws Exception {
        // Arrange
        when(foodCatalogService.fetchFoodCataloguePageDetails(RESTAURANT_ID))
                .thenThrow(new ResourceAccessException("Connection refused: RESTAURANT-SERVICE"));

        // Act & Assert
        mockMvc.perform(get("/foodCatalog/fetchRestaurantAndFoodItemsById/{restaurantId}", RESTAURANT_ID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value(
                        "Restaurant service is currently unavailable. Please try again later."));
    }
}