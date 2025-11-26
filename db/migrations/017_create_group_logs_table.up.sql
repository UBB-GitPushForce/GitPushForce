CREATE TABLE IF NOT EXISTS group_logs (
    id SERIAL PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    action VARCHAR(20) NOT NULL, -- JOIN | LEAVE
    created_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_group_logs_group FOREIGN KEY (group_id)
        REFERENCES groups(id) ON DELETE CASCADE,

    CONSTRAINT fk_group_logs_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_group_log_action CHECK (
        action IN ('JOIN', 'LEAVE')
    )
);
