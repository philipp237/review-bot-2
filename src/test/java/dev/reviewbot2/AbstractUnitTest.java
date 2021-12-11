package dev.reviewbot2;

import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.domain.task.TaskType;
import org.mockito.Mock;
import org.telegram.telegrambots.meta.api.objects.*;

import java.time.Instant;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.*;

public abstract class AbstractUnitTest {
    protected final Config config;

    protected static final int MESSAGE_ID = 51632862;
    protected static final int UPDATE_ID = 42368635;
    protected static final long CHAT_ID = 57164325;
    protected static final String UUID = "29e286b2-5aa9-11ec-bf63-0242ac130002";
    protected static final String LOGIN = "test_login";
    protected static final String BOT_NAME = "test_bot_name";
    protected static final String BOT_TOKEN = "test_bot_config";
    protected static final String JIRA_LINK = "https://test.com/";
    protected static final List<String> DASHBOARD = List.of("TEST1", "TEST2");
    protected static final String TASK_NAME = DASHBOARD.get(0) + "-1234";
    protected static final String TASK_LINK = JIRA_LINK + TASK_NAME;

    @Mock
    private ProcessAccessor processAccessor;



    public AbstractUnitTest() {
        Config config = new Config();
        config.BOT_NAME = BOT_NAME;
        config.BOT_TOKEN = BOT_TOKEN;
        config.JIRA_LINK = JIRA_LINK;
        config.DASHBOARDS = DASHBOARD;
        this.config = config;
    }

    protected Update getUpdateWithoutMessage() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        return update;
    }

    protected Update getUpdateWithMessage() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setMessage(getMessage());
        return update;
    }

    protected Update getUpdateWithCallbackQuery() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setCallbackQuery(getCallbackQuery());
        return update;
    }

    protected CallbackQuery getCallbackQuery() {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(getMessage());
        return callbackQuery;
    }

    protected Message getMessage() {
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        message.setText(JIRA_LINK + DASHBOARD.get(0));
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
