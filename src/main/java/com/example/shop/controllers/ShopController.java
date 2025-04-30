package com.example.shop.controllers;

import com.example.shop.models.Category;
import com.example.shop.models.Order;
import com.example.shop.models.Product;
import com.example.shop.models.User;
import com.example.shop.services.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

@Tag(name = "Shop API", description = "Операции с пользователями, заказами и продуктами")
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
    @Operation(summary = "Получить все продукты")
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
    @Operation(summary = "Получить продукт по айди")
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
    @Operation(summary = "Получить всех пользовтелей")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return shopService.getAllUsers();
    }

    @Operation(summary = "Добавить пользователя")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User saved = shopService.saveUser(user);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Изменить данные пользователя")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@Valid 
        @PathVariable Long id, @RequestBody User updatedUser) {
        return shopService.updateUser(id, updatedUser);
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return shopService.deleteUser(id);
    }

    @Operation(summary = "Получить все заказы")
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return shopService.getAllOrders();
    }

    @Operation(summary = "Создать заказ")
    @PostMapping("/orders")
    public Order createOrder(@Valid @RequestBody Order order) {
        return shopService.saveOrder(order);
    }

    @Operation(summary = "Получить заказы по продукту")
    @GetMapping("/orders/by-product")
    public List<Order> getOrdersByProduct(@RequestParam String productName) {
        return shopService.getOrdersByProductName(productName);
    }

    @Operation(summary = "Получить заказы по продукту")
    @GetMapping("/orders/by-productN")
    public List<Order> getOrdersByProductNameNative(@RequestParam String productName) {
        return shopService.getOrdersByProductNameNative(productName);
    }

    @Operation(summary = "Получить заказы по пользователю")
    @GetMapping("/orders/by-username")
    public ResponseEntity<List<Order>> getOrdersByUserName(@RequestParam String userName) {
        List<Order> orders = shopService.getOrdersByUserNameCached(userName);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Удалить заказ")
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        return shopService.deleteOrder(id);
    }

    @Operation(summary = "Очистить кэш заказов")
    @DeleteMapping("/orders/cache/clear")
    public ResponseEntity<String> clearOrderCache() {
        shopService.clearOrderCache();
        return ResponseEntity.ok("🧹 Кэш заказов очищен");
    }

    @Operation(summary = "Очистить кэш заказов по пользователю")
    @DeleteMapping("/cache/orders-by-username")
    public ResponseEntity<String> clearOrderUserNameCache() {
        shopService.clearOrderByUserNameCache();
        return ResponseEntity.ok("Кэш заказов по имени пользователя очищен");
    }

    @Operation(summary = "Получить список категорий")
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return shopService.getAllCategories();
    }

    @Operation(summary = "Добавить категорию")
    @PostMapping("/categories")
    public Category createCategory(@Valid @RequestBody Category category) {
        return shopService.saveCategory(category);
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        return shopService.deleteCategory(id);
    }

    /**
     * Creates a new product in the system.
     *
     * @param product the product to be saved
     * @return the saved product
     */
    /** === CRUD ДЛЯ PRODUCTS === */
    @Operation(summary = "Добавить продукт")
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = shopService.saveProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    @Operation(summary = "Изменить данные продукта")
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@Valid
        @PathVariable Long id, @RequestBody Product updatedProduct) {
        return shopService.updateProduct(id, updatedProduct);
    }

    @Operation(summary = "Удалить продукт")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return shopService.deleteProduct(id);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    
    @Operation(summary = "Добавить несколько продуктов")
    @PostMapping("/products/bulk")
    public ResponseEntity<List<Product>> createProducts(@RequestBody 
        List<@Valid Product> products) {
        List<Product> saved = shopService.saveAllProducts(products);
        return ResponseEntity.ok(saved);
    }
}