package com.example.handler.command;

import com.example.MyTelegramBot;
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
public class InstructionCommand implements Command {

    private final MyTelegramBot bot;

    @Override
    public void apply(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getUserName();
        long userId = update.getMessage().getFrom().getId();
        log.info("[{}] Коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("""
                        📖 Instructions for using the bot:
                        
                        1. Click ‘⭐️ Earn Stars’.
                        2. Subscribe to the available channels.
                        3. Pass the test, and get stars
                        4. Keep track of your balance and withdraw stars whenever you want 💸
                        5. Wait for your gift to be approved (usually no more than two days).
                        
                        ⭐️Earn Stars - Click to earn stars for completing tasks.
                        
                        
                        🎁Get Stars - Apply to withdraw stars.
                        
                        
                        📢Referrals - Get your referral link and earn stars for inviting friends.
                        
                        
                        ⚒Help - if you have any questions, email us😊
                        """)
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public CommandType getType() {
        return CommandType.INSTRUCTION;
    }
}
