package com.example.shop.services;

import com.example.shop.exceptions.NotFoundException;
import com.example.shop.models.Category;
import com.example.shop.models.Order;
import com.example.shop.models.Product;
import com.example.shop.models.User;
import com.example.shop.repositories.CategoryRepository;
import com.example.shop.repositories.OrderRepository;
import com.example.shop.repositories.ProductRepository;
import com.example.shop.repositories.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
    private static final String CATEGORY_NOT_FOUND = "Category not found with id: ";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String PRODUCT_NOT_FOUND = "Product not found";

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final Cache<String, List<Order>> orderCache;
    private final Cache<String, List<Order>> orderByUserNameCache;

    @Autowired
    public ShopService(
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            CategoryRepository categoryRepository,
            Cache<String, List<Order>> orderCache,
            Cache<String, List<Order>> orderByUserNameCache) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.orderCache = orderCache;
        this.orderByUserNameCache = orderByUserNameCache;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        List<Category> realCategories = product.getCategories().stream()
            .map(c -> categoryRepository.findById(c.getId())
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND + c.getId())))
            .toList();

        product.setCategories(realCategories);

        return productRepository.save(product);
    }

    @Transactional
    public ResponseEntity<Product> updateProduct(Long id, Product updatedProduct) {
        try {
            return productRepository.findById(id).map(product -> {
                product.setName(updatedProduct.getName());
                product.setPrice(updatedProduct.getPrice());

                List<Category> realCategories = new ArrayList<>(
                    updatedProduct.getCategories().stream()
                        .map(c -> categoryRepository.findById(c.getId())
                            .orElseThrow(() -> new 
                            NotFoundException(CATEGORY_NOT_FOUND + c.getId())))
                        .toList()
                );

                product.setCategories(realCategories);

                return ResponseEntity.ok(productRepository.save(product));
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка при обновлении продукта: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ResponseEntity<String> deleteProduct(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            throw new NotFoundException("🛑 Товар с id=" + id + " не найден для удаления");
        }

        Product product = optionalProduct.get();

        List<Long> affectedOrders = new ArrayList<>();
        List<Long> deletedOrders = new ArrayList<>();

        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            boolean removed = order.getProducts().removeIf(p -> p.getId().equals(product.getId()));
            if (removed) {
                if (order.getProducts().isEmpty()) {
                    orderRepository.delete(order);
                    deletedOrders.add(order.getId());
                } else {
                    orderRepository.save(order);
                    affectedOrders.add(order.getId());
                }
            }
        }

        product.getCategories().clear();
        productRepository.save(product);

        productRepository.delete(product);

        String msg = "✅ Товар с id=" + id + " удалён.\n"
                + "Обновлены заказы: " + affectedOrders + "\n"
                + "Удалены пустые заказы: " + deletedOrders;
        if (logger.isInfoEnabled()) {
            logger.info(msg, sanitize(msg));
        }
        return ResponseEntity.ok(msg);
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
            return ResponseEntity.ok().<Void>build();
        }).orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public List<Order> getOrdersByUserNameCached(String userName) {
        List<Order> cached = orderByUserNameCache.getIfPresent(userName);
        if (cached != null) {
            if (logger.isInfoEnabled()) {
                logger.info("📦 Кэш: заказы пользователя с именем: {}", sanitize(userName));
            }
            return cached;
        }

        if (logger.isInfoEnabled()) {
            logger.info("🗃️ Запрос в БД: {}", sanitize(userName));
        }
        List<Order> orders = orderRepository.findOrdersByUserName(userName);

        logger.info("✅ put вызван с: {}", userName);
        orderByUserNameCache.put(userName, orders);

        return orders;
    }

    public void clearOrderByUserNameCache() {
        orderByUserNameCache.invalidateAll();
        logger.info("🧹 Кэш заказов по имени пользователя очищен");
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order saveOrder(Order order) {
        Long userId = order.getUser().getId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        List<Product> realProducts = order.getProducts().stream()
            .map(p -> productRepository.findById(p.getId())
                .orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND)))
            .toList();

        order.setUser(user);
        order.setProducts(realProducts);

        orderCache.invalidateAll();
        orderByUserNameCache.invalidateAll();
        logger.info("✅ Кэш заказов очищен после создания нового заказа");

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByProductName(String productName) {
        List<Order> cached = orderCache.getIfPresent(productName);
        if (cached != null) {
            if (logger.isInfoEnabled()) {
                logger.info("👉 Из кэша: {}", sanitize(productName));
            }
            return cached;
        }

        if (logger.isInfoEnabled()) {
            logger.info("🗃️ Запрос в БД: {}", sanitize(productName));
        }
        List<Order> orders = orderRepository.findOrdersByProductName(productName);

        logger.info("✅ put вызван с: {}", productName);
        orderCache.put(productName, orders);

        return orders;
    }

    public List<Order> getOrdersByProductNameNative(String productName) {
        return orderRepository.findOrdersByProductNameNative(productName);
    }

    public ResponseEntity<Void> deleteOrder(Long id) {
        orderCache.invalidateAll();
        return orderRepository.findById(id).map(order -> {
            orderRepository.delete(order);
            logger.info("🗑️ Заказ с id={} удалён", id);
            return ResponseEntity.ok().<Void>build();
        }).orElseThrow(() -> new NotFoundException("Заказ с id=" + id + " не найден"));
    }

    public void clearOrderCache() {
        orderCache.invalidateAll();
        logger.info("🧹 Кэш заказов очищен вручную");
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public ResponseEntity<Void> deleteCategory(Long id) {
        return categoryRepository.findById(id).map(category -> {
            boolean usedByAnyProduct = productRepository.findAll().stream()
                .anyMatch(product -> product.getCategories().contains(category));

            if (usedByAnyProduct) {
                throw new IllegalStateException(
                    "❌ Нельзя удалить категорию, она используется продуктами");
            }

            categoryRepository.delete(category);
            return ResponseEntity.ok().<Void>build();
        }).orElseThrow(() -> new NotFoundException("Категория с id=" + id + " не найдена"));
    }

    public List<Product> saveAllProducts(List<Product> products) {
        return products.stream()
            .map(product -> {
                List<Category> realCategories = product.getCategories().stream()
                    .map(c -> categoryRepository.findById(c.getId())
                            .orElseThrow(() -> new NotFoundException(
                                CATEGORY_NOT_FOUND + c.getId())))
                    .toList();
                product.setCategories(realCategories);
                return product;
            })
            .map(productRepository::save)
            .toList();
    }

    private String sanitize(String input) {
        return input.replaceAll("[\n\r\t]", "_").replaceAll("[^\\w@.-]", "_");
    }
}
