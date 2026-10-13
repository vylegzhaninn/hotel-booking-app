package io.github.vylegzhaninn.paymentservice.repository;

import io.github.vylegzhaninn.paymentservice.model.Payment;
import io.github.vylegzhaninn.paymentservice.model.PaymentStatus;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(Payment.class)
public interface PaymentRepository {

    String SELECT_PAYMENT = """
        SELECT id, booking_id, user_name, amount, status, created_at
        FROM payments
        """;

    @SqlQuery("SELECT EXISTS (SELECT 1 FROM payments WHERE booking_id = :bookingId)")
    boolean existsByBookingId(@Bind("bookingId") long bookingId);

    @SqlQuery(SELECT_PAYMENT + " WHERE booking_id = :bookingId AND user_name = :userName ORDER BY created_at")
    List<Payment> findByBookingIdAndUserName(@Bind("bookingId") long bookingId, @Bind("userName") String userName);

    @SqlQuery(SELECT_PAYMENT + " WHERE booking_id = :bookingId AND status = 'SUCCEEDED' LIMIT 1")
    Optional<Payment> findSucceededByBookingId(@Bind("bookingId") long bookingId);

    @SqlQuery("""
        INSERT INTO payments (booking_id, user_name, amount, status)
        VALUES (:bookingId, :userName, :amount, :status)
        RETURNING id, booking_id, user_name, amount, status, created_at
        """)
    Payment insert(
        @Bind("bookingId") long bookingId,
        @Bind("userName") String userName,
        @Bind("amount") BigDecimal amount,
        @Bind("status") PaymentStatus status
    );

    @SqlUpdate("UPDATE payments SET status = 'REFUNDED' WHERE booking_id = :bookingId AND status = 'SUCCEEDED'")
    int markRefunded(@Bind("bookingId") long bookingId);
}
