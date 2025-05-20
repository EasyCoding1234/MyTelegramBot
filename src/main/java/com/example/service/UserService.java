package com.example.service;

import com.example.config.RewardConfig;
import com.example.exception.UserNotFoundException;
import com.example.model.GiftType;
import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    // Конфигурация наград из application.yml
    private final RewardConfig rewardConfig;
    // Репозиторий для работы с базой данных
    private final UserRepository userRepository;

    /**
     * Регистрация нового пользователя без реферальной ссылки
     * @param telegramId - ID пользователя в Telegram
     * @param username - имя пользователя
     * @param firstName - имя
     * @param lastName - фамилия
     */
    @Transactional
    public void registerUser(long telegramId, String username, String firstName, String lastName) {
        // Проверяем, не зарегистрирован ли пользователь
        if (userRepository.findByTelegramId(telegramId).isPresent()) {
            return;
        }

        // Создаем нового пользователя через билдер
        User user = User.builder()
                .telegramId(telegramId)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .registeredAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    /**
     * Регистрация с реферальной ссылкой
     * @param telegramId - ID пользователя в Telegram
     * @param username - имя пользователя
     * @param firstName - имя
     * @param lastName - фамилия
     * @param referralCode - реферальный код
     */
    @Transactional
    public void registerWithReferral(long telegramId, String username, String firstName,
                                     String lastName, String referralCode) {
        Optional<User> referrer = userRepository.findByReferralCode(referralCode);

        //Защита от своего же реферального кода
        if (referrer.isEmpty()) {
            throw new IllegalArgumentException("Пользователь с таким реферальным кодом не найден");
        }

        if (referrer.get().getTelegramId().equals(telegramId)) {
            throw new IllegalArgumentException("Пользователь не может пригласить сам себя");
        }

        // Проверка существующего пользователя
        if (userRepository.findByTelegramId(telegramId).isPresent()) {
            return;
        }

        // Создание объекта пользователя
        User newUser = User.builder()
                .telegramId(telegramId)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .registeredAt(LocalDateTime.now())
                .build();

        // Обработка реферальной ссылки
        processReferralLink(newUser, referralCode);
        userRepository.save(newUser);
    }

    /**
     * Обработка реферальной ссылки при регистрации
     * @param newUser - новый пользователь
     * @param referralCode - реферальный код
     */
    private void processReferralLink(User newUser, String referralCode) {
        // Пропускаем если код не указан
        if (referralCode == null || referralCode.isEmpty()) {
            return;
        }

        // Находим пользователя по реферальному коду и связываем их
        // Устанавливаем реферера
        userRepository.findByReferralCode(referralCode).ifPresent(newUser::setFatherReferer);
    }

    /**
     * Проверка выполнения задания пользователем
     * @param userId - ID пользователя
     * @param taskCode - код задания
     * @return true если задание выполнено
     */
    @Transactional(readOnly = true)
    public boolean isTaskCompleted(Long userId, String taskCode) {
        return userRepository.findByTelegramId(userId)
                .map(user -> user.getCompletedTasks().contains(taskCode))
                .orElse(false);
    }

    /**
     * Отметка задания как выполненного
     * @param userId - ID пользователя
     * @param taskCode - код задания
     */
    @Transactional
    public void completeTask(Long userId, String taskCode) {
        User user = getUserById(userId);
        user.getCompletedTasks().add(taskCode); // Добавляем задание в список выполненных
        userRepository.save(user);
    }

    /**
     * Начисление звезд пользователю
     * @param userId - ID пользователя
     * @param stars - количество звезд для начисления
     */
    @Transactional
    public void addStars(Long userId, int stars) {
        validateStarsAmount(stars); // Проверка лимитов
        userRepository.addStars(userId, stars); // Обновление в БД
        log.debug("Added {} stars to user {}", stars, userId);// Логирование
    }

    /**
     * Валидация количества звезд
     * @param stars - количество звезд
     * @throws IllegalStateException если превышен дневной лимит
     */
    private void validateStarsAmount(int stars) {
        if (stars > rewardConfig.getDaily().getLimit()) {
            throw new IllegalStateException("Дневной лимит пополнения звёзд превышен");
        }
    }


    // Выдаём награды за приглашённых
    @Scheduled(cron = "${reward.referral.schedule:0 0 12 * * ?}")
    @Transactional
    public void processReferralRewards() {
        int minStarsRequired = rewardConfig.getMinStars().getMinStarsForReward();

        // Находим только тех, кто:
        // - набрал нужное количество звёзд
        // - ещё не получил награду (referralRewardGiven = false)
        // - был зарегистрирован по реферальной ссылке (fatherReferer != null)
        // - invitedBy всё ещё null (то есть награда не выдавалась)
        List<User> eligibleReferrals = userRepository.findEligibleReferralsWithNullInvitedBy(minStarsRequired);

        if (eligibleReferrals.isEmpty()) {
            log.info("Нет подходящих рефералов для начисления бонусов");
            return;
        }

        // Группируем по пригласившему
        Map<User, List<User>> referralsByReferrer = eligibleReferrals.stream()
                .filter(user -> user.getFatherReferer() != null)
                .collect(Collectors.groupingBy(User::getFatherReferer));

        int totalProcessed = 0;

        for (Map.Entry<User, List<User>> entry : referralsByReferrer.entrySet()) {
            User referrer = entry.getKey();
            List<User> referrals = entry.getValue();

            try {
                int bonus = rewardConfig.getReferral().getBonus();
                int totalBonus = bonus * referrals.size();

                for (User referral : referrals) {
                    referral.setInvitedBy(referrer);
                    referral.setReferralRewardGiven(true);
                    userRepository.save(referral);
                }

                userRepository.addStars(referrer.getId(), totalBonus);
                userRepository.incrementReferralCount(referrer.getId());
                totalProcessed += referrals.size();

                log.info("Начислено {} звёзд пользователю {} за {} верифицированных рефералов",
                        totalBonus, referrer.getId(), referrals.size());

            } catch (Exception e) {
                log.error("Ошибка начисления бонуса для пользователя {}: {}", referrer.getId(), e.getMessage());
            }
        }

        log.info("Итого обработано верифицированных рефералов: {}", totalProcessed);
    }

    /**
     * Получение пользователя по ID
     * @param userId - ID пользователя в базе данных
     * @return сущность User
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public boolean existsByTelegramId(Long telegramId){
        return userRepository.existsByTelegramId(telegramId);
    }

    /**
     * Получение пользователя по Telegram ID
     * @param telegramId - ID пользователя в Telegram
     * @return сущность User
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException(telegramId));
    }

    /**
     * Генерация гарантированно уникального реферального кода
     */
    private String generateUniqueReferralCode() {
        String code;
        int attempts = 0;
        do {
            if (attempts++ > 5) {
                throw new IllegalStateException("Не удалось сгенерировать уникальный реферальный код");
            }
            code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (userRepository.existsByReferralCode(code));

        return code;
    }

    /**
     * Гарантирует наличие реферального кода у пользователя
     */
    @Transactional
    public String ensureReferralCodeExists(User user) {
        if (user.getReferralCode() == null || user.getReferralCode().isEmpty()) {
            String newCode = generateUniqueReferralCode();
            user.setReferralCode(newCode);
            userRepository.save(user);
            log.info("Сгенерирован новый реферальный код {} для пользователя {}", newCode, user.getId());
        }
        return user.getReferralCode();
    }

    /**
     * Публичный метод для получения реферального кода
     */
    @Transactional
    public String getOrCreateReferralCode(Long userId) {
        User user = getUserById(userId);
        return ensureReferralCodeExists(user);
    }

    /**
     * Можно ли отправить подарок пользователю
     * @param telegramId Пользователь которому мы отправляем подарок
     * @param gift Что за подарок
     * @return Можно ли отправить подарок
     */
    public boolean tryWithdrawGift(Long telegramId, GiftType gift) {
        Optional<User> optionalUser = userRepository.findByTelegramId(telegramId);
        if (optionalUser.isEmpty()){
            return false;
        }

        User user = optionalUser.get();
        if (user.getStars() < gift.getCost()) return false;

        user.setStars(user.getStars() - gift.getCost());
        userRepository.save(user);
        return true;
    }
}
