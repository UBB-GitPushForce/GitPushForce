CREATE TABLE expense_payments (
    expense_id INT NOT NULL,
    user_id INT NOT NULL,
    paid_at TIMESTAMP DEFAULT NOW(),

    PRIMARY KEY (expense_id, user_id),

    CONSTRAINT fk_payment_expense FOREIGN KEY (expense_id)
        REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);
