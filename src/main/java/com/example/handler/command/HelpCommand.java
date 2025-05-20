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
public class HelpCommand  implements Command{

    private final MyTelegramBot bot;

    @Override
    public void apply(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getUserName();
        long userId = update.getMessage().getFrom().getId();
        log.info("[{}] Команда {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("""
                        If you have any questions or need help,
                        email us - we'll be sure to help!
                        
                        If you have any questions about cooperation, please contact us there.
                        
                        mr.earn.sars@mail.ru
                        """)
                .build();

        bot.sendNewMessage(message);
    }

    @Override
    public CommandType getType() {
        return CommandType.HELP;
    }
}
