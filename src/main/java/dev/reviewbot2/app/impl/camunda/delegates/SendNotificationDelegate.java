package dev.reviewbot2.app.impl.camunda.delegates;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendNotificationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
    }
}
