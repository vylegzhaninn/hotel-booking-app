CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    hotel_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(10) NOT NULL,
    category VARCHAR(30) NOT NULL,
    price_per_night NUMERIC(10, 2) NOT NULL CHECK (price_per_night > 0),
    CONSTRAINT uq_rooms_hotel_number UNIQUE (hotel_name, room_number)
);
