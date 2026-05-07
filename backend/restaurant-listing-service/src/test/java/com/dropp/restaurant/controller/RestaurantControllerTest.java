package com.dropp.restaurant.controller;

import com.dropp.restaurant.config.GlobalExceptionHandler;
import com.dropp.restaurant.controlller.RestaurantController;
import com.dropp.restaurant.dto.RestaurantDTO;
import com.dropp.restaurant.exception.RestaurantNotFoundException;
import com.dropp.restaurant.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RestaurantController.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
public class RestaurantControllerTest {
    private static final Long RESTAURANT_ID = 1L;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RestaurantService restaurantService;

    private List<RestaurantDTO> buildListOfRestaurantDto() {
        return Arrays.asList(
                new RestaurantDTO(1L, "Restaurant 1", "Address 1", "City 1", "Description Restaurant 1", "/images/restaurant1.png", 4.5),
                new RestaurantDTO(2L, "Restaurant 2", "Address 2", "City 2", "Description Restaurant 2", "/images/restaurant2.png", 4.0)
        );
    }

    private RestaurantDTO buildRestaurantDTO() {
        return new RestaurantDTO(RESTAURANT_ID, "Restaurant 1", "Address 1", "City 1",
                "Description Restaurant 1", "/images/restaurant1.png", 4.5);
    }

    @Test
    public void fetchRestaurants_shouldReturnMappedDtoPage() throws Exception {
        List<RestaurantDTO> mockRestaurants = buildListOfRestaurantDto();
        Page<RestaurantDTO> mockPage = new PageImpl<>(mockRestaurants);
        when(restaurantService.fetchRestaurants(0, 10)).thenReturn(mockPage);
        mockMvc.perform(get("/restaurant/fetchRestaurants")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void fetchRestaurantById_whenExists_shouldReturn200() throws Exception {
        RestaurantDTO dto = buildRestaurantDTO();
        when(restaurantService.fetchRestaurantById(RESTAURANT_ID)).thenReturn(dto);

        mockMvc.perform(get("/restaurant/fetchRestaurantById/{id}", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RESTAURANT_ID))
                .andExpect(jsonPath("$.name").value("Restaurant 1"));
    }

    @Test
    void fetchRestaurantById_whenNotExists_shouldReturn404() throws Exception {
        when(restaurantService.fetchRestaurantById(RESTAURANT_ID))
                .thenThrow(new RestaurantNotFoundException(RESTAURANT_ID));
        mockMvc.perform(get("/restaurant/fetchRestaurantById/{id}", RESTAURANT_ID))
                .andExpect(status().isNotFound());
    }
}
