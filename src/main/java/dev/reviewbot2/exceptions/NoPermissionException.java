package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class NoPermissionException extends ReviewBotException {

    public NoPermissionException(Update update) {
        super(update);
    }
}
