DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM users
        GROUP BY LOWER(BTRIM(email))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Existem e-mails duplicados ao ignorar maiúsculas/minúsculas; corrija-os antes de aplicar a migração';
    END IF;
END $$;

UPDATE users SET email = LOWER(BTRIM(email));

CREATE UNIQUE INDEX uq_users_email_lower ON users (LOWER(email));
