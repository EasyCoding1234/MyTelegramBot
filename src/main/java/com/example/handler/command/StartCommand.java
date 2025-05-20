package com.example.handler.command;

import com.example.MyTelegramBot;
import com.example.service.UserService;
import com.example.utils.KeyboardUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StartCommand implements Command {

    MyTelegramBot bot;
    UserService userService;

    @Override
    public void apply(Update update) {
        try {
            Message message = update.getMessage(); // Получаем сообщение от пользователя
            Long chatId = message.getChatId(); // ID чата для ответа
            User telegramUser = message.getFrom(); // Объект пользователя Telegram
            String commandText = message.getText(); // Текст команды, например: "/start ref_123"

            logCommand(update.getUpdateId(), telegramUser); // Логируем факт вызова команды
            processUserRegistration(telegramUser, commandText); // Регистрируем пользователя или по рефералке
            deleteOriginalMessage(chatId, message.getMessageId()); // Удаляем оригинальное сообщение от пользователя
            sendWelcomeMessage(chatId); // Отправляем приветственное сообщение с кнопками

        } catch (Exception e) {
            log.error("Ошибка обработки команды /start", e); // Логируем возможные ошибки
        }
    }

    // Логирование вызова команды с инфой о пользователе
    private void logCommand(Integer updateId, User user) {
        log.info("[{}] Команда {} от {} [id={}]", updateId, getType(), user.getUserName(), user.getId());
    }

    // Регистрация пользователя с учётом реферального кода (если есть)
    private void processUserRegistration(User user, String commandText) {
        try {
            // Проверка на то есть ли пользователь в базе данных
            if (userService.existsByTelegramId(user.getId())) {
                // Пользователь найден в базе — выход
                log.info(" Пользователь {} уже зарегистрирован [id={}]", user.getUserName(), user.getId());
                return;

            }else if (commandText != null && commandText.startsWith("/start")) {
                // Разбиваем текст команды по пробелу: "/start ref_123"
                String[] parts = commandText.split(" ");
                if (parts.length > 1 && parts[1].startsWith("ref_")) {
                    String refCode = parts[1].substring(4); // Извлекаем сам реферальный код
                    userService.registerWithReferral(
                            user.getId(),
                            user.getUserName(),
                            user.getFirstName(),
                            user.getLastName(),
                            refCode
                    );
                    log.info("Пользователь {} зарегистрирован по реферальному коду {}", user.getUserName(), refCode);
                    return;
                }
            }

            // Если рефералки нет — обычная регистрация
            userService.registerUser(
                    user.getId(),
                    user.getUserName(),
                    user.getFirstName(),
                    user.getLastName()
            );
            log.info("Пользователь {} зарегистрирован без реферального кода", user.getUserName());

        } catch (Exception e) {
            log.error("Ошибка регистрации пользователя", e);
        }
    }

    // Удаление исходного сообщения пользователя (если хочет, чтобы бот "очищал" чат)
    private void deleteOriginalMessage(Long chatId, Integer messageId) {
        try {
            bot.execute(new DeleteMessage(chatId.toString(), messageId));
        } catch (TelegramApiException e) {
            log.error("Ошибка удаления сообщения", e);
        }
    }

    // Отправляем одно приветственное сообщение с клавиатурой
    private void sendWelcomeMessage(Long chatId) {
        SendMessage welcome = SendMessage.builder()
                .chatId(chatId)
                .text("""
                    Hi! 👋
                    Welcome to Earn Stars!
                    
                    Here you can complete simple tasks and earn stars ⭐️ which can be exchanged for real prizes!
                    
                    Before you start, we advise you to read the instructions👀
                    Choose an action below 👇
                    """)
                .replyMarkup(KeyboardUtils.buildMenuInlineKeyboard()) // Клавиатура
                .build();

        bot.sendNewMessage(welcome);
    }

    // Возвращает тип команды (для обработки в CommandHandler)
    @Override
    public CommandType getType() {
        return CommandType.START;
    }
}