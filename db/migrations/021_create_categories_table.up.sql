CREATE TABLE IF NOT EXISTS categories(
    id SERIAL PRIMARY KEY,
    title VARCHAR(30) NOT NULL
);

INSERT INTO categories(title) VALUES('Demo category');