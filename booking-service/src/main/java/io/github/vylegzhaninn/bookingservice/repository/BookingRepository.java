package io.github.vylegzhaninn.bookingservice.repository;

import io.github.vylegzhaninn.bookingservice.model.Booking;
import io.github.vylegzhaninn.bookingservice.model.BookingResponse;
import io.github.vylegzhaninn.bookingservice.model.BookingStatus;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    String SELECT_BOOKING = """
        SELECT id, room_id, user_name, check_in, check_out, status, total_amount, expires_at, created_at
        FROM bookings
        """;

    String SELECT_USER_VIEW = """
        SELECT b.id, b.room_id, r.hotel_name, r.room_number, b.check_in, b.check_out,
               b.status, b.total_amount, b.expires_at
        FROM bookings b
        JOIN rooms r ON r.id = b.room_id
        """;

    @SqlQuery(SELECT_BOOKING + " WHERE id = :id")
    @RegisterConstructorMapper(Booking.class)
    Optional<Booking> findById(@Bind("id") long id);

    @SqlQuery(SELECT_USER_VIEW + " WHERE b.id = :id")
    @RegisterConstructorMapper(BookingResponse.class)
    Optional<BookingResponse> findResponseById(@Bind("id") long id);

    @SqlQuery(SELECT_USER_VIEW + " WHERE b.user_name = :userName ORDER BY b.created_at DESC")
    @RegisterConstructorMapper(BookingResponse.class)
    List<BookingResponse> findResponsesByUserName(@Bind("userName") String userName);

    @SqlQuery("""
        SELECT EXISTS (
            SELECT 1 FROM bookings
            WHERE room_id = :roomId
              AND status IN ('PENDING', 'CONFIRMED')
              AND check_in < :checkOut
              AND check_out > :checkIn
        )
        """)
    boolean existsActiveOverlap(
        @Bind("roomId") long roomId,
        @Bind("checkIn") LocalDate checkIn,
        @Bind("checkOut") LocalDate checkOut
    );

    @SqlQuery("""
        INSERT INTO bookings (room_id, user_name, check_in, check_out, status, total_amount, expires_at)
        VALUES (:roomId, :userName, :checkIn, :checkOut, 'PENDING', :totalAmount, :expiresAt)
        RETURNING id, room_id, user_name, check_in, check_out, status, total_amount, expires_at, created_at
        """)
    @RegisterConstructorMapper(Booking.class)
    Booking insert(
        @Bind("roomId") long roomId,
        @Bind("userName") String userName,
        @Bind("checkIn") LocalDate checkIn,
        @Bind("checkOut") LocalDate checkOut,
        @Bind("totalAmount") BigDecimal totalAmount,
        @Bind("expiresAt") Instant expiresAt
    );

    @SqlUpdate("UPDATE bookings SET status = :next WHERE id = :id AND status = :current")
    int updateStatusIfCurrent(
        @Bind("id") long id,
        @Bind("current") BookingStatus current,
        @Bind("next") BookingStatus next
    );

    @SqlQuery("SELECT id FROM bookings WHERE status = 'PENDING' AND expires_at <= now()")
    List<Long> findPendingExpiredIds();

    @SqlUpdate("UPDATE bookings SET status = 'EXPIRED' WHERE id IN (<ids>)")
    int markExpired(@BindList("ids") List<Long> ids);
}
