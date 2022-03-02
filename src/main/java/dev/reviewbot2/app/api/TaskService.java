package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
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

    List<Task> getTaskInMemberReview(Member member);
}
