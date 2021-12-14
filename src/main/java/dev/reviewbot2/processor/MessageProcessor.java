package dev.reviewbot2.processor;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.config.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Utils.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class MessageProcessor {
    private final MemberService memberService;
    private final UpdateService updateService;
    private final Config config;

    public BotApiMethod<?> processMessage(Update update) throws TelegramApiException {
        if (updateHasMessage(update) && hasAuthorities(update)) {
            return processRequest(update);
        } else {
            return null;
        }
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private BotApiMethod<?> processRequest(Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            updateService.deletePreviousMessage(update);
        }
        String messageText = getTextFromUpdate(update);
        if (messageText.startsWith(config.JIRA_LINK)) {
            return updateService.processTaskLink(update);
            //TODO Добавить интеграционные тесты на это дело
        }
        if (messageText.startsWith("/")) {
            //TODO Обработка команд
        }
        return null;
    }

    private boolean hasAuthorities(Update update) throws TelegramApiException {
        boolean hasAuthorities = memberService.isExists(getChatId(update));
        if (!hasAuthorities) {
            log.warn("{} has no authorities", getLoginFromUpdate(update));
        }
        return hasAuthorities;
    }
}
