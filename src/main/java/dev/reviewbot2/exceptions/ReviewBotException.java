package dev.reviewbot2.exceptions;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
public class ReviewBotException extends RuntimeException {
    protected final Update update;

    public ReviewBotException(Update update) {
        super();
        this.update = update;
    }
}
