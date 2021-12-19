package dev.reviewbot2;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.config.Config;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.ReviewServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
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

    @Captor
    protected ArgumentCaptor<Review> reviewArgumentCaptor;

    protected MemberServiceMock memberServiceMock;
    protected TaskServiceMock taskServiceMock;
    protected ReviewServiceMock reviewServiceMock;
    protected ProcessAccessorMock processAccessorMock;

    public AbstractUnitTest() {
        Config config = new Config();
        config.BOT_NAME = BOT_NAME;
        config.BOT_TOKEN = BOT_TOKEN;
        config.JIRA_LINK = JIRA_LINK;
        config.DASHBOARDS = DASHBOARD;
        this.config = config;
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }
}
