package com.example.config;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminConfig {

    // Добавь сюда Telegram ID всех администраторов
    private final Set<Long> adminIds = Set.of(
            5281275821L, // Заменить на реальные ID
            7640347117L
    );

    public boolean isAdmin(Long userId) {
        return adminIds.contains(userId);
    }
}
