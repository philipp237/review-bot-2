package dev.reviewbot2.app.impl.camunda.delegates;

import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;

@Component
@RequiredArgsConstructor
public class DesignCheckDelegate implements JavaDelegate {
    private final TaskService taskService;
    private final ProcessAccessor processAccessor;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {
        String taskUuid = processAccessor.getTaskUuid(execution);
        Task task = taskService.getTaskByUuid(taskUuid);
        if (DESIGN.equals(task.getTaskType())) {
            processAccessor.setDesignTaskType(execution.getId());
        }
    }
}
