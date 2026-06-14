CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT NOT NULL,

    CONSTRAINT fk_tickets_created_by
        FOREIGN KEY (created_by_id)
        REFERENCES users(id)
);