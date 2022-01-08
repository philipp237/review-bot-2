package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.app.impl.ts.AcceptReviewTransactionScript;
import dev.reviewbot2.app.impl.ts.CreateTaskTransactionScript;
import dev.reviewbot2.app.impl.ts.TakeInReviewTransactionScript;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.webhook.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Utils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateServiceImpl implements UpdateService {
    private final WebhookRestClient webhookRestClient;
    private final Config config;

    private final CreateTaskTransactionScript createTask;
    private final TakeInReviewTransactionScript takeInReview;
    private final AcceptReviewTransactionScript acceptReview;

    @Override
    public void deletePreviousMessage(Update update) throws TelegramApiException {
        Integer messageId = getMessageId(update);
        String chatId = getChatId(update);

        DeleteMessage deleteMessage = getDeleteMessage(chatId, messageId);
        webhookRestClient.deleteMessage(deleteMessage);
    }

    @Override
    public SendMessage processTaskLink(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String link = getTextFromUpdate(update);
        String taskName = getTaskNameFromLink(link);

        validateTaskName(taskName);

        if (link.contains("#")) {
            TaskType taskType = getTaskTypeFromLink(link);
            return createTask.execute(chatId, taskName, link, taskType);
        }

        InlineKeyboardMarkup keyboard = getKeyboard(TaskType.values().length);
        fillKeyboardWithTaskTypes(keyboard, taskName);

        String TASK_TYPE_CHOOSE_HINT = "Выберите тип задачи";
        return sendMessage(chatId, TASK_TYPE_CHOOSE_HINT, keyboard);
    }

    @Override
    public SendMessage takeInReview(Update update) throws TelegramApiException {
        return takeInReview.execute(update);
    }

    @Override
    public SendMessage acceptReview(Update update) throws TelegramApiException {
        return acceptReview.execute(update);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private DeleteMessage getDeleteMessage(String chatId, Integer messageId) {
        DeleteMessage.DeleteMessageBuilder deleteMessageBuilder = DeleteMessage.builder();
        deleteMessageBuilder.chatId(chatId);
        deleteMessageBuilder.messageId(messageId);
        return deleteMessageBuilder.build();
    }

    private String getTaskNameFromLink(String link) throws TelegramApiException {
        String[] parsedUrl = link.split("/");
        String rowTaskName = parsedUrl[parsedUrl.length - 1];

        if (rowTaskName.contains("#")) {
            return rowTaskName.split("#")[0];
        }

        return rowTaskName;
    }

    private TaskType getTaskTypeFromLink(String text) {
        String[] parsedUrl = text.split("#");
        return TaskType.valueOf(parsedUrl[parsedUrl.length - 1]);
    }

    private void validateTaskName(String taskName) throws TelegramApiException {
        if (config.DASHBOARDS.stream().noneMatch(taskName::contains)) {
            throw new TelegramApiException("Incorrect task name " + taskName);
        }
    }

    private void fillKeyboardWithTaskTypes(InlineKeyboardMarkup keyboard, String taskName) {
        int i = 0;

        for (TaskType taskType : TaskType.values()) {
            keyboard.getKeyboard().get(i).add(getButton(taskType.getName(), "/" + taskName + "#" + taskType.toString()));
            i++;
        }
    }
}
