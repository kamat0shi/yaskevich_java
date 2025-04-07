package com.example.shop.repositories;

import com.example.shop.models.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN o.products p WHERE p.name = :productName")
    List<Order> findOrdersByProductName(@Param("productName") String productName);

    @Query(value = """
        SELECT o.* FROM orders o
        JOIN order_product op ON o.id = op.order_id
        JOIN products p ON p.id = op.product_id
        WHERE p.name = :productName
        """, nativeQuery = true)
    List<Order> findOrdersByProductNameNative(@Param("productName") String productName);

    @Query("SELECT o FROM Order o WHERE o.user.name = :userName")
    List<Order> findOrdersByUserName(@Param("userName") String userName);
}