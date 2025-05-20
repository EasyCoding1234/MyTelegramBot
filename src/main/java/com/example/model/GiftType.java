package com.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GiftType {
    BEAR("\uD83E\uDDF8Bear", 15),
    HEART("❤\uFE0FHeart", 15),
    ROSE("\uD83C\uDF39Rose", 25),
    PRESENT("\uD83C\uDF81Gift", 25),
    CAKE("\uD83C\uDF82Cake", 50),
    FLOWERS("\uD83D\uDC90Flowers", 50),
    ROCKET("\uD83D\uDE80Rocket", 50),
    CHAMPAGNE("\uD83C\uDF7EChampagne", 50),
    TROPHY("\uD83C\uDFC6Trophy", 100),
    RING("\uD83D\uDC8DRing", 100),
    DIAMOND("\uD83D\uDC8EDiamond", 100),
    STARS100("100 Stars⭐\uFE0F", 100),
    STARS150("150 Stars⭐\uFE0F", 150),
    STARS250("250 Stars⭐\uFE0F", 250),
    STARS350("350 Stars⭐\uFE0F", 350),
    STARS500("500 Stars⭐\uFE0F", 500),

    ;

    private final String title;
    private final int cost;

    // Находит подарок по названию и стоимости (регистронезависимо)
    public static GiftType findByTitleAndCost(String title, int cost) {
        for (GiftType gift : values()) {
            if (gift.title.equalsIgnoreCase(title) && gift.cost == cost) {
                return gift;
            }
        }
        throw new IllegalArgumentException("Gift not found: " + title + " (cost: " + cost + ")");
    }

    // Находит подарок по enum-имени (BEAR, HEART и т.д.)
    public static GiftType fromName(String name) {
        for (GiftType gift : values()) {
            if (gift.name().equalsIgnoreCase(name)) {
                return gift;
            }
        }
        throw new IllegalArgumentException("Unknown gift: " + name);
    }
}
