package com.example;

import com.example.config.TelegramBotProperties;
import com.example.handler.command.CommandType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    UpdateHandler updateHandler;
    TelegramBotProperties properties;

    @Autowired
    public MyTelegramBot(@Lazy UpdateHandler updateHandler,
                         TelegramBotProperties properties) {
        super(new DefaultBotOptions(), properties.getToken());
        this.properties = properties;
        this.updateHandler = updateHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateHandler.handle(update);
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    public void sendNewEditMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNewMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(DeleteMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void initCommands() {
        List<BotCommand> commands = Arrays
                .stream(CommandType.values())
                .map(command -> new BotCommand(command.getName(), command.getDescription()))
                .toList();
        try {
            execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
