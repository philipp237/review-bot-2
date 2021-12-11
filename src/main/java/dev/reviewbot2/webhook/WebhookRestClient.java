package dev.reviewbot2.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.config.Config.BOT_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRestClient {
    private final RestTemplate restTemplate;

    private String url = "https://api.telegram.org/bot" + BOT_TOKEN;
    private String delete = "/deleteMessage";

    public void deleteMessage(DeleteMessage deleteMessage) throws TelegramApiException {
        HttpEntity<DeleteMessage> request = new HttpEntity<>(deleteMessage);
        boolean published = restTemplate.postForObject(url + delete, request, Boolean.class) != null;

        if (!published) {
            log.error("Message with id={} wasn't deleted", deleteMessage.getMessageId());
            throw new TelegramApiException("Error while deleting message");
        }
    }
}