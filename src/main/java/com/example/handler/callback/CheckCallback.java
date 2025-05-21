package com.example.handler.callback;

import com.example.MyTelegramBot;
import com.example.model.Task;
import com.example.service.TaskRegistry;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Slf4j // Добавляет логгер (переменная log) с помощью Lombok
@Component // Делает этот класс Spring-компонентом, чтобы он автоматически подхватывался
@RequiredArgsConstructor // Автоматически создаёт конструктор с final-полями (внедрение зависимостей через Spring)
public class CheckCallback implements Callback {

    private final MyTelegramBot bot; // Telegram-бот для отправки сообщений
    private final TaskRegistry taskRegistry; // Реестр всех заданий (используется для получения награды и др.)
    private final UserService userService; // Сервис для работы с пользователями (хранение, проверка выполнения и т.д.)

    @Override
    public void apply(Update update) {
        // Получаем данные из нажатой кнопки, например: "CHECK:task1"
        String callbackData = update.getCallbackQuery().getData();

        long userId = update.getCallbackQuery().getFrom().getId();     // Telegram ID пользователя
        long chatId = update.getCallbackQuery().getMessage().getChatId(); // ID чата
        String userName = update.getCallbackQuery().getFrom().getUserName(); // username пользователя

        log.info("[{}] Коллбэк {} от пользователя {} [id={}]",
                update.getUpdateId(), getType(), userName, userId);

        // Извлекаем код задания после "CHECK:"
        String taskCode = callbackData.split(":")[1];

        // Проверяем, не выполнял ли пользователь уже это задание
        if (userService.isTaskCompleted(userId, taskCode)) {
            sendAlert(update, "Вы уже выполнили это задание!", true);
            return;
        }

        // Ссылка на Google Apps Script, который проверяет выполнение задания
        String checkUrl = "https://script.google.com/macros/s/AKfycbw5PpsEzx6qb--Kx4JLO9-NSyH39ADszyAnRYv8xa_sbwxMopHjxUPf93B96vWorMJoPA/exec"
                + "?telegramId=" + userId
                + "&taskCode=" + taskCode;

        try {
            // Создаём HTTP-клиент, который следует за редиректами (обязательно для Google Apps Script)
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(checkUrl))
                    .GET()
                    .build();

            // Выполняем запрос
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Ответ получен: {}", response);

            // Парсим JSON-ответ
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.body());

            // Проверяем флаг completed
            boolean completed = jsonNode.get("completed").asBoolean();
            log.info("[{}] {} отправил запрос на исполнение задания {}",
                    update.getUpdateId(), userName, taskCode);

            if (completed) {
                // Отмечаем выполнение задания
                userService.completeTask(userId, taskCode);

                // Получаем задание по коду
                Task task = taskRegistry.getTaskByCode(taskCode);
                if (task == null) {
                    log.error("[{}] {} задание {} не найдено {}", update.getUpdateId(), userName, taskCode, userId);
                    return;
                }

                int stars = task.getRewardStars();
                userService.addStars(userId, stars);

                sendAlert(update, "✅ Congratulations! You have completed the task and received the award - "
                        + stars + " stars!", true);
                log.info("[{}] {} выполнил задание {} {}", update.getUpdateId(), userName, taskCode, userId);

            } else {
                // Задание не выполнено
                sendAlert(update, "❌ Checking may take some time. Try again later.", true);
            }

        } catch (Exception e) {
            // Ошибка при отправке или разборе ответа
            log.error("Ошибка при проверке выполнения задания", e);
            SendMessage errorMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text("⚠️ Ошибка при проверке выполнения. Попробуйте позже.")
                    .build();
            bot.sendNewMessage(errorMessage);
        }
    }

    // Метод отправки всплывающего уведомления пользователю
    private void sendAlert(Update update, String text, boolean showAlert) {
        try {
            AnswerCallbackQuery alert = AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .text(text)
                    .showAlert(showAlert) // true — большое уведомление, false — маленькое
                    .build();
            bot.execute(alert); // Отправка через TelegramBot API
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке алерта", e);
        }
    }

    // Возвращает тип коллбэка, используется для маршрутизации
    @Override
    public CallbackType getType() {
        return CallbackType.CHECK;
    }
}
