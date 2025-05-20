package com.example.handler.callback;

import com.example.service.CallbackHandler;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultCallbackHandler implements CallbackHandler {

    Map<CallbackType, Callback> callbacksMap;

    @Autowired
    public DefaultCallbackHandler(List<Callback> callbacks) {
        this.callbacksMap = new HashMap<>();
        callbacks.forEach(callback -> callbacksMap.put(callback.getType(), callback));
    }

    @Override
    public void handle(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        log.debug("[{}] Получен колбэк {}", update.getUpdateId(), callbackData);

        CallbackType type;

        try {
            String typeString = callbackData.split(":")[0];

            if (typeString.equals("CHECK")) {
                type = CallbackType.CHECK;
            } else if (typeString.equals("SELECT_GIFT")) {
                type = CallbackType.SELECT_GIFT;
            } else {
                type = CallbackType.valueOf(typeString);
            }

            var callback = callbacksMap.get(type);
            if (callback != null) {
                callback.apply(update);
            } else {
                long userId = update.getCallbackQuery().getFrom().getId();
                String userName = update.getCallbackQuery().getFrom().getUserName();
                log.error("[{}] Неизвестный коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), callbackData, userName, userId);
            }
        } catch (IllegalArgumentException e) {
            long userId = update.getCallbackQuery().getFrom().getId();
            String userName = update.getCallbackQuery().getFrom().getUserName();
            log.error("[{}] Неверный формат колбэка {} от пользователя {} [id={}]", update.getUpdateId(), callbackData, userName, userId);
        }
    }
}
