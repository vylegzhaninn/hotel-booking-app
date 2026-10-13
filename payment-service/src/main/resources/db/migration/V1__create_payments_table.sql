CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_booking_id ON payments (booking_id);
CREATE INDEX idx_payments_user_name ON payments (user_name);

-- защита от двойного успешного списания по одному бронированию
CREATE UNIQUE INDEX uq_payments_booking_succeeded ON payments (booking_id) WHERE status = 'SUCCEEDED';
