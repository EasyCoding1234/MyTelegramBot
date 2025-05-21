package com.example.handler.callback.tasks;

import com.example.MyTelegramBot;
import com.example.handler.callback.Callback;
import com.example.handler.callback.CallbackType;
import com.example.model.Task;
import com.example.utils.KeyboardUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j // Автоматически добавляет логгер (log) в класс с помощью Lombok
@Component
public class Task1 extends Task implements Callback {

    private final MyTelegramBot bot;

    // Конструктор, в котором задаются параметры задания через super():
    public Task1(MyTelegramBot bot){
        super("task1",
            "Task 1",
                CallbackType.TASK1,
            """
                    Yandex Go is a service for ordering rides that operates in more than 10 countries around the world.
                    
                    The application is available in AppStore and Google Play.
                    
                    You can also make an order in the web interface of Yandex Go.
                    
                    Place orders using this link and you will receive a bonus in the form of stars⭐️
                    
                    Countries: Armenia, Belarus, Georgia, Kirgizia, Kazakhstan,
                    Russia, Uzbekistan, Serbia.
                    
                    Purpose: Install the app and book your first trip
                    
                    Reward: 60⭐️
            """,
                "https://whjre.com/v2/click-B6w0w-W6yPaD-PDp7y-0dba325c?tl=1&erid=LdtCKK463",
                20,
                true);
        this.bot = bot;
    }

    // Метод вызывается при нажатии пользователем на кнопку задания
    @Override
    public void apply(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId(); // Получаем ID чата для отправки сообщения
        String userName = update.getCallbackQuery().getFrom().getUserName(); // Имя пользователя
        long userId = update.getCallbackQuery().getFrom().getId(); // Telegram ID пользователя
        // Логируем вызов
        log.info("[{}] Коллбэк {} от пользователя {} [id={}]",
                update.getUpdateId(), getType(), userName, userId);

        String link = String.format(getLink() + "?xid=%d_%s", userId, getTaskCode());

        // Формируем сообщение с описанием задания и кнопкой "Проверить"
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(getDescription() + "\n" + link) // Например: "Подпишись на канал"
                .replyMarkup(KeyboardUtils.buildCheckButton(getTaskCode())) // Кнопка: "Проверить"
                .build();

        // Отправляем сообщение через нашего Telegram-бота
        bot.sendNewMessage(message);
    }

    // Возвращает уникальный код задания (используется для кнопок и проверки)
    @Override
    public String getTaskCode() {
        return "task1";
    }

    // Возвращает тип коллбэка (чтобы определить, какой обработчик запускать)
    @Override
    public CallbackType getType() {return getCallbackType();}
}
