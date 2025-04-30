package com.example.shop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.shop.services.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("📥 Вызов метода: {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(value = "execution(* com.example.shop.services.*.*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logger.info(
            "📤 Завершение метода: {} -> результат: {}", 
            joinPoint.getSignature().toShortString(), result);
    }

    @AfterThrowing(value = "execution(* com.example.shop.services.*.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logger.error(
            "❌ Ошибка в методе: {} -> {}", 
            joinPoint.getSignature().toShortString(), ex.getMessage());
    }
}