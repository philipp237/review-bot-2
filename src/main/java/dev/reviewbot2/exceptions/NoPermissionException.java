package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class NoPermissionException extends ReviewBotException {

    public NoPermissionException(MessageInfo messageInfo) {
        super(messageInfo);
    }
}
