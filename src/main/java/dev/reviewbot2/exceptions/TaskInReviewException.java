package dev.reviewbot2.exceptions;

import dev.reviewbot2.domain.MessageInfo;

public class TaskInReviewException extends ReviewBotException {

    public TaskInReviewException(MessageInfo messageInfo ) {
        super(messageInfo);
    }
}
