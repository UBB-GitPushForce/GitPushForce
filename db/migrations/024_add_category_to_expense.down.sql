ALTER TABLE expenses
DROP CONSTRAINT fk_expense_category;

ALTER TABLE expenses
DROP COLUMN category_id;