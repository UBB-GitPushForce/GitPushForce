ALTER TABLE expenses
ADD COLUMN category_id INT NOT NULL DEFAULT 1;

ALTER TABLE expenses
ADD CONSTRAINT fk_expense_category FOREIGN KEY(category_id) REFERENCES categories(id);