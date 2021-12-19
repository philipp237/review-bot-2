package dev.reviewbot2.mock;

import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import lombok.RequiredArgsConstructor;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;

@RequiredArgsConstructor
public class ProcessAccessorMock {
    private final ProcessAccessor processAccessor;

    public void mockStartProcess() {
        doNothing().when(processAccessor).startProcess(any());
    }
}
