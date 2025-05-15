package com.example.shop.services;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class VisitCounterService {
    private static final Logger logger = LoggerFactory.getLogger(VisitCounterService.class);
    private final ConcurrentHashMap<String, AtomicInteger> counterMap = new ConcurrentHashMap<>();
    private final RateLimiter rateLimiter = RateLimiter.create(1000);

    public void recordVisit(String endpoint) {
        if (rateLimiter.tryAcquire()) {
            counterMap.computeIfAbsent(endpoint, k -> new AtomicInteger(0)).incrementAndGet();
        } else {
            logger.warn("⛔ Превышен лимит запросов к {}", endpoint);
        }
    }

    public int getVisitCount(String endpoint) {
        return counterMap.getOrDefault(endpoint, new AtomicInteger(0)).get();
    }

    public ConcurrentMap<String, AtomicInteger> getAllStats() {
        return counterMap;
    }

    public void resetAll() {
        counterMap.clear();
    }
}