package com.example.shop.services;

import com.example.shop.models.Category;
import com.example.shop.models.Order;
import com.example.shop.models.Product;
import com.example.shop.models.User;
import com.example.shop.repositories.CategoryRepository;
import com.example.shop.repositories.OrderRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ShopService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ShopService(
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public ResponseEntity<Product> updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            product.setCategories(updatedProduct.getCategories());
            return ResponseEntity.ok(productRepository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteProduct(Long id) {
        return productRepository.findById(id).map(product -> {
            productRepository.delete(product);
            return ResponseEntity.ok().<Void>build(); // Добавляем <Void>
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build()); // Добавляем <Void>
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public ResponseEntity<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> deleteUser(Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().<Void>build(); // Добавляем <Void>
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build()); // Добавляем <Void>
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
}