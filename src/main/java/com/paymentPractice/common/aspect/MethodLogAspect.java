package com.paymentPractice.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Aspect
@Component
public class MethodLogAspect {
    @Around("@annotation(methodLog)")
    public Object doLog(ProceedingJoinPoint joinPoint, MethodLog methodLog) throws  Throwable {
        String description = methodLog.description();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        log.info("{} start : [{}][{}]", description, uuid, joinPoint.getSignature());
        Object result = joinPoint.proceed();
        log.info("{} end : [{}][{}]",description, uuid, joinPoint.getSignature());
        return result;
    }
}
