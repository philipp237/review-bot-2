package dev.reviewbot2.domain.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тип задачи
 */
@AllArgsConstructor
@Getter
public enum TaskType {
    /**
     * Реализация
     */
    IMPLEMENTATION("Реализация"),

    /**
     * Дизайн
     */
    DESIGN("Дизайн");

    String name;
}
