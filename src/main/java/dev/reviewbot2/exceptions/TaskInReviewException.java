package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class TaskInReviewException extends ReviewBotException {

    public TaskInReviewException(Update update) {
        super(update);
    }
}
