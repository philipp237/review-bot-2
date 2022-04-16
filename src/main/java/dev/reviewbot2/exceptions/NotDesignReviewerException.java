package dev.reviewbot2.exceptions;

import org.telegram.telegrambots.meta.api.objects.Update;

public class NotDesignReviewerException extends ReviewBotException {

    public NotDesignReviewerException(Update update) {
        super(update);
    }
}
