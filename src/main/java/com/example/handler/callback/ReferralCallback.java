package com.example.handler.callback;

import com.example.MyTelegramBot;
import com.example.model.User;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReferralCallback implements Callback {

    private final MyTelegramBot bot;
    private final UserService userService;

    @Override
    public void apply(Update update) {
        try {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();
            var userName = update.getCallbackQuery().getFrom().getUserName();
            log.info("[{}] Коллбэк {} от пользователя {} [id={}]", update.getUpdateId(), getType(), userName, userId);

            User user = userService.getUserByTelegramId(userId);
            String referralLink = String.format("https://t.me/%s?start=ref_%s",
                    bot.getBotUsername(),
                    userService.getOrCreateReferralCode(user.getId()));

            bot.sendNewMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(formatReferralMessage(referralLink))
                    .build());

        } catch (Exception e) {
            log.error("Ошибка при обработке реферального запроса", e);
        }
    }


    private String formatReferralMessage(String link) {
        return String.format("""
            Invite your friends🤝 and get 10⭐
            
            The reward will be credited if the invited user has at least 20 stars, check once a day.
            
            Your referral link🔗
            %s
            """, link);
    }

    @Override
    public CallbackType getType() {
        return CallbackType.REFERRAL;
    }
}