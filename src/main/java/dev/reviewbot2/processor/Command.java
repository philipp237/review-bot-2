package dev.reviewbot2.processor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Телеграм-команда
 */
@Getter
@AllArgsConstructor
public enum Command {
    /**
     * Основная команда. Выводит список возможных действий
     */
    START("Показать список возможных действий"),

    /**
     * Взять задачу в ревью
     */
    TAKE_IN_REVIEW("Список задач на ревью"),

    /**
     * Подтвердить взятие задачи в ревью
     */
    ACCEPT_REVIEW("Взять в ревью"),

    /**
     * Одобрить задачу
     */
    APPROVE("Одобрить"),

    /**
     * Отправить задачу на доработку
     */
    DECLINE("Отправить на доработку"),

    /**
     * Отправить задачу на ревью
     */
    SUBMIT("Отправить на ревью"),

    /**
     * Закрыть задачу
     */
    CLOSE("Закрыть"),

    /**
     * Создать задачу
     */
    CREATE_TASK("Создать задачу"),

    /**
     * Список ревью пользователя
     */
    MY_REVIEWS("Список моих ревью"),

    /**
     * Список задач пользователя
     */
    MY_TASKS("Мои активные задачи");

    private String buttonText;
}
