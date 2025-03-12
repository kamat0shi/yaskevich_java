package com.example.shop.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling shop-related API endpoints.
 */
@RestController
@RequestMapping("/api")
public class ShopController {

    /**
     * Welcome endpoint that greets the user.
     *
     * @param name the name of the user (optional, default is "Guest")
     * @return a welcome message in JSON format
     */
    @GetMapping("/welcome")
    public Map<String, String> welcome(
            @RequestParam(required = false, defaultValue = "Guest") String name) {
        return Map.of("message", "Welcome, " + name + "!");
    }

    /**
     * Retrieves product details by ID.
     *
     * @param id the product ID
     * @return JSON response with product ID and status
     */
    @GetMapping("/product/{id}")
    public Map<String, String> getProductById(@PathVariable String id) {
        return Map.of("productId", id, "status", "Product found");
    }

    /**
     * Searches for products based on category and price range.
     *
     * @param category the product category (default: "all")
     * @param minPrice minimum price filter (default: "0")
     * @param maxPrice maximum price filter (default: "10000")
     * @return JSON response with search criteria and result status
     */
    @GetMapping("/search")
    public Map<String, String> searchProduct(
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(required = false, defaultValue = "0") String minPrice,
            @RequestParam(required = false, defaultValue = "10000") String maxPrice) {
        return Map.of(
                "category", category,
                "minPrice", minPrice,
                "maxPrice", maxPrice,
                "result", "Multiple products found"
        );
    }
}
