package com.dropp.restaurant.service;

import com.dropp.restaurant.dto.RestaurantDTO;
import com.dropp.restaurant.entity.Restaurant;
import com.dropp.restaurant.exception.RestaurantNotFoundException;
import com.dropp.restaurant.mapper.RestaurantMapper;
import com.dropp.restaurant.repo.RestaurantRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {
    private final RestaurantRepo restaurantRepo;

    public RestaurantService(RestaurantRepo restaurantRepo) {
        this.restaurantRepo = restaurantRepo;
    }

    public Page<RestaurantDTO> fetchRestaurants(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return restaurantRepo.findAll(pageRequest).map(RestaurantMapper.INSTANCE::mapRestaurantToRestaurantDTO);
    }

    public RestaurantDTO addRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = RestaurantMapper.INSTANCE.mapRestaurantDTOToRestaurant(restaurantDTO);
        restaurantRepo.save(restaurant);
        return RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(restaurant);
    }

    public RestaurantDTO fetchRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepo.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id));
        return RestaurantMapper.INSTANCE.mapRestaurantToRestaurantDTO(restaurant);
    }
}
