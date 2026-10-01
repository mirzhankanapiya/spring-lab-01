package kz.iitu.springlab.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
@Order(0)
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastResetTimes = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object limit(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String methodName = pjp.getSignature().toShortString();
        long currentTime = System.currentTimeMillis();

        lastResetTimes.putIfAbsent(methodName, currentTime);
        requestCounts.putIfAbsent(methodName, new AtomicInteger(0));

        if (currentTime - lastResetTimes.get(methodName) > 60_000) {
            lastResetTimes.put(methodName, currentTime);
            requestCounts.get(methodName).set(0);
        }

        int currentRequests = requestCounts.get(methodName).incrementAndGet();
        if (currentRequests > rateLimit.perMinute()) {
            log.warn("[RATE LIMIT] Exceeded limit for {}: {}/{}", methodName, currentRequests, rateLimit.perMinute());
            throw new IllegalStateException("Rate limit exceeded. Maximum " + rateLimit.perMinute() + " requests per minute allowed.");
        }

        return pjp.proceed();
    }
}