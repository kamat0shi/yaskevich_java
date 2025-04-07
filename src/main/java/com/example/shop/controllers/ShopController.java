package com.example.shop.controllers;

import com.example.shop.models.Category;
import com.example.shop.models.Order;
import com.example.shop.models.Product;
import com.example.shop.models.User;
import com.example.shop.services.ShopService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling shop-related API requests.
 */
@RestController
@RequestMapping("/api")
public class ShopController {

    private final ShopService shopService;

    /**
     * Constructor-based dependency injection for ShopService.
     *
     * @param shopService the service layer for handling product operations
     */
    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * Retrieves all available products.
     *
     * @return a list of products
     */
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return shopService.getAllProducts();
    }

    /**
     * Retrieves a specific product by its ID.
     *
     * @param id the ID of the product
     * @return ResponseEntity containing the product if found, or an error message with 404 status
     */
    @GetMapping("/product/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable Long id) {
        Optional<Product> product = shopService.getProductById(id);
        
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Product not found"));
        }
    }

    /** === CRUD ДЛЯ USERS === */
    
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return shopService.getAllUsers();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return shopService.saveUser(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return shopService.updateUser(id, updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return shopService.deleteUser(id);
    }

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return shopService.getAllOrders();
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        return shopService.saveOrder(order);
    }

    @GetMapping("/orders/by-product")
    public List<Order> getOrdersByProduct(@RequestParam String productName) {
        return shopService.getOrdersByProductName(productName);
    }

    @GetMapping("/orders/by-productN")
    public List<Order> getOrdersByProductNameNative(@RequestParam String productName) {
        return shopService.getOrdersByProductNameNative(productName);
    }

    @GetMapping("/orders/by-username")
    public ResponseEntity<List<Order>> getOrdersByUserName(@RequestParam String userName) {
        List<Order> orders = shopService.getOrdersByUserNameCached(userName);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/orders/cache/clear")
    public ResponseEntity<String> clearOrderCache() {
        shopService.clearOrderCache();
        return ResponseEntity.ok("🧹 Кэш заказов очищен");
    }

    @DeleteMapping("/cache/orders-by-username")
    public ResponseEntity<String> clearOrderUserNameCache() {
        shopService.clearOrderByUserNameCache();
        return ResponseEntity.ok("Кэш заказов по имени пользователя очищен");
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return shopService.getAllCategories();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category category) {
        return shopService.saveCategory(category);
    }

    /**
     * Creates a new product in the system.
     *
     * @param product the product to be saved
     * @return the saved product
     */
    /** === CRUD ДЛЯ PRODUCTS === */

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = shopService.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
        @PathVariable Long id, @RequestBody Product updatedProduct) {
        return shopService.updateProduct(id, updatedProduct);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        return shopService.deleteProduct(id);
    }
}