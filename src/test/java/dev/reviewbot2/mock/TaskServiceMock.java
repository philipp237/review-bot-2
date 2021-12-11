package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class TaskServiceMock {
    private final TaskService taskService;

    public void mockSave(Task task) {
        doReturn(task).when(taskService).save(any());
    }
}