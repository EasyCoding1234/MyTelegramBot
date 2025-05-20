package com.example.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_telegram_id", columnList = "telegramId", unique = true),
        @Index(name = "idx_user_referral_code", columnList = "referral_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false, unique = true)
    private Long telegramId;

    @Size(max = 32)
    private String username;

    @Size(max = 64)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 64)
    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false, columnDefinition = "int default 0")
    @Max(value = 2000, message = "Stars balance cannot exceed 2000")
    private int stars = 0;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "referral_code", unique = true, length = 8, updatable = false)
    private String referralCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_referer_id")
    private User fatherReferer;

    @OneToMany(mappedBy = "invitedBy", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> invitedUsers = new ArrayList<>();

    @Column(name = "count_invited_users", columnDefinition = "int default 0")
    private int countInvitedUsers = 0;

    @Column(name = "referral_reward_given", columnDefinition = "boolean default false")
    private boolean referralRewardGiven = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_completed_tasks",
            joinColumns = @JoinColumn(name = "user_id"),
            indexes = @Index(name = "idx_completed_tasks_user", columnList = "user_id")
    )
    @Column(name = "completed_task", length = 32)
    @Builder.Default
    private Set<String> completedTasks = new HashSet<>();

    @Version
    private Integer version;

}
