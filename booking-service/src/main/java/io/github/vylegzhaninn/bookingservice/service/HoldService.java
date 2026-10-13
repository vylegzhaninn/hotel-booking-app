package io.github.vylegzhaninn.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Временный холд номера в Redis: маркер {@code booking:hold:{id}} живёт, пока
 * бронирование ожидает оплаты. БД остаётся источником истины, Redis даёт быстрый
 * доступ к состоянию холда и автоматически удаляет его по TTL.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HoldService {

    private static final String HOLD_KEY_PREFIX = "booking:hold:";

    private final StringRedisTemplate redisTemplate;

    public void acquire(long bookingId, long roomId, Duration ttl) {
        redisTemplate.opsForValue().set(holdKey(bookingId), String.valueOf(roomId), ttl);
        log.debug("Создан холд бронирования {} на {}", bookingId, ttl);
    }

    public void release(long bookingId) {
        var removed = redisTemplate.delete(holdKey(bookingId));
        if (Boolean.TRUE.equals(removed)) {
            log.debug("Снят холд бронирования {}", bookingId);
        }
    }

    public boolean isActive(long bookingId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(holdKey(bookingId)));
    }

    private String holdKey(long bookingId) {
        return HOLD_KEY_PREFIX + bookingId;
    }
}
