package dev.reviewbot2.app.impl.camunda.delegates;

import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.app.impl.ts.UpdateTaskStatusTransactionScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateStatusDelegate implements JavaDelegate {
    private final ProcessAccessor processAccessor;
    private final UpdateTaskStatusTransactionScript updateTaskStatus;

    @Override
    public void execute(DelegateExecution execution) throws TelegramApiException {
        String taskUuid = processAccessor.getTaskUuid(execution);
        updateTaskStatus.execute(execution, taskUuid);
    }
}
