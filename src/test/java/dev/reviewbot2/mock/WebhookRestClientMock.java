package dev.reviewbot2.mock;

import dev.reviewbot2.webhook.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RequiredArgsConstructor
public class WebhookRestClientMock {
    private final WebhookRestClient webhookRestClient;

    public void mockSendMessage() throws TelegramApiException {
        doNothing().when(webhookRestClient).sendMessage(any());
    }
}
