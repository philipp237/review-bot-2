package dev.reviewbot2.processor;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.MessageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageProcessor {
    private final MemberService memberService;
    private final UpdateService updateService;
    private final CommandProcessor commandProcessor;
    private final Config config;

    public BotApiMethod<?> processMessage(MessageInfo messageInfo) {
        String text = messageInfo.getText();
        if (text != null && hasAuthorities(messageInfo)) {
            log.info("Message {} was received by {}", text, messageInfo.getLogin());
            return processRequest(messageInfo);
        } else {
            return null;
        }
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private BotApiMethod<?> processRequest(MessageInfo messageInfo) {
        if (messageInfo.getHasCallbackQuery()) {
            updateService.deletePreviousMessage(messageInfo);
        }
        String messageText = messageInfo.getText();
        if (messageText.startsWith(config.JIRA_LINK)) {
            return updateService.processTaskLink(messageInfo);
        }
        if (messageText.startsWith("/")) {
            return commandProcessor.processCommand(messageInfo);
        }
        return null;
    }

    private boolean hasAuthorities(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String login = messageInfo.getLogin();

        boolean chatIdExists = memberService.isChatIdExists(chatId);
        boolean loginExists = config.BOT_NAME.equals(login) || memberService.isLoginExists(login);
        if (!chatIdExists && !loginExists) {
            log.warn("{} has no authorities", login);
        }

        if (chatIdExists && !loginExists) {
            updateService.updateMemberLogin(messageInfo);
        }

        if (!chatIdExists && loginExists) {
            updateService.updateChatId(messageInfo);
        }

        return chatIdExists;
    }
}
