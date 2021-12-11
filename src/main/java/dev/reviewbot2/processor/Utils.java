package dev.reviewbot2.processor;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Utils {
    public static SendMessage sendMessage (String chatId, String text) {
        SendMessage.SendMessageBuilder sendMessage = SendMessage.builder();
        sendMessage.chatId(chatId);
        sendMessage.text(text);
        return sendMessage.build();
    }

    public static DeleteMessage deletePreviousMessage(Update update) throws TelegramApiException {
        int messageId = getMessageId(update);
        String chatId = getChatId(update);

        DeleteMessage.DeleteMessageBuilder deleteMessage = DeleteMessage.builder();
        deleteMessage.messageId(messageId);
        deleteMessage.chatId(chatId);
        return deleteMessage.build();
    }

    public static boolean updateHasMessage(Update update) throws TelegramApiException {
        return getMessageFromUpdate(update) != null;
    }

    public static String getTextFromUpdate(Update update) throws TelegramApiException {
        return getMessageFromUpdate(update).getText();
    }

    public static String getChatId(Update update) throws TelegramApiException {
        return getMessageFromUpdate(update).getChatId().toString();
    }

    public static int getMessageId(Update update) throws TelegramApiException {
        return getMessageFromUpdate(update).getMessageId();
    }

    public static String getLoginFromUpdate(Update update) throws TelegramApiException {
        return getMessageFromUpdate(update).getFrom().getUserName();
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private static Message getMessageFromUpdate(Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage();
        } else if (update.hasMessage()) {
            return update.getMessage();
        } else {
            log.error("Update with id={} has no message", update.getUpdateId());
            throw new TelegramApiException("Update has no message");
        }
    }
}
