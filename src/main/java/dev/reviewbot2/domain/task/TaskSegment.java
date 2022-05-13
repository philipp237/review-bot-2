package dev.reviewbot2.domain.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Сегмент задачи
 */
@Getter
@AllArgsConstructor
public enum TaskSegment implements Comparable<TaskSegment> {

    /**
     * Дефект
     */
    DEFECT("Дефект"),

    /**
     * Бизнес-функционал
     */
    BF("БФ");

    private final String text;
}
