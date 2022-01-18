package dev.reviewbot2.app.impl.camunda;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessAccessor {
    private final String PROCESS_ID = "review-process";
    private final String TAKE_IN_REVIEW_MESSAGE = "take-in-review-message";
    private final String COMPLETE_REVIEW_MESSAGE = "complete-review-message";
    private final String SUBMIT_FOR_REVIEW_MESSAGE = "submit-for-review-message";
    private final String VAR_TASK_UUID = "taskUuid";
    private final String VAR_NEEDS_REWORK = "needsRework";
    private final String VAR_LAST_STAGE = "lastStage";

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public void startProcess(String uuid) {
        runtimeService.startProcessInstanceByKey(PROCESS_ID, Map.of(
            VAR_TASK_UUID, uuid,
            VAR_NEEDS_REWORK, "",
            VAR_LAST_STAGE, ""
        ));
    }

    public String getTaskUuid(DelegateExecution execution) {
        return execution.getVariable(VAR_TASK_UUID).toString();
    }

    public void takeInReview(String taskUuid) {
        correlateProcess(runtimeService.createMessageCorrelation(TAKE_IN_REVIEW_MESSAGE)
            .processInstanceVariableEquals(VAR_TASK_UUID, taskUuid));
    }

    public void completeReview(String taskUuid, boolean isApproved) {
        correlateProcess(runtimeService.createMessageCorrelation(COMPLETE_REVIEW_MESSAGE)
            .processInstanceVariableEquals(VAR_TASK_UUID, taskUuid)
            .setVariable(VAR_NEEDS_REWORK, !isApproved));
    }

    public void submitForReview(String taskUuid) {
        correlateProcess(runtimeService.createMessageCorrelation(SUBMIT_FOR_REVIEW_MESSAGE)
            .processInstanceVariableEquals(VAR_TASK_UUID, taskUuid));
    }

    public void checkReviewStage(String executionId, boolean isLastStage) {
        runtimeService.setVariable(executionId, VAR_LAST_STAGE, isLastStage);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private Execution correlateProcess(MessageCorrelationBuilder correlation) {
        return correlation.correlateWithResult().getExecution();
    }
}
