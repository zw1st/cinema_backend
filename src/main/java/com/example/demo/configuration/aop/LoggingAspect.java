package com.example.demo.configuration.aop;

import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(
            LoggingAspect.class);

    @Around("""
                execution(* com.example.demo.service..*(..))
            """)
    public Object logServiceMethods(
            ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature()
                .getDeclaringType()
                .getSimpleName();

        String methodName = joinPoint.getSignature()
                .getName();

        try {

            log.info(
                    "START {}.{}",
                    className,
                    methodName);

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;

            log.info(
                    "SUCCESS {}.{} [{} ms]",
                    className,
                    methodName,
                    duration);

            return result;

        } catch (Exception e) {

            long duration = System.currentTimeMillis() - start;

            log.error(
                    "ERROR {}.{} [{} ms]: {}",
                    className,
                    methodName,
                    duration,
                    e.getMessage(),
                    e);

            throw e;
        }
    }
}