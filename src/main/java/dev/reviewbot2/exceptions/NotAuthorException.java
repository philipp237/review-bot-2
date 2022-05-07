package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class NotAuthorException extends ReviewBotException {

    public NotAuthorException(MessageInfo messageInfo) {
        super(messageInfo);
    }
}
