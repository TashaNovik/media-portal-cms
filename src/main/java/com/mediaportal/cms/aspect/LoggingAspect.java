package com.mediaportal.cms.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for centralized logging of controller and service operations.
 * Implements cross-cutting concerns using AOP (Aspect Oriented Programming).
 * 
 * Disabled during tests to avoid interfering with MockMvc and Spring filters.
 */
@Aspect
@Component
@Slf4j
@Profile("!test")
public class LoggingAspect {

    /**
     * Pointcut for all controller methods - specifically targeting REST controller classes
     */
    @Pointcut("execution(public * com.mediaportal.cms.controller.*Controller.*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut for all service methods - specifically targeting service classes
     */
    @Pointcut("execution(public * com.mediaportal.cms.service.*Service.*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut for all repository methods
     */
    @Pointcut("execution(public * com.mediaportal.cms.repository.*Repository.*(..))")
    public void repositoryMethods() {}

    /**
     * Log before controller method execution
     */
    @Before("controllerMethods()")
    public void logControllerMethodCall(JoinPoint joinPoint) {
        log.info("API Call: {}.{}() with arguments: {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * Log after successful controller method execution
     */
    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logControllerMethodReturn(JoinPoint joinPoint, Object result) {
        log.info("API Response: {}.{}() returned successfully",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName());
    }

    /**
     * Log exceptions in controller methods
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logControllerException(JoinPoint joinPoint, Throwable exception) {
        log.error("API Error: {}.{}() threw exception: {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                exception.getMessage());
    }

    /**
     * Measure execution time for service methods
     */
    @Around("serviceMethods()")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.debug("Service: {}.{}() executed in {} ms",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Service: {}.{}() failed after {} ms with error: {}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    executionTime,
                    ex.getMessage());
            throw ex;
        }
    }

    /**
     * Log security-related operations
     */
    @Before("execution(* com.mediaportal.cms.security..*.*(..))")
    public void logSecurityOperation(JoinPoint joinPoint) {
        log.debug("Security: {}.{}()",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName());
    }
}
