package io.github.vylegzhaninn.bookingservice.repository;

import io.github.vylegzhaninn.bookingservice.model.Room;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(Room.class)
public interface RoomRepository {

    String SELECT_ROOM = """
        SELECT id, hotel_name, room_number, category, price_per_night
        FROM rooms
        """;

    @SqlQuery(SELECT_ROOM + " WHERE id = :id")
    Optional<Room> findById(@Bind("id") long id);

    @SqlQuery(SELECT_ROOM + " ORDER BY hotel_name, room_number")
    List<Room> findAll();

    @SqlQuery("""
        SELECT r.id, r.hotel_name, r.room_number, r.category, r.price_per_night
        FROM rooms r
        WHERE NOT EXISTS (
            SELECT 1 FROM bookings b
            WHERE b.room_id = r.id
              AND b.status IN ('PENDING', 'CONFIRMED')
              AND b.check_in < :checkOut
              AND b.check_out > :checkIn
        )
        ORDER BY r.hotel_name, r.room_number
        """)
    List<Room> findAvailable(@Bind("checkIn") LocalDate checkIn, @Bind("checkOut") LocalDate checkOut);
}
