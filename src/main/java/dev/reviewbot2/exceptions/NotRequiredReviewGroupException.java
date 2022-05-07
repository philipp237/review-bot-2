package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class NotRequiredReviewGroupException extends ReviewBotException {

    public NotRequiredReviewGroupException(MessageInfo messageInfo) {
        super(messageInfo);
    }
}
