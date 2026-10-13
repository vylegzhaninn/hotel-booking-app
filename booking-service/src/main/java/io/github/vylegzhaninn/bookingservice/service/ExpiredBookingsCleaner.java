package io.github.vylegzhaninn.bookingservice.service;

import io.github.vylegzhaninn.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Периодически переводит просроченные PENDING-брони в EXPIRED
 * и снимает их Redis-холды.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredBookingsCleaner {

    private final BookingService bookingService;

    @Scheduled(fixedDelayString = "${booking.expiry-check-interval:30s}")
    public void expireOverduePendingBookings() {
        bookingService.expireOverduePendingBookings();
    }
}
