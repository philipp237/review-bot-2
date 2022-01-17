package dev.reviewbot2.processor;

/**
 * Телеграм-команда
 */
public enum Command {
    /**
     * Основная команда. Выводит список возможных действий
     */
    START,

    /**
     * Взять задачу в ревью
     */
    TAKE_IN_REVIEW,

    /**
     * Подтвердить взятие задачи в ревью
     */
    ACCEPT_REVIEW,

    /**
     * Подтвердить задачу
     */
    APPROVE,

    /**
     * Отправить задачу на доработку
     */
    DECLINE
}
