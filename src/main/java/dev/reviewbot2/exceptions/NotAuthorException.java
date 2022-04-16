package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class NotAuthorException extends ReviewBotException {

    public NotAuthorException(Update update) {
        super(update);
    }
}
