package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Instant;

import static dev.reviewbot2.domain.task.TaskStatus.CLOSED;
import static dev.reviewbot2.domain.task.TaskStatus.getTaskStatusFromActivityId;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateTaskStatusTransactionScript {
    private final TaskService taskService;

    @Transactional
    public void execute(DelegateExecution execution, String taskUuid) {
        Task task = taskService.getTaskByUuid(taskUuid);

        TaskStatus status = getTaskStatusFromActivityId(execution.getCurrentActivityId());
        if (CLOSED.equals(status)) {
            task.setCloseTime(Instant.now());
        }
        task.setStatus(status);
        taskService.save(task);
        log.info("Task with uuid={} was changed status to {}", taskUuid, status);
    }
}
