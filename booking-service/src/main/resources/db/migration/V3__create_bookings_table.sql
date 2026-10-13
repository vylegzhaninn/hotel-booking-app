CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES rooms (id),
    user_name VARCHAR(50) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount > 0),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_bookings_dates CHECK (check_out > check_in)
);

CREATE INDEX idx_bookings_room_status_dates ON bookings (room_id, status, check_in, check_out);
CREATE INDEX idx_bookings_user_name ON bookings (user_name);

ALTER TABLE bookings
    ADD CONSTRAINT excl_bookings_no_overlap
    EXCLUDE USING gist (
        room_id WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status IN ('PENDING', 'CONFIRMED'));
