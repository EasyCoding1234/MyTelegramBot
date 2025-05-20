package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 🔍 Основные методы поиска
    Optional<User> findByTelegramId(Long telegramId);
    Optional<User> findByReferralCode(String referralCode);

    // 📊 Методы для реферальной системы
    @Query("SELECT u FROM User u WHERE " +
            "u.stars >= :minStars AND " +
            "u.referralRewardGiven = false AND " +
            "u.invitedBy IS NULL AND " +
            "u.fatherReferer IS NOT NULL")
    List<User> findEligibleReferralsWithNullInvitedBy(@Param("minStars") int minStars);

    boolean existsByReferralCode(String referralCode);

    // ⚡ Оптимизированные update-запросы
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User u SET u.countInvitedUsers = u.countInvitedUsers + 1 WHERE u.id = :userId")
    void incrementReferralCount(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE User u SET u.stars = u.stars + :stars WHERE u.id = :userId")
    void addStars(@Param("userId") Long userId, @Param("stars") int stars);

    boolean existsByTelegramId(Long telegramId);

    // 🎯 Дополнительные методы для бизнес-логики
    @Query("SELECT u FROM User u JOIN FETCH u.invitedBy WHERE u.telegramId = :telegramId")
    Optional<User> findByTelegramIdWithReferrer(@Param("telegramId") Long telegramId);

    @Query("SELECT u.referralCode FROM User u WHERE u.telegramId = :telegramId")
    Optional<String> findReferralCodeByTelegramId(@Param("telegramId") Long telegramId);
}

