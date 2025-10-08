CREATE TABLE IF NOT EXISTS users_groups (
    user_id INT NOT NULL,
    group_id INT NOT NULL,

    CONSTRAINT pk_users_groups PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_users_groups_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_groups_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);