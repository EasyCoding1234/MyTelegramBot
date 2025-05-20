package com.example.handler.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {

    void apply(Update update);

    CommandType getType();
}
