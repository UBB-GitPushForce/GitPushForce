CREATE TABLE IF NOT EXISTS users_categories(
    user_id INT NOT NULL,
    category_id INT NOT NULL,

    CONSTRAINT pk_users_categories PRIMARY KEY(user_id, category_id),
    CONSTRAINT fk_users_categories_users FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_categories_categories FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
)