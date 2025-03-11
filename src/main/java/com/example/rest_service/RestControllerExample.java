package com.example.rest_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RestControllerExample {

    @GetMapping("/hello")
    public String sayHello() {
        return "{\"message\": \"Hello, Spring Boot!\"}";
    }
    
    @GetMapping("/greet")
    public Map<String, String> greet(@RequestParam(required = false, defaultValue = "Guest") final String name,
                                     @RequestParam(required = false, defaultValue = "0") final String age) { 
        return Map.of(
                "message", "Hello, " + name + "!",
                "age", age
        );
    }

    @GetMapping("/echo/{word}")
    public Map<String, String> echo(@PathVariable final String word) { 
        return Map.of("echo", "You said: " + word);
    }
}