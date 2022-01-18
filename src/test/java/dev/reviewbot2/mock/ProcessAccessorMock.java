package dev.reviewbot2.mock;

import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import lombok.RequiredArgsConstructor;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;

@RequiredArgsConstructor
public class ProcessAccessorMock {
    private final ProcessAccessor processAccessor;

    public void mockStartProcess() {
        doNothing().when(processAccessor).startProcess(any());
    }

    public void mockTakeInReview() {
        doNothing().when(processAccessor).takeInReview(anyString());
    }

    public void mockCompleteReview() {
        doNothing().when(processAccessor).completeReview(anyString(), anyBoolean());
    }

    public void mockSubmitForReview() {
        doNothing().when(processAccessor).submitForReview(anyString());
    }
}
