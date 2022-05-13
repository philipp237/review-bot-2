package dev.reviewbot2.domain.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Статус задачи
 */
@AllArgsConstructor
@Getter
public enum TaskStatus {
    /**
     * Задача на доработке
     */
    IN_PROGRESS("in-progress"),

    /**
     * Задача готова к ревью
     */
    READY_FOR_REVIEW("ready-for-review"),

    /**
     * Задача в ревью
     */
    IN_REVIEW("in-review"),

    /**
     * Задача одобрена
     */
    APPROVED("approved"),

    /**
     * Задача закрыта
     */
    CLOSED("closed"),

    /**
     * Задача закрыта принудительно
     */
    FORCE_CLOSED("force-closed"),

    /**
     * Задача выведена в продакшн
     */
    IN_PRODUCTION("in-production");

    private final String activityId;

    public static TaskStatus getTaskStatusFromActivityId(String activityId) throws TelegramApiException {
        return stream(TaskStatus.values())
            .filter(taskStatus -> activityId.equals(taskStatus.getActivityId()))
            .findAny()
            .orElseThrow(TelegramApiException::new);
    }

    public static List<TaskStatus> getClosedStatuses() {
        return Stream.of(CLOSED, FORCE_CLOSED, IN_PRODUCTION).collect(toList());
    }

    public static List<TaskStatus> getStatusesReadyForIncorporation() {
        return Stream.of(CLOSED, FORCE_CLOSED).collect(toList());
    }
}
