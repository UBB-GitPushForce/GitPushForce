CREATE TABLE IF NOT EXISTS categories(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(30),

    CONSTRAINT fk_group_logs_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
)