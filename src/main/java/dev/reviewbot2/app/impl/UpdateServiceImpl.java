package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.webhook.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Utils.getChatId;
import static dev.reviewbot2.processor.Utils.getMessageId;

@RequiredArgsConstructor
public class UpdateServiceImpl implements UpdateService {
    private final WebhookRestClient webhookRestClient;

    @Override
    public void deletePreviousMessage(Update update) throws TelegramApiException {
        Integer messageId = getMessageId(update);
        String chatId = getChatId(update);

        DeleteMessage deleteMessage = getDeleteMessage(chatId, messageId);
        webhookRestClient.deleteMessage(deleteMessage);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private DeleteMessage getDeleteMessage(String chatId, Integer messageId) {
        DeleteMessage.DeleteMessageBuilder deleteMessageBuilder = DeleteMessage.builder();
        deleteMessageBuilder.chatId(chatId);
        deleteMessageBuilder.messageId(messageId);
        return deleteMessageBuilder.build();
    }
}
