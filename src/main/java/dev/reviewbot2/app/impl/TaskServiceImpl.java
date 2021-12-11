package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Override
    public Task save(Task task) {
        return taskRepository.save(task);
    }
}
