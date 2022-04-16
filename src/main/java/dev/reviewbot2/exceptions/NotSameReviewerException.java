package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class NotSameReviewerException extends ReviewBotException {

    public NotSameReviewerException(Update update) {
        super(update);
    }
}
