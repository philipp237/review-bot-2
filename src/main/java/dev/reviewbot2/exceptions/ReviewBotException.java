package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;
import lombok.Getter;

@Getter
public class ReviewBotException extends RuntimeException {
    protected final MessageInfo messageInfo;

    public ReviewBotException(MessageInfo messageInfo) {
        super();
        this.messageInfo = messageInfo;
    }
}
