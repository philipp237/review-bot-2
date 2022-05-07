package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.app.impl.ts.*;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.adapter.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
    public void deletePreviousMessage(MessageInfo messageInfo) {
        Integer messageId = messageInfo.getMessageId();
        String chatId = messageInfo.getChatId();

        DeleteMessage deleteMessage = getDeleteMessage(chatId, messageId);
        webhookRestClient.deleteMessage(deleteMessage);
    }

    @Override
    public SendMessage processTaskLink(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String link = messageInfo.getText();
        String taskName = getTaskNameFromLink(link);

        validateTaskName(taskName);

        if (link.contains("#")) {
            TaskType taskType = getTaskTypeFromLink(link);
            link = link.split("#")[0];
            return createTask.execute(chatId, taskName, link, taskType);
        }

        InlineKeyboardMarkup keyboard = getKeyboard(TaskType.values().length);
        fillKeyboardWithTaskTypes(keyboard, link);

        return sendMessage(chatId, "Выберите тип задачи", keyboard);
    }

    @Override
    public SendMessage takeInReview(MessageInfo messageInfo) {
        return takeInReview.execute(messageInfo);
    }

    @Override
    public SendMessage acceptReview(MessageInfo messageInfo) {
        return acceptReview.execute(messageInfo);
    }

    @Override
    public SendMessage completeReview(MessageInfo messageInfo, boolean isApproved) {
        return completeReview.execute(messageInfo, isApproved);
    }

    @Override
    public SendMessage submitForReview(MessageInfo messageInfo) {
        return submitForReview.execute(messageInfo);
    }

    @Override
    public SendMessage closeTask(MessageInfo messageInfo) {
        return closeTask.execute(messageInfo);
    }

    @Override
    public SendMessage start(MessageInfo messageInfo) {
        return getStartMessage.execute(messageInfo);
    }

    @Override
    public SendMessage createTask(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();

        return sendMessage(chatId, "Вставь ссылку на задачу");
    }

    @Override
    public SendMessage getMemberReviews(MessageInfo messageInfo) {
        return getMemberReviews.execute(messageInfo);
    }

    @Override
    public void updateMemberLogin(MessageInfo messageInfo) {
        updateLogin.execute(messageInfo);
    }

    @Override
    public void updateChatId(MessageInfo messageInfo) {
        updateChatId.execute(messageInfo);
    }

    @Override
    public SendMessage getMemberTasks(MessageInfo messageInfo) {
        return getMemberTasks.execute(messageInfo);
    }

    @Override
    public SendMessage addMember(MessageInfo messageInfo) {
        return addMember.execute(messageInfo);
    }

    @Override
    public SendMessage updateMember(MessageInfo messageInfo) {
        return updateMember.execute(messageInfo);
    }

    @Override
    public SendMessage getTaskInfo(MessageInfo messageInfo) {
        return getTaskInfo.execute(messageInfo);
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

    private void validateTaskName(String taskName) {
        if (config.getDASHBOARDS().stream().noneMatch(taskName::contains)) {
            throw new IllegalArgumentException("Incorrect task name " + taskName);
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
