package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class NotRequiredTaskStatusException extends ReviewBotException {

    public NotRequiredTaskStatusException(MessageInfo messageInfo) {
        super(messageInfo);
    }
}
