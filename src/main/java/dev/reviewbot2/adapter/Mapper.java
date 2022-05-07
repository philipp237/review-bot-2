package dev.reviewbot2.adapter;

import dev.reviewbot2.domain.MessageInfo;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Mapper {

    public MessageInfo mapToMessageInfo(Update update) {
        Message message = getMessageFromUpdate(update);

        return MessageInfo.builder()
            .messageId(getMessageId(message))
            .chatId(getChatId(message))
            .text(getTextFromUpdate(update))
            .login(getLogin(message))
            .hasCallbackQuery(checkCallbackQuery(update))
            .build();
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private Message getMessageFromUpdate(Update update) {
        if (checkCallbackQuery(update)) {
            return update.getCallbackQuery().getMessage();
        } else if (update.hasMessage()) {
            return update.getMessage();
        } else {
            throw new IllegalArgumentException("Update has no message");
        }
    }

    private Boolean checkCallbackQuery(Update update) {
        return update.hasCallbackQuery();
    }

    private Integer getMessageId(Message message) {
        return message.getMessageId();
    }

    private String getChatId(Message message) {
        return message.getChatId().toString();
    }

    private String getTextFromUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        }
        return getMessageFromUpdate(update).getText();
    }

    private String getLogin(Message message) {
        return message.getFrom().getUserName();
    }
}
