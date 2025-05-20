package com.example.utils;

import com.example.handler.callback.CallbackType;
import com.example.model.GiftType;
import com.example.model.Task;
import com.example.model.User;
import com.example.service.TaskRegistry;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class KeyboardUtils {

    public static ReplyKeyboardMarkup buildMainReplyKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(KeyboardButton.builder().text("⭐ Зработать звёзды").build());
        row1.add(KeyboardButton.builder().text("\uD83D\uDCD8 Инструкция").build());
        row1.add(KeyboardButton.builder().text("\uD83C\uDF81 Вывести звёзды").build());

        return ReplyKeyboardMarkup.builder()
                .keyboardRow(row1)
                .resizeKeyboard(true)
                .selective(false)
                .isPersistent(true)
                .build();

    }

    public static InlineKeyboardMarkup buildMenuInlineKeyboard() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text(CallbackType.EARN.getButtonText())
                .callbackData(CallbackType.EARN.toString())
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text(CallbackType.INSTRUCTION.getButtonText())
                .callbackData(CallbackType.INSTRUCTION.toString())
                .build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder()
                .text(CallbackType.WITHDRAW.getButtonText())
                .callbackData(CallbackType.WITHDRAW.toString())
                .build());


        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(InlineKeyboardButton.builder()
                .text(CallbackType.REFERRAL.getButtonText())
                .callbackData(CallbackType.REFERRAL.toString())
                .build());

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(InlineKeyboardButton.builder()
                .text(CallbackType.HELP.getButtonText())
                .callbackData(CallbackType.HELP.toString())
                .build());

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3, row4, row5))
                .build();
    }

    public static InlineKeyboardMarkup getTasksBoardKeyboard(int page, User user, TaskRegistry taskRegistry) {
        int tasksPerPage = 5;

        List<Task> allTasks = taskRegistry.getActiveTasks().stream()
                .filter(task -> !user.getCompletedTasks().contains(task.getCode()))
                .toList();

        int totalTasks = allTasks.size();
        int totalPages = (int) Math.ceil((double) totalTasks / tasksPerPage);

        int fromIndex = (page - 1) * tasksPerPage;
        int toIndex = Math.min(fromIndex + tasksPerPage, totalTasks);

        List<Task> pageTasks = allTasks.subList(fromIndex, toIndex);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Task task : pageTasks) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(task.getButtonText()) // Или task.getButtonText(), если есть
                    .callbackData(task.getCallbackType().toString()) // Или свой метод
                    .build());
            rows.add(row);
        }

        // Навигация по страницам
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        if (page > 1) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("⬅️ Предыдущая страница")
                    .callbackData("TASKS_PAGE_" + (page - 1))
                    .build());
        }
        if (page < totalPages) {
            navigationRow.add(InlineKeyboardButton.builder()
                    .text("➡️ Следующая страница")
                    .callbackData("TASKS_PAGE_" + (page + 1))
                    .build());
        }
        if (!navigationRow.isEmpty()) {
            rows.add(navigationRow);
        }

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    //Кнопка проверки заданий
    public static InlineKeyboardMarkup buildCheckButton(String taskCode ) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> task1 = new ArrayList<>();
        task1.add(InlineKeyboardButton.builder()
                .text(CallbackType.CHECK.getButtonText())
                .callbackData("CHECK:" + taskCode)
                .build());

        rows.add(task1);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }

    /**
     * Клавиатура со списком подарков
     */
    public InlineKeyboardMarkup buildGiftSelectionKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(createGiftButton(GiftType.BEAR)));
        rows.add(List.of(createGiftButton(GiftType.HEART)));
        rows.add(List.of(createGiftButton(GiftType.ROSE)));
        rows.add(List.of(createGiftButton(GiftType.PRESENT)));
        rows.add(List.of(createGiftButton(GiftType.CAKE)));
        rows.add(List.of(createGiftButton(GiftType.FLOWERS)));
        rows.add(List.of(createGiftButton(GiftType.ROCKET)));
        rows.add(List.of(createGiftButton(GiftType.CHAMPAGNE)));
        rows.add(List.of(createGiftButton(GiftType.TROPHY)));
        rows.add(List.of(createGiftButton(GiftType.RING)));
        rows.add(List.of(createGiftButton(GiftType.DIAMOND)));
        rows.add(List.of(createGiftButton(GiftType.STARS100)));
        rows.add(List.of(createGiftButton(GiftType.STARS150)));
        rows.add(List.of(createGiftButton(GiftType.STARS250)));
        rows.add(List.of(createGiftButton(GiftType.STARS350)));
        rows.add(List.of(createGiftButton(GiftType.STARS500)));

        return new InlineKeyboardMarkup(rows);
    }

    /**
     * Создание кнопки с подарком
     */
    private InlineKeyboardButton createGiftButton(GiftType gift) {
        return InlineKeyboardButton.builder()
                .text(gift.getTitle() + " — " + gift.getCost() + "⭐️")
                .callbackData("SELECT_GIFT:" + gift.name() + ":" + gift.getCost())
                .build();
    }

    /**
     * Кнопка подтверждения подарка
     */
    public InlineKeyboardMarkup createConfirmWithdraw(GiftType gift) {
        List<InlineKeyboardButton> row = List.of(
                InlineKeyboardButton.builder()
                        .text("Подтвердить подарок")
                        .callbackData("CONFIRM_WITHDRAW:" + gift.name() + ":" + gift.getCost())
                        .build()
        );
        return new InlineKeyboardMarkup(List.of(row));
    }

    /**
     * Кнопка "Не выполнено" для админа
     */
    public InlineKeyboardMarkup createGiftDoneKeyboard(User user) {
        List<InlineKeyboardButton> row = List.of(
                InlineKeyboardButton.builder()
                        .text("❌ Не выполнено")
                        .callbackData("GIFT_DONE:" + user.getTelegramId())
                        .build()
        );
        return new InlineKeyboardMarkup(List.of(row));
    }
}



