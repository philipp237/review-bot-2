package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;

@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Override
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task getTaskByUuid(String uuid) {
        return taskRepository.getByUuid(uuid);
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.getById(id);
    }

    @Override
    public List<Task> getTaskInMemberReview(Member member) {
        return taskRepository.getTaskInMemberReview(member.getId(), member.getReviewGroup());
    }

    @Override
    public List<Task> getMemberTasks(Member author) {
        return taskRepository.getTaskByAuthorAndStatusNotIn(author, TaskStatus.getClosedStatuses());
    }

    @Override
    public List<Task> getClosedTasks() {
        return taskRepository.getTaskByStatusInAndTaskType(TaskStatus.getStatusesReadyForIncorporation(), IMPLEMENTATION);
    }
}
