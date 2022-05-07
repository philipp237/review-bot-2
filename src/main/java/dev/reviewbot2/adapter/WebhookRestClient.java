package dev.reviewbot2.adapter;

import dev.reviewbot2.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRestClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Config config;

    private static final String URL = "https://api.telegram.org/bot";
    private static final String DELETE = "/deleteMessage";
    private static final String SEND = "/sendMessage";

    public void deleteMessage(DeleteMessage deleteMessage) {
        HttpEntity<DeleteMessage> request = new HttpEntity<>(deleteMessage);
        boolean published =
            restTemplate.postForEntity(URL + config.BOT_TOKEN + DELETE, request, String.class).getStatusCode() == HttpStatus.OK;

        if (!published) {
            log.error("Message with id={} wasn't deleted", deleteMessage.getMessageId());
            throw new IllegalStateException("Error while deleting message");
        }
    }

    public void sendMessage(SendMessage sendMessage) {
        HttpEntity<SendMessage> request = new HttpEntity<>(sendMessage);
        boolean published =
            restTemplate.postForEntity(URL + config.BOT_TOKEN + SEND, request, String.class).getStatusCode() == HttpStatus.OK;

        if (!published) {
            log.error("Message to chat with id={} wasn't sent", sendMessage.getChatId());
            throw new IllegalStateException("Error while sending message");
        }
    }
}