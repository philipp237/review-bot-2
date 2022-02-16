package dev.reviewbot2;

import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static java.time.Instant.now;
import static java.util.Collections.singletonList;

public class AbstractTest {
    protected static final int MESSAGE_ID = 51632862;
    protected static final int UPDATE_ID = 42368635;
    protected static final String LOGIN = "test_login";
    protected static final String BOT_NAME = "test_bot_name";
    protected static final String BOT_TOKEN = "test_bot_token";
    protected static final String JIRA_LINK = "https://test.com/";
    protected static final String DASHBOARD = "TEST1";

    protected static final String UUID_1 = "29e286b2-5aa9-11ec-bf63-0242ac130002";
    protected static final String UUID_2 = "1258ce35-b1a9-494b-b1e0-76cc64e8f0c5";
    protected static final String UUID_3 = "0385cd13-44e3-4bf5-901e-15ce4f58f6dd";
    protected static final String UUID_4 = "b0a7165f-7c6e-419e-bdb3-8bebf7bf3ee9";

    protected static final String TASK_NAME_1 = DASHBOARD + "-1111";
    protected static final String TASK_NAME_2 = DASHBOARD + "-2222";
    protected static final String TASK_NAME_3 = DASHBOARD + "-3333";
    protected static final String TASK_NAME_4 = DASHBOARD + "-4444";

    protected static final String MEMBER_1_CHAT_ID = "57164325";
    protected static final String MEMBER_2_CHAT_ID = "27621396";
    protected static final String MEMBER_3_CHAT_ID = "56247114";

    protected static final long TASK_ID_1 = 1L;

    protected Update getUpdateWithoutMessage() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        return update;
    }

    protected Update getUpdateWithMessage(String text, String chatId) {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setMessage(getMessage(text, chatId));
        return update;
    }

    protected Update getUpdateWithCallbackQuery(String text, String chatId) {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setCallbackQuery(getCallbackQuery(text, chatId));
        return update;
    }

    protected CallbackQuery getCallbackQuery(String text, String chatId) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(text);
        callbackQuery.setMessage(getMessage(null, chatId));
        return callbackQuery;
    }

    protected Message getMessage(String text, String chatId) {
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        message.setText(text);
        message.setFrom(getUser());
        message.setChat(getChat(chatId));
        return message;
    }

    protected User getUser() {
        User user = new User();
        user.setUserName("testUserName");
        return user;
    }

    protected Chat getChat(String chatId) {
        Chat chat = new Chat();
        chat.setId(Long.parseLong(chatId));
        return chat;
    }

    protected Member getMember(String chatId, int reviewGroup, boolean canReviewDesign, boolean isOmni) {
        return Member.builder()
            .chatId(chatId)
            .login(LOGIN)
            .reviewGroup(reviewGroup)
            .canReviewDesign(canReviewDesign)
            .isOmni(isOmni)
            .build();
    }

    protected Task getTask(TaskType taskType, String uuid, String taskName, long taskId, String chatId) {
        return Task.builder()
            .id(taskId)
            .uuid(uuid)
            .name(taskName)
            .link(JIRA_LINK + taskName)
            .creationTime(now())
            .taskType(taskType)
            .status(READY_FOR_REVIEW)
            .author(getMember(chatId, 0, false, false))
            .build();
    }

    protected Review getReview(TaskType taskType, int reviewStage, String uuid, String taskName, long taskId, String chatId) {
        return Review.builder()
            .id(1L)
            .reviewStage(reviewStage)
            .task(getTask(taskType, uuid, taskName, taskId, chatId))
            .build();
    }

    protected MemberReview getMemberReview(Review review, Member member) {
        return MemberReview.builder()
            .startTime(now())
            .reviewer(member)
            .review(review)
            .build();
    }
}
