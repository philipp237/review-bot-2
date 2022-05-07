package dev.reviewbot2.app.impl.camunda;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class ProcessAccessor {
    private final String PROCESS_ID = "review-process";
    private final String TAKE_IN_REVIEW_MESSAGE = "take-in-review-message";
    private final String COMPLETE_REVIEW_MESSAGE = "complete-review-message";
    private final String SUBMIT_FOR_REVIEW_MESSAGE = "submit-for-review-message";
    private final String CLOSE_TASK_MESSAGE = "close-task-message";
    private final String FORCE_CLOSE_TASK_MESSAGE = "force-close-task-message";
    private final String VAR_TASK_UUID = "taskUuid";
    private final String VAR_NEEDS_REWORK = "needsRework";
    private final String VAR_LAST_STAGE = "lastStage";

    private final RuntimeService runtimeService;

    public void startProcess(String uuid) {
        runtimeService.startProcessInstanceByKey(PROCESS_ID, new HashMap<String, Object>() {{
                put(VAR_TASK_UUID, uuid);
                put(VAR_NEEDS_REWORK, "");
                put(VAR_LAST_STAGE, "");
            }}
        );
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

    public void closeTask(String taskUuid) {
        correlateProcess(runtimeService.createMessageCorrelation(CLOSE_TASK_MESSAGE)
            .processInstanceVariableEquals(VAR_TASK_UUID, taskUuid));
    }

    public void forceCloseTask(String taskUuid) {
        correlateProcess(runtimeService.createMessageCorrelation(FORCE_CLOSE_TASK_MESSAGE)
            .processInstanceVariableEquals(VAR_TASK_UUID, taskUuid));
    }

    public void checkReviewStage(String executionId, boolean isLastStage) {
        runtimeService.setVariable(executionId, VAR_LAST_STAGE, isLastStage);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void correlateProcess(MessageCorrelationBuilder correlation) {
        correlation.correlateWithResult().getExecution();
    }
}
