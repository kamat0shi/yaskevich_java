package com.example.shop.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ShopServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Cache<String, List<Order>> orderCache;

    @Mock
    private Cache<String, List<Order>> orderByUserNameCache;

    @InjectMocks
    private ShopService shopService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts() {
        Product product = new Product();
        product.setName("Test Product");
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = shopService.getAllProducts();
        assertEquals(1, products.size());
        assertEquals("Test Product", products.get(0).getName());
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setName("John");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User saved = shopService.saveUser(user);
        assertEquals("John", saved.getName());
    }

    @Test
    void testDeleteUserWhenExists() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = shopService.deleteUser(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUserWhenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            com.example.shop.exceptions.NotFoundException.class,
            () -> shopService.deleteUser(1L)
        );
    }

    @Test
    void testSaveProductWithValidCategories() {
        Category category = new Category();
        category.setId(1L);

        Product product = new Product();
        product.setCategories(List.of(category));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = shopService.saveProduct(product);
        assertEquals(1, saved.getCategories().size());
    }

    @Test
    void testDeleteOrder_whenExists() {
        Order order = new Order();
        order.setId(10L);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        ResponseEntity<Void> response = shopService.deleteOrder(10L);
        response.getStatusCode().value();
        verify(orderRepository).delete(order);
    }

    @Test
    void testDeleteOrder_whenNotExists() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(com.example.shop.exceptions.NotFoundException.class,
            () -> shopService.deleteOrder(10L));
    }

    @Test
    void testUpdateProduct_whenExists() {
        Product oldProduct = new Product();
        oldProduct.setId(1L);
        oldProduct.setName("Old");
        oldProduct.setPrice(100.0);

        Product newProduct = new Product();
        newProduct.setName("New");
        newProduct.setPrice(200.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(oldProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<Product> response = shopService.updateProduct(1L, newProduct);

        response.getStatusCode().value();
        assertEquals("New", response.getBody().getName());
        assertEquals(200.0, response.getBody().getPrice());
    }

    @Test
    void testUpdateUser_whenExists() {
        User oldUser = new User();
        oldUser.setId(1L);
        oldUser.setName("Old");

        User newUser = new User();
        newUser.setName("New");

        when(userRepository.findById(1L)).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<User> response = shopService.updateUser(1L, newUser);

        response.getStatusCode().value();
        assertEquals("New", response.getBody().getName());
    }

    @Test
    void testDeleteCategory_whenUnused() {
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<Void> response = shopService.deleteCategory(1L);

        response.getStatusCode().value();
        verify(categoryRepository).delete(category);
    }

    @Test
    void testDeleteCategory_whenUsedByProduct() {
        Category category = new Category();
        category.setId(1L);

        Product product = new Product();
        product.setCategories(List.of(category));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findAll()).thenReturn(List.of(product));

        assertThrows(IllegalStateException.class, () -> shopService.deleteCategory(1L));
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setName("User1");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = shopService.getAllUsers();
        assertEquals(1, result.size());
        assertEquals("User1", result.get(0).getName());
    }

    @Test
    void testGetAllOrders() {
        Order order = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = shopService.getAllOrders();
        assertEquals(1, result.size());
    }

    @Test
    void testGetOrdersByProductNameNative() {
        Order order = new Order();
        when(orderRepository.findOrdersByProductNameNative("nativeProduct"))
            .thenReturn(List.of(order));

        List<Order> result = shopService.getOrdersByProductNameNative("nativeProduct");
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateProduct_whenNotExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = shopService.updateProduct(1L, new Product());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUser_whenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = shopService.updateUser(1L, new User());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSaveProduct_whenCategoryNotFound() {
        Category category = new Category();
        category.setId(99L);
        Product product = new Product();
        product.setCategories(List.of(category));

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        List<Product> products = List.of(product); 
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            shopService.saveAllProducts(products)
        );

        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    void testSaveOrder_whenUserNotFound() {
        Order order = new Order();
        User user = new User();
        user.setId(1L);
        order.setUser(user);
    
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
    
        assertThrows(RuntimeException.class, () -> shopService.saveOrder(order));
    }

    @Test
    void testSaveOrder_whenProductNotFound() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
        Product product = new Product();
        product.setId(99L);
    
        Order order = new Order();
        order.setUser(user);
        order.setProducts(List.of(product));
    
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
    
        assertThrows(RuntimeException.class, () -> shopService.saveOrder(order));
    }

    @Test
    void testGetProductById() {
        Product product = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = shopService.getProductById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testSaveAllProducts_withValidCategories() {
        Category category = new Category();
        category.setId(1L);

        Product product = new Product();
        product.setName("Bulk Product");
        product.setCategories(List.of(category));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        List<Product> saved = shopService.saveAllProducts(List.of(product));

        assertEquals(1, saved.size());
        assertEquals("Bulk Product", saved.get(0).getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testSaveAllProducts_whenCategoryNotFound() {
        Category category = new Category();
        category.setId(99L);

        Product product = new Product();
        product.setCategories(List.of(category));

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Вынеси вызываемый код в отдельный Executable
        Executable executable = () -> {
            List<Product> input = List.of(product);
            shopService.saveAllProducts(input);
        };

        RuntimeException exception = assertThrows(RuntimeException.class, executable);
        assertTrue(exception.getMessage().contains("Category not found"));
    }

    @Test
    void testSaveCategory() {
        Category category = new Category();
        category.setName("New Category");

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = shopService.saveCategory(category);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
    }

    @Test
    void testGetAllCategories() {
        Category category = new Category();
        category.setName("C1");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = shopService.getAllCategories();
        assertEquals(1, result.size());
        assertEquals("C1", result.get(0).getName());
    }

    @Test
    void testDeleteProduct_whenProductExistsWithOrders() {
        Product product = new Product();
        product.setId(1L);
        product.setCategories(new ArrayList<>());

        Order orderWithProduct = new Order();
        orderWithProduct.setId(100L);
        orderWithProduct.setProducts(new ArrayList<>(List.of(product)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findAll()).thenReturn(List.of(orderWithProduct));
        when(productRepository.save(any())).thenReturn(product);

        ResponseEntity<String> response = shopService.deleteProduct(1L);

        response.getStatusCode().value();
        assertTrue(response.getBody().contains("удалён"));
        verify(orderRepository).delete(any(Order.class));
        verify(productRepository).delete(product);
    }

    @Test
    void testDeleteProduct_updatesOrderWithoutDeleting() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setCategories(new ArrayList<>());

        Product product2 = new Product();
        product2.setId(2L);

        Order order = new Order();
        order.setId(100L);
        order.setProducts(new ArrayList<>(List.of(product1, product2)));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(productRepository.save(any())).thenReturn(product1);

        ResponseEntity<String> response = shopService.deleteProduct(1L);

        response.getStatusCode().value();
        verify(orderRepository).save(any(Order.class));
        verify(orderRepository, never()).delete(any(Order.class));
    }
}