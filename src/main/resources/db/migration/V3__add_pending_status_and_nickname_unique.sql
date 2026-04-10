ALTER TABLE users
    ADD CONSTRAINT uq_users_nickname UNIQUE (nickname);

UPDATE users SET status = 'ACTIVE' WHERE status NOT IN ('ACTIVE', 'INACTIVE', 'DELETED', 'PENDING');
