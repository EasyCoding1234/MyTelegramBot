package com.example.model;

import com.example.handler.callback.CallbackType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Task {

    private Long id;  // ID больше не генерируется автоматически через базу данных

    private String code; // Например, "task1", "task2"

    private String buttonText;

    private CallbackType callbackType;

    private String description;

    private String link;

    private int rewardStars;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected Task(String code, String buttonText, CallbackType callbackType, String description, String link, int rewardStars, boolean active) {
        this.code = code;
        this.buttonText = buttonText;
        this.callbackType = callbackType;
        this.description = description;
        this.link = link;
        this.rewardStars = rewardStars;
        this.active = active;
    }

    public abstract String getTaskCode();
}