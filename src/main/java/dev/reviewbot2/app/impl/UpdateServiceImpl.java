package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.app.impl.ts.*;
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
    private final CompleteReviewTransactionScript completeReview;
    private final SubmitForReviewTransactionScript submitForReview;
    private final CloseTaskTransactionScript closeTask;
    private final GetMemberReviewsTransactionScript getMemberReviews;
    private final UpdateLoginTransactionScript updateLogin;
    private final UpdateChatIdTransactionScript updateChatId;
    private final AddMemberTransactionScript addMember;
    private final UpdateMemberTransactionScript updateMember;
    private final GetMemberTasksTransactionScript getMemberTasks;
    private final GetTaskInfoTransactionScript getTaskInfo;
    private final GetStartMessageTransactionScript getStartMessage;

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
            link = link.split("#")[0];
            return createTask.execute(chatId, taskName, link, taskType);
        }

        InlineKeyboardMarkup keyboard = getKeyboard(TaskType.values().length);
        fillKeyboardWithTaskTypes(keyboard, link);

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

    @Override
    public SendMessage completeReview(Update update, boolean isApproved) throws TelegramApiException {
        return completeReview.execute(update, isApproved);
    }

    @Override
    public SendMessage submitForReview(Update update) throws TelegramApiException {
        return submitForReview.execute(update);
    }

    @Override
    public SendMessage closeTask(Update update) throws TelegramApiException {
        return closeTask.execute(update);
    }

    @Override
    public SendMessage start(Update update) throws TelegramApiException {
        return getStartMessage.execute(update);
    }

    @Override
    public SendMessage createTask(Update update) throws TelegramApiException {
        String chatId = getChatId(update);

        return sendMessage(chatId, "Вставь ссылку на задачу");
    }

    @Override
    public SendMessage getMemberReviews(Update update) throws TelegramApiException {
        return getMemberReviews.execute(update);
    }

    @Override
    public void updateMemberLogin(String chatId, String login) {
        updateLogin.execute(chatId, login);
    }

    @Override
    public void updateChatId(String chatId, String login) {
        updateChatId.execute(chatId, login);
    }

    @Override
    public SendMessage getMemberTasks(Update update) throws TelegramApiException {
        return getMemberTasks.execute(update);
    }

    @Override
    public SendMessage addMember(Update update) throws TelegramApiException {
        return addMember.execute(update);
    }

    @Override
    public SendMessage updateMember(Update update) throws TelegramApiException {
        return updateMember.execute(update);
    }

    @Override
    public SendMessage getTaskInfo(Update update) throws TelegramApiException {
        return getTaskInfo.execute(update);
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

    private String getTaskNameFromLink(String link) {
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
        if (config.getDASHBOARDS().stream().noneMatch(taskName::contains)) {
            throw new TelegramApiException("Incorrect task name " + taskName);
        }
    }

    private void fillKeyboardWithTaskTypes(InlineKeyboardMarkup keyboard, String link) {
        int i = 0;

        for (TaskType taskType : TaskType.values()) {
            keyboard.getKeyboard().get(i).add(getButton(taskType.getName(), link + "#" + taskType.toString()));
            i++;
        }
    }
}
