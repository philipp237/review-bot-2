package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class NotRequiredReviewGroupException extends ReviewBotException {

    public NotRequiredReviewGroupException(Update update) {
        super(update);
    }
}
