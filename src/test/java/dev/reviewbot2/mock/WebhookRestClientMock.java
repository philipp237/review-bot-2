package dev.reviewbot2.mock;

import dev.reviewbot2.adapter.WebhookRestClient;
import lombok.RequiredArgsConstructor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RequiredArgsConstructor
public class WebhookRestClientMock {
    private final WebhookRestClient webhookRestClient;

    public void mockSendMessage() {
        doNothing().when(webhookRestClient).sendMessage(any());
    }
}
