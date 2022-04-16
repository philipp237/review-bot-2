package dev.reviewbot2.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.util.Arrays.stream;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {
    DEFAULT(Exception.class, "Что-то пошло не так"),
    NOT_DESIGN_REVIEWER(NotDesignReviewerException.class, "Ты не можешь ревьюить дизайны"),
    NOT_REQUIRED_REVIEW_GROUP(NotRequiredReviewGroupException.class, "Ты не можешь взять эту задачу в ревью, ты не в той группе ревью"),
    TASK_IN_REVIEW(TaskInReviewException.class, "Кто-то успел взять задачу на ревью раньше тебя ¯\\_(ツ)_/¯"),
    NOT_REQUIRED_TASK_STATUS(NotRequiredTaskStatusException.class, "У задачи успел поменяться статус, действие не выполнено"),
    NO_PERMISSION(NoPermissionException.class, "Нет прав"),
    NOT_AUTHOR(NotAuthorException.class, "Не ты автор этой задачи"),
    NOT_SAME_REVIEWER(NotSameReviewerException.class, "Ты не можешь завершить ревью, которое не проводил");

    private final Class<? extends Exception> exception;
    private final String message;

    public static String getByException(Class<? extends Exception> exception) {
        return stream(values())
            .filter(exceptionMessage -> exceptionMessage.getException().equals(exception))
            .findFirst()
            .orElse(DEFAULT)
            .getMessage();
    }
}
