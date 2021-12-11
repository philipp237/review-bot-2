package dev.reviewbot2.processor;

import dev.reviewbot2.config.Config;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramWebhookBot {
    private final Config config;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotUsername() {
        return config.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return config.BOT_TOKEN;
    }

    @Override
    public String getBotPath() {
        return config.BOT_NAME;
    }
}
