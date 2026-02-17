package com.navneet.telegrambot.config;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<Long, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long chatId) {
        return buckets.computeIfAbsent(chatId, id -> createBucket());
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(requestsPerMinute).refillGreedy(requestsPerMinute, Duration.ofMinutes(1)))
                .build();
    }
}
