package com.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * Конфигурационный класс для настроек вознаграждений.
 * Значения загружаются из application.yml с префиксом 'reward'.
 */
@Getter
@Setter
@Validated  // Включает валидацию для свойств конфигурации
@Configuration
@ConfigurationProperties(prefix = "reward") // Биндинг свойств с префиксом 'reward'
public class RewardConfig {

    /**
     * Настройки дневных лимитов
     */
    private final Daily daily = new Daily();

    /**
     * Настройки реферальной программы
     */
    private final Referral referral = new Referral();

    /**
     * Настройки минимального количества звезд
     */
    private final MinStars minStars = new MinStars();

    /**
     * Вложенный класс для дневных лимитов
     */
    @Getter
    @Setter
    public static class Daily {
        @Min(1) // Валидация - значение должно быть ≥ 1
        private int limit = 100; // Значение по умолчанию
    }

    /**
     * Вложенный класс для реферальных бонусов
     */
    @Getter
    @Setter
    public static class Referral {
        @Min(1) // Валидация - значение должно быть ≥ 1
        private int bonus = 10; // Значение по умолчанию
    }

    /**
     * Вложенный класс для минимального количества звезд
     */
    @Getter
    @Setter
    public static class MinStars {
        @Min(1) // Валидация - значение должно быть ≥ 1
        private int minStarsForReward = 20; // Значение по умолчанию
    }
}
