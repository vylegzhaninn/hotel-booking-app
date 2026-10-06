CREATE TABLE users (
    id BIGSERIAL primary key,
    name VARCHAR(15) not null,
    password VARCHAR(30) not null,
    role VARCHAR(255) not null
);