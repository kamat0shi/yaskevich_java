package com.example.shop.aspects;

import com.example.shop.services.VisitCounterService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class VisitAspect {

    private final VisitCounterService visitCounterService;
    private final HttpServletRequest request;

    public VisitAspect(VisitCounterService service, HttpServletRequest request) {
        this.visitCounterService = service;
        this.request = request;
    }

    @Before("execution(* com.example.shop.controllers.*.*(..))")
    public void countVisit() {
        String uri = request.getRequestURI();
        if (uri != null && !uri.startsWith("/visits")) { // исключаем сам /visits/*
            visitCounterService.recordVisit(uri);
        }
    }
}