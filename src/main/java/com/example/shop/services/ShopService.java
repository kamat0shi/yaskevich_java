package com.example.shop.services;

import com.example.shop.models.Category;
import com.example.shop.models.Order;
import com.example.shop.models.Product;
import com.example.shop.models.User;
import com.example.shop.repositories.CategoryRepository;
import com.example.shop.repositories.OrderRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.repositories.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ShopService {

    private final Map<String, List<Order>> orderCache = new HashMap<>();
    private final Map<String, List<Order>> orderByUserNameCache = new HashMap<>();
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
        // Загружаем реальные категории из базы данных
        List<Category> realCategories = product.getCategories().stream()
            .map(c -> categoryRepository.findById(c.getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " 
                + c.getId())))
            .toList();
    
        product.setCategories(realCategories); // Устанавливаем реальные сущности
    
        return productRepository.save(product); // Сохраняем продукт
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

    public List<Order> getOrdersByUserNameCached(String userName) {
        if (orderByUserNameCache.containsKey(userName)) {
            System.out.println("📦 Кэш: заказы пользователя с именем: " + userName);
            return orderByUserNameCache.get(userName);
        }
        System.out.println("🗃️ Запрос в БД: " + userName);
    
        List<Order> orders = orderRepository.findOrdersByUserName(userName);
        orderByUserNameCache.put(userName, orders);
        return orders;
    }

    public void clearOrderByUserNameCache() {
        orderByUserNameCache.clear();
        System.out.println("🧹 Кэш заказов по имени пользователя очищен");
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order saveOrder(Order order) {
        Long userId = order.getUser().getId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    
        List<Product> realProducts = order.getProducts().stream()
            .map(p -> productRepository.findById(p.getId())
                .orElseThrow(() -> new RuntimeException("Product not found")))
            .toList();
    
        order.setUser(user);
        order.setProducts(realProducts);
    
        orderCache.clear();
        System.out.println("✅ Кэш заказов очищен после создания нового заказа");
    
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByProductName(String productName) {
        if (orderCache.containsKey(productName)) {
            System.out.println("👉 Из кэша: " + productName);
            return orderCache.get(productName);
        }
    
        System.out.println("🗃️ Запрос в БД: " + productName);
        List<Order> orders = orderRepository.findOrdersByProductName(productName);
        orderCache.put(productName, orders);
        return orders;
    }

    public List<Order> getOrdersByProductNameNative(String productName) {
        return orderRepository.findOrdersByProductNameNative(productName);
    }

    public void clearOrderCache() {
        orderCache.clear();
        System.out.println("🧹 Кэш заказов очищен вручную");
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
}