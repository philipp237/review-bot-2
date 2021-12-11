package dev.reviewbot2.app.impl.camunda;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessAccessor {
    private final ProcessEngine processEngine;

    public void startProcess(String uuid) {
        processEngine.getRuntimeService().startProcessInstanceByKey("review-process", uuid);
    }
}
