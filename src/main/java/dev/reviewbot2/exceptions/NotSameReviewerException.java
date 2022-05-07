package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class NotSameReviewerException extends ReviewBotException {

    public NotSameReviewerException(MessageInfo messageInfo) {
        super(messageInfo);
    }
}
