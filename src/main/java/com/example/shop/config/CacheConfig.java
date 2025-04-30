package com.example.shop.config;

import com.example.shop.models.Order;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, List<Order>> orderCache() {
        return Caffeine.newBuilder()
                .maximumSize(100) // максимум 100 ключей в кэше
                .build();
    }

    @Bean
    public Cache<String, List<Order>> orderByUserNameCache() {
        return Caffeine.newBuilder()
                .maximumSize(50) // меньше, если пользователей меньше
                .build();
    }
}