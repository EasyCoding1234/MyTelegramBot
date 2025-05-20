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
public class InstructionCallback implements Callback {

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
                        📖 Instructions for using the bot:
                        
                        The first way is to complete tasks and get stars
                        Second way - invite new users (more details in the ‘Referrals’ section)
                        
                        The first way:
                        1. Click ‘⭐️ Earn Stars’.
                        2. Subscribe to the available channels.
                        3. Pass the test, and get stars
                        4. Keep track of your balance and withdraw stars whenever you want 💸
                        5. Wait for your gift to be approved (usually no more than two days).
                        
                        Button description:
                        ⭐️Earn Stars - Click to earn stars for completing tasks.
                        
                        
                        🎁Get Stars - Apply to withdraw stars.
                        
                        
                        📢Referrals - Get your referral link and earn stars for inviting friends.
                        
                        
                        ⚒Help - if you have any questions, email us😊
                        """)
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.INSTRUCTION;
    }
}