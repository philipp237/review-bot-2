package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;

import java.util.List;

public interface TaskService {

    /**
     * Сохранить задачу
     *
     * @param task задача
     * @return задача
     */
    Task save(Task task);

    /**
     * Найти задачу по ее uuid
     *
     * @param uuid uuid задачи
     * @return задача с заданным uuid
     */
    Task getTaskByUuid(String uuid);

    /**
     * Найти задачу по ее id
     *
     * @param id идентификатор задачи
     * @return задача с заданным id
     */
    Task getTaskById(Long id);

    /**
     * Получить список задач в ревью у пользователя
     *
     * @param member пользователь
     * @return список задач, для которых пользователь является ревьюером
     */
    List<Task> getTaskInMemberReview(Member member);

    /**
     * Получить список незакрытых задач за авторством пользователя
     *
     * @param author пользователь - автор задач
     * @return список незакрытых задач за авторством пользователя
     */
    List<Task> getMemberTasks(Member author);

    /**
     * Получить все закрытые задачи
     *
     * @return список всех закрытых задач
     */
    List<Task> getClosedTasks();
}
