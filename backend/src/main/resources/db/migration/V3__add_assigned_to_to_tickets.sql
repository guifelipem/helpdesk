ALTER TABLE tickets
ADD COLUMN assigned_to_id BIGINT;

ALTER TABLE tickets
ADD CONSTRAINT fk_tickets_assigned_to_id
FOREIGN KEY (assigned_to_id)
REFERENCES users(id);