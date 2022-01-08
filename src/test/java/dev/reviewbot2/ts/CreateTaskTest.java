package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.CreateTaskTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.ReviewServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CreateTaskTest extends AbstractUnitTest {
    private CreateTaskTransactionScript createTaskTransactionScript;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.createTaskTransactionScript = new CreateTaskTransactionScript(memberService, taskService, reviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() {
        TaskType taskType = TaskType.IMPLEMENTATION;
        Review review = getReview(taskType, 1, UUID_1, TASK_NAME_1, 1);
        Member member = review.getTask().getAuthor();

        memberServiceMock.mockGetMemberByChatId(member);
        reviewServiceMock.mockSave();
        processAccessorMock.mockStartProcess();

        createTaskTransactionScript.execute(MEMBER_CHAT_ID, TASK_NAME_1, JIRA_LINK + TASK_NAME_1, taskType);

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
        assertEquals(expected.getAuthor(), actual.getAuthor());
    }

}
