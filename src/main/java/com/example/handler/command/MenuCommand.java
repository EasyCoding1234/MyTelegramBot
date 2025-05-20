package com.example.handler.command;

import com.example.MyTelegramBot;
import com.example.utils.KeyboardUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuCommand implements Command {

    MyTelegramBot bot;

    @Override
    public void apply(Update update) {
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        log.info("[{}] Команда {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

        var chatId = update.getMessage().getChatId();

        var deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(update.getMessage().getMessageId())
                .build();
        bot.deleteMessage(deleteMessage);

        var menuMessage = SendMessage.builder()
                .chatId(chatId)
                .text("Menu 👇")
                .replyMarkup(KeyboardUtils.buildMenuInlineKeyboard())
                .build();

        bot.sendNewMessage(menuMessage);
    }

    @Override
    public CommandType getType() {
        return CommandType.MENU;
    }
}


