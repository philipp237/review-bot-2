package dev.reviewbot2.processor;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Utils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageProcessor {
    private final MemberService memberService;
    private final UpdateService updateService;
    private final CommandProcessor commandProcessor;
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
        }
        if (messageText.startsWith("/")) {
            return commandProcessor.processCommand(update);
        }
        return null;
    }

    private boolean hasAuthorities(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String login = getLoginFromUpdate(update);

        boolean chatIdExists = memberService.isChatIdExists(chatId);
        boolean loginExists = memberService.isLoginExists(login);
        if (!chatIdExists && !loginExists) {
            log.warn("{} has no authorities", getLoginFromUpdate(update));
        }

        if (chatIdExists && !loginExists) {
            updateService.updateMemberLogin(chatId, login);
        }

        if (!chatIdExists && loginExists) {

        }

        return chatIdExists;
    }
}
