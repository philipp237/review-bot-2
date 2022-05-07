package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class NotDesignReviewerException extends ReviewBotException {

    public NotDesignReviewerException(MessageInfo messageInfo) {
        super(messageInfo);
    }
}
