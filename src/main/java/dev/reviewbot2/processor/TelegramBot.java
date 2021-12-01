package dev.reviewbot2.processor;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import static dev.reviewbot2.config.Config.BOT_NAME;
import static dev.reviewbot2.config.Config.BOT_TOKEN;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramWebhookBot {

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotPath() {
        return BOT_NAME;
    }
}
