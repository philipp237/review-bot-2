package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class TaskServiceMock {
    private final TaskService taskService;

    public void mockSave(Task task) {
        doReturn(task).when(taskService).save(any());
    }

    public void mockGetTaskById(Task task) {
        doReturn(task).when(taskService).getTaskById(anyLong());
    }

    public void mockGetMemberTasks(List<Task> tasks) {
        doReturn(tasks).when(taskService).getMemberTasks(any());
    }

    public void mockGetClosedTasks(List<Task> tasks) {
        doReturn(tasks).when(taskService).getClosedTasks();
    }
}