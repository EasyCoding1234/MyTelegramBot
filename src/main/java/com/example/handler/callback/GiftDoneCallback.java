package com.example.handler.callback;

import com.example.MyTelegramBot;
import com.example.config.AdminConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiftDoneCallback implements Callback{

    private final MyTelegramBot bot;
    private final AdminConfig adminConfig;

    @Override
    public void apply(Update update) {
        var callbackQuery = update.getCallbackQuery();
        var message = callbackQuery.getMessage();
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var originalText = message.getText();
        log.info("[{}] Коллбэк {}, задание выполнено!", update.getUpdateId(), getType());

        Long userId = update.getCallbackQuery().getFrom().getId();

        if (!adminConfig.isAdmin(userId)) {
            log.info("[{}] Пользователь пытается одобрить отправку подарка", update.getUpdateId());
            return;
        }

        String updatedText = originalText + "\n\n✅ Статус: ВЫПОЛНЕНО";

        try {
            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(updatedText)
                    .parseMode("Markdown")
                    .build();

            bot.sendNewEditMessage(editMessage);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[{}] Ошибка при выдаче подарка {}", update.getUpdateId(), getType());
        }
    }

    @Override
    public CallbackType getType() {
        return CallbackType.GIFT_DONE;
    }
}
