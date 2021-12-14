package dev.reviewbot2;

import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import org.telegram.telegrambots.meta.api.objects.*;

import java.time.Instant;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;

public class AbstractTest {
    protected static final int MESSAGE_ID = 51632862;
    protected static final int UPDATE_ID = 42368635;
    protected static final long CHAT_ID = 57164325;
    protected static final String UUID = "29e286b2-5aa9-11ec-bf63-0242ac130002";
    protected static final String LOGIN = "test_login";
    protected static final String BOT_NAME = "test_bot_name";
    protected static final String BOT_TOKEN = "test_bot_token";
    protected static final String JIRA_LINK = "https://test.com/";
    protected static final List<String> DASHBOARD = List.of("TEST1");
    protected static final String TASK_NAME = DASHBOARD.get(0) + "-1234";
    protected static final String TASK_LINK = JIRA_LINK + TASK_NAME;

    protected Update getUpdateWithoutMessage() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        return update;
    }

    protected Update getUpdateWithMessage(String text) {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setMessage(getMessage(text));
        return update;
    }

    protected Update getUpdateWithCallbackQuery(String text) {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setCallbackQuery(getCallbackQuery(text));
        return update;
    }

    protected CallbackQuery getCallbackQuery(String text) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(getMessage(text));
        return callbackQuery;
    }

    protected Message getMessage(String text) {
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        message.setText(text);
        message.setFrom(getUser());
        message.setChat(getChat());
        return message;
    }

    protected User getUser() {
        User user = new User();
        user.setUserName("testUserName");
        return user;
    }

    protected Chat getChat() {
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        return chat;
    }

    protected Member getMember(int reviewGroup, boolean canReviewDesign, boolean isOmni) {
        return Member.builder()
            .chatId(String.valueOf(CHAT_ID))
            .login(LOGIN)
            .reviewGroup(reviewGroup)
            .canReviewDesign(canReviewDesign)
            .isOmni(isOmni)
            .build();
    }

    protected Task getTask(TaskType taskType) {
        return Task.builder()
            .uuid(UUID)
            .name(TASK_NAME)
            .link(TASK_LINK)
            .creationTime(Instant.now())
            .taskType(taskType)
            .status(READY_FOR_REVIEW)
            .author(getMember(0, false, false))
            .build();
    }

    protected Review getReview(TaskType taskType) {
        return Review.builder()
            .reviewStage(1)
            .task(getTask(taskType))
            .build();
    }
}
