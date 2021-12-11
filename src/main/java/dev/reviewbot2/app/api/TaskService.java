package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.task.Task;

public interface TaskService {

    /**
     * Сохранить задачу
     *
     * @param task - задача
     * @return задача
     */
    Task save(Task task);
}
