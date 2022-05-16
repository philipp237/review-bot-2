package dev.reviewbot2.processor;

import dev.reviewbot2.adapter.Mapper;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@AllArgsConstructor
public class TelegramBot extends TelegramWebhookBot {
    private final Config config;
    private final MessageProcessor messageProcessor;
    private final Mapper mapper;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        MessageInfo messageInfo = mapper.mapToMessageInfo(update);
        return messageProcessor.processMessage(messageInfo);
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
