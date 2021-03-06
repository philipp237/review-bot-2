package dev.reviewbot2;

import dev.reviewbot2.app.api.MemberReviewService;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.mock.*;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public abstract class AbstractUnitTest extends AbstractTest {
    protected final Config config;

    protected AutoCloseable closeable;

    @Mock
    protected MemberService memberService;
    @Mock
    protected TaskService taskService;
    @Mock
    protected ReviewService reviewService;
    @Mock
    protected ProcessAccessor processAccessor;
    @Mock
    protected MemberReviewService memberReviewService;

    @Captor
    protected ArgumentCaptor<Review> reviewArgumentCaptor;
    @Captor
    protected ArgumentCaptor<MemberReview> memberReviewArgumentCaptor;
    @Captor
    protected ArgumentCaptor<Task> taskArgumentCaptor;
    @Captor
    protected ArgumentCaptor<Member> memberArgumentCaptor;

    protected MemberServiceMock memberServiceMock;
    protected TaskServiceMock taskServiceMock;
    protected ReviewServiceMock reviewServiceMock;
    protected ProcessAccessorMock processAccessorMock;
    protected MemberReviewServiceMock memberReviewServiceMock;

    public AbstractUnitTest() {
        Config config = new Config();
        config.BOT_NAME = BOT_NAME;
        config.BOT_TOKEN = BOT_TOKEN;
        config.JIRA_LINK = JIRA_LINK;
        config.setDASHBOARDS(DASHBOARD);
        this.config = config;
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    protected MessageInfo getMessageInfo(int messageId, String chatId, String text, String login, boolean hasCallbackQuery) {
        return MessageInfo.builder()
            .messageId(messageId)
            .chatId(chatId)
            .text(text)
            .login(login)
            .hasCallbackQuery(hasCallbackQuery)
            .build();
    }

    protected MessageInfo getSimpleMessageInfo(String chatId, String text) {
        return getMessageInfo(1, chatId, text, LOGIN, false);
    }
}
