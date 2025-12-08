CREATE TABLE IF NOT EXISTS categories(
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(30) NOT NULL,
    keywords TEXT[],

    CONSTRAINT fk_users_categories FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);