package dev.reviewbot2.app.impl.camunda;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessAccessor {
    private final String PROCESS_ID = "review-process";
    private final String TAKE_ON_REVIEW_MESSAGE = "take-on-review-message";

    private final RuntimeService runtimeService;

    public void startProcess(String uuid) {
        runtimeService.startProcessInstanceByKey(PROCESS_ID, uuid);
    }

    public String getTaskUuid(DelegateExecution execution) {
        return execution.getBusinessKey();
    }

    public void takeOnReview(String taskUuid) {
        correlateProcess(runtimeService.createMessageCorrelation(TAKE_ON_REVIEW_MESSAGE)
            .processInstanceBusinessKey(taskUuid));
    }

    private Execution correlateProcess(MessageCorrelationBuilder correlation) {
        return correlation.correlateWithResult().getExecution();
    }
}
