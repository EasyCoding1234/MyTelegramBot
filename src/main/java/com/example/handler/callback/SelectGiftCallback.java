package com.example.handler.callback;

import com.example.MyTelegramBot;
import com.example.model.GiftType;
import com.example.utils.KeyboardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectGiftCallback implements Callback {

    private final MyTelegramBot bot;

    @Override
    public void apply(Update update) {
        var callbackQuery = update.getCallbackQuery();
        var chatId = callbackQuery.getMessage().getChatId();
        var userId = callbackQuery.getFrom().getId();
        var userName = callbackQuery.getFrom().getUserName();
        log.info("[{}] Коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

        String data = callbackQuery.getData(); // SELECT_GIFT:Мишка:15

        try {
            String[] parts = data.split(":");
            if (parts.length < 3) {
                sendError(chatId, "Ошибка при выборе подарка. Попробуйте ещё раз.");
                return;
            }

            String giftTitle = parts[1];
            int giftCost = Integer.parseInt(parts[2]);

            GiftType gift = GiftType.fromName(giftTitle);

            // Проверка, что стоимость совпадает с актуальной
            if (gift.getCost() != giftCost) {
                sendError(chatId, "Incorrect gift value. Try again.");
                return;
            }

            // Отправка сообщения с кнопкой подтверждения
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Confirm gift purchase  \n" +
                            "Gift: " + gift.getTitle() +
                            "\nPrice: " + giftCost + "⭐️")
                    .replyMarkup(KeyboardUtils.createConfirmWithdraw(gift))
                    .build();

            bot.sendNewMessage(message);

        } catch (Exception e) {
            log.error("Ошибка при выборе подарка", e);
            sendError(chatId, "There was an error in processing the gift.");
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
        return CallbackType.SELECT_GIFT;
    }
}
