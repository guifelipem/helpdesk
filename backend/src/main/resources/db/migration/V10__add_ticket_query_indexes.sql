CREATE INDEX idx_tickets_assigned_status_updated
    ON tickets (assigned_to_id, status, updated_at);

CREATE INDEX idx_tickets_created_by_updated
    ON tickets (created_by_id, updated_at);

CREATE INDEX idx_tickets_status_updated
    ON tickets (status, updated_at);
