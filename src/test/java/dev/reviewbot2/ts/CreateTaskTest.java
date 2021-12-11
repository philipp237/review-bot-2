package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.app.impl.ts.CreateTaskTransactionalScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.ReviewServiceMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CreateTaskTest extends AbstractUnitTest {
    @Mock
    private MemberService memberService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private ProcessAccessor processAccessor;

    private AutoCloseable closeable;
    private ArgumentCaptor<Review> reviewArgumentCaptor;

    private MemberServiceMock memberServiceMock;
    private ReviewServiceMock reviewServiceMock;
    private ProcessAccessorMock processAccessorMock;

    private CreateTaskTransactionalScript createTaskTransactionalScript;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.createTaskTransactionalScript = new CreateTaskTransactionalScript(memberService, reviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);

        this.reviewArgumentCaptor = ArgumentCaptor.forClass(Review.class);
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void execute() {
        TaskType taskType = TaskType.IMPLEMENTATION;
        Review review = getReview(taskType);
        Member member = review.getTask().getAuthor();

        memberServiceMock.mockGetMemberByChatId(member);
        reviewServiceMock.mockSave();
        processAccessorMock.mockStartProcess();

        createTaskTransactionalScript.execute(String.valueOf(CHAT_ID), TASK_NAME, TASK_LINK, taskType);

        verify(reviewService, times(1)).save(reviewArgumentCaptor.capture());
        assertReview(review, reviewArgumentCaptor.getValue());
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void assertReview(Review expected, Review actual) {
        assertEquals(expected.getReviewStage(), actual.getReviewStage());
        assertTask(expected.getTask(), actual.getTask());
    }

    private void assertTask(Task expected, Task actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getLink(), actual.getLink());
        assertEquals(expected.getTaskType(), actual.getTaskType());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getAuthor(), actual.getAuthor());
    }

}
