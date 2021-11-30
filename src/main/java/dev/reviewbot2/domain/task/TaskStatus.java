package dev.reviewbot2.domain.task;

/**
 * Статус задачи
 */
public enum TaskStatus {
    /**
     * Задача на доработке
     */
    IN_PROGRESS,

    /**
     * Задача готова к ревью
     */
    READY_FOR_REVIEW,

    /**
     * Задача в ревью
     */
    IN_REVIEW,

    /**
     * Задача одобрена
     */
    APPROVED,

    /**
     * Задача закрыта
     */
    CLOSED
}
