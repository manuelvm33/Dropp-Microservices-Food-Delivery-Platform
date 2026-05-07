package com.dropp.restaurant.exception;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(Long id) {
        super("Restaurant not found with id: " + id);
    }
}
