package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class NotRequiredTaskStatusException extends ReviewBotException {

    public NotRequiredTaskStatusException(Update update) {
        super(update);
    }
}
