package dev.reviewbot2.processor;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class Utils {
    public static SendMessage sendMessage (String chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    public static SendMessage sendMessage (String chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage.SendMessageBuilder sendMessage = SendMessage.builder();
        sendMessage.chatId(chatId);
        sendMessage.text(text);
        sendMessage.replyMarkup(keyboard);
        return sendMessage.build();
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

    public static InlineKeyboardMarkup getKeyboard(int rows) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            keyboard.add(row);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public static InlineKeyboardButton getButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static String getFormattedTime(Instant dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
            .withLocale(Locale.ROOT)
            .withZone(ZoneId.of("Europe/Moscow"));
        return formatter.format(dateTime);
    }

    public static Long getTaskIdFromText(String text) {
        String taskId = text.split("#")[1];
        return Long.parseLong(taskId);
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
