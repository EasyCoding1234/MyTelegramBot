package com.example.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallbackHandler {

    void handle(Update update);
}
