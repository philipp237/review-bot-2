package dev.reviewbot2.webhook;

import dev.reviewbot2.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRestClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Config config;

    private String url = "https://api.telegram.org/bot";
    private String delete = "/deleteMessage";
    private String send = "/sendMessage";

    public void deleteMessage(DeleteMessage deleteMessage) throws TelegramApiException {
        HttpEntity<DeleteMessage> request = new HttpEntity<>(deleteMessage);
        boolean published = restTemplate.postForObject(url + config.BOT_TOKEN + delete, request, Boolean.class) != null;

        if (!published) {
            log.error("Message with id={} wasn't deleted", deleteMessage.getMessageId());
            throw new TelegramApiException("Error while deleting message");
        }
    }

    public void sendMessage(SendMessage sendMessage) throws TelegramApiException {
        HttpEntity<SendMessage> request = new HttpEntity<>(sendMessage);
        boolean published = restTemplate.postForObject(url + config.BOT_TOKEN + send, request, Boolean.class) != null;

        if (!published) {
            log.error("Message to chat with id={} wasn't sent", sendMessage.getChatId());
            throw new TelegramApiException("Error while sending message");
        }
    }
}