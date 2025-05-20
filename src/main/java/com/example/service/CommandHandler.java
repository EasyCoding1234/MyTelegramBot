package com.example.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {

    void handle(Update update);

}