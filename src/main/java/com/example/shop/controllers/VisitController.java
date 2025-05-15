package com.example.shop.controllers;

import com.example.shop.services.VisitCounterService;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visits")
public class VisitController {

    private final VisitCounterService visitCounterService;

    @Autowired
    public VisitController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/count")
    public Map<String, Integer> getAllVisits() {
        return visitCounterService.getAllStats().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    @GetMapping("/count/endpoint")
    public int getCount(@RequestParam String path) {
        return visitCounterService.getVisitCount(path);
    }
    
    @DeleteMapping("/reset")
    public String resetAll() {
        visitCounterService.resetAll();
        return "📉 Статистика посещений сброшена";
    }

    @GetMapping("/test")
    public String visitTest() {
        visitCounterService.recordVisit("/visit/test");
        return "OK";
    }
}