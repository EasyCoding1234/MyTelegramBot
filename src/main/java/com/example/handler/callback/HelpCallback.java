package com.example.handler.callback;

import com.example.MyTelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelpCallback implements Callback {

    private final MyTelegramBot bot;

    @Override
    public void apply(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String userName = update.getCallbackQuery().getFrom().getUserName();
        long userId = update.getCallbackQuery().getFrom().getId();
        log.info("[{}] Коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("""
                        If you have any questions or need help,
                        email us - we'll be sure to help!
                        
                        If you have any questions about cooperation, please contact us there.
                        
                        @poddergka
                        """)
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.HELP;
    }
}
