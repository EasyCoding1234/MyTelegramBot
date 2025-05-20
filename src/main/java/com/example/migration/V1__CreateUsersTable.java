package com.example.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V1__CreateUsersTable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            // Таблица users
            statement.execute("""
                CREATE TABLE users (
                    id BIGSERIAL PRIMARY KEY,
                    telegram_id BIGINT NOT NULL UNIQUE,
                    username VARCHAR(255),
                    first_name VARCHAR(64),
                    last_name VARCHAR(64),
                    stars INT DEFAULT 0,
                    registered_at TIMESTAMP DEFAULT NOW() NOT NULL,
                    referral_code VARCHAR(8) UNIQUE,
                    invited_by_id BIGINT,
                    father_referer_id BIGINT,
                    count_invited_users INT DEFAULT 0,
                    referral_reward_given BOOLEAN DEFAULT FALSE,
                    version INT,
                    CONSTRAINT fk_invited_by FOREIGN KEY (invited_by_id) REFERENCES users(id),
                    CONSTRAINT fk_father_referer FOREIGN KEY (father_referer_id) REFERENCES users(id)
                );
            """);

            // Таблица user_completed_tasks
            statement.execute("""
                CREATE TABLE user_completed_tasks (
                    user_id BIGINT NOT NULL,
                    completed_task VARCHAR(32) NOT NULL,
                    PRIMARY KEY (user_id, completed_task),
                    CONSTRAINT fk_user_tasks FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);
        }
    }
}
