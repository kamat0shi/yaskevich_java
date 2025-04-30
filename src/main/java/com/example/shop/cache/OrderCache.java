package com.example.shop.cache;

import com.example.shop.models.Order;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderCache {
    private final Map<String, List<Order>> cache = new LinkedHashMap<>(16, 0.75f, true) {
        private static final int MAX_ENTRIES = 100;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Order>> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public List<Order> get(String key) {
        return cache.get(key);
    }

    public void put(String key, List<Order> orders) {
        cache.put(key, orders);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        cache.clear();
    }
}