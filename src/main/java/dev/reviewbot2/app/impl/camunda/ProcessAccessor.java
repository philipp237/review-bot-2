package dev.reviewbot2.app.impl.camunda;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessAccessor {
    private final String PROCESS_ID = "review-process";

    private final RuntimeService runtimeService;

    public void startProcess(String uuid) {
        runtimeService.startProcessInstanceByKey(PROCESS_ID, uuid);
    }

    public String getTaskUuid(DelegateExecution execution) {
        return execution.getBusinessKey();
    }
}
