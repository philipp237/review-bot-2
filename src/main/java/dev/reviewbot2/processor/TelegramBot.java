package dev.reviewbot2.processor;

import dev.reviewbot2.config.Config;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@AllArgsConstructor
public class TelegramBot extends TelegramWebhookBot {
    private final Config config;
    private final MessageProcessor messageProcessor;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return messageProcessor.processMessage(update);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
            return null;
        }
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
