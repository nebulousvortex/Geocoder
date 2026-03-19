CREATE TABLE IF NOT EXISTS statuses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(7) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(500) UNIQUE NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    aliases VARCHAR(2000),
    normalized_address VARCHAR(2000),
    country VARCHAR(100),
    city VARCHAR(100),
    status_id BIGINT NOT NULL REFERENCES statuses(id),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO statuses (name, color) VALUES ('В процессе', '#FFA500')
ON CONFLICT (name) DO NOTHING;

INSERT INTO statuses (name, color) VALUES ('Готово', '#008000')
ON CONFLICT (name) DO NOTHING;

INSERT INTO statuses (name, color) VALUES ('Ошибка', '#FF0000')
ON CONFLICT (name) DO NOTHING;