CREATE TABLE IF NOT EXISTS expenses (
    id SERIAL PRIMARY KEY,
    user_id INT,
    group_id INT,
    title TEXT,
    category TEXT,
    amount FLOAT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,

    CONSTRAINT chk_expenses_one_fk CHECK (
        (user_id IS NOT NULL AND group_id IS NULL)
        OR
        (user_id IS NULL AND group_id IS NOT NULL)
    )
);
