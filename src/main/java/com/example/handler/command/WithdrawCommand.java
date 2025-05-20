package com.example.handler.command;

import com.example.MyTelegramBot;
import com.example.model.User;
import com.example.service.UserService;
import com.example.utils.KeyboardUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WithdrawCommand implements Command {

    private final MyTelegramBot bot;
    private final UserService userService;

    @Override
    public void apply(Update update) {
        long chatId = update.getMessage().getChatId();
        var userName = update.getMessage().getFrom().getUserName();
        long userId = update.getMessage().getFrom().getId();
        log.info("[{}] Коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

        User user  = userService.getUserByTelegramId(userId);
        int stars = user.getStars();


        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("⭐️Your stars: " + stars + "\nSelect gift:")
                .replyMarkup(KeyboardUtils.buildGiftSelectionKeyboard())
                .build();

        try {
            bot.sendNewMessage(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке клавиатуры подарков", e);
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.WITHDRAW;
    }
}
