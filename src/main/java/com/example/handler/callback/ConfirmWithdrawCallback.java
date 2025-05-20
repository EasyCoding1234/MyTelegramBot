package com.example.handler.callback;

import com.example.MyTelegramBot;
import com.example.model.GiftType;
import com.example.service.UserService;
import com.example.utils.KeyboardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmWithdrawCallback implements Callback {

    private final UserService userService;
    private final MyTelegramBot bot;

    @Override
    public void apply(Update update) {
        var callbackQuery = update.getCallbackQuery();
        var chatId = callbackQuery.getMessage().getChatId();
        var userId = callbackQuery.getFrom().getId();
        var userName = callbackQuery.getFrom().getUserName();
        var user = userService.getUserByTelegramId(userId);
        String data = callbackQuery.getData(); // CONFIRM_WITHDRAW:Мишка:15
        log.info("[{}] Коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);


        try {
            String[] parts = data.split(":");
            if (parts.length < 3) {
                sendError(chatId, "Confirmation error. Try again.");
                log.error("[{}] Ошибка подтверждения {} [id={}]", update.getUpdateId(), userName, userId);

                return;
            }

            String giftTitle = parts[1];
            int giftCost = Integer.parseInt(parts[2]);
            GiftType gift = GiftType.fromName(giftTitle);

            if (gift.getCost() != giftCost) {
                sendError(chatId, "Incorrect gift value. Try again.");
                log.error("[{}] Неправильная стоимость подарка {} [id={}]", update.getUpdateId(), userName, userId);

                return;
            }

            if (userService.tryWithdrawGift(userId, gift)) {
                log.info("[{}] Пользователь {} запросил подарок [id={}]", update.getUpdateId(),userName, userId);


                // Сообщение в канал о новом запросе
                bot.sendNewMessage(SendMessage.builder()
                        .chatId("-1002670885996") // канал
                        .text("New gift request!\n" +
                                "User: @" + user.getUsername() + " (ID: " + user.getTelegramId() + ")\n" +
                                "Gift: " + gift.getTitle() + " " + giftCost + "⭐️\n" +
                                "Date: " + LocalDate.now())
                        .replyMarkup(KeyboardUtils.createGiftDoneKeyboard(user))
                        .build());

                // Подтверждение пользователю
                bot.sendNewMessage(SendMessage.builder()
                        .chatId(chatId.toString())
                        .text("Your gift request has been successfully submitted!\n" +
                                "Follow the status in the channel: https://t.me/+fKlxze3mN78zZjgy")
                        .build());
            } else {
                sendError(chatId, "Not enough stars for a gift: " + gift.getTitle());
            }

        } catch (Exception e) {
            log.error("Ошибка при подтверждении подарка", e);
            sendError(chatId, "There was an error processing the gift.");
        }
    }

    private void sendError(Long chatId, String text) {
        bot.sendNewMessage(SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build());
    }

    @Override
    public CallbackType getType() {
        return CallbackType.CONFIRM_WITHDRAW;
    }
}
