package com.dropp.restaurant.repo;

import com.dropp.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepo extends JpaRepository<Restaurant, Long> {

}
