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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class CreateTaskTest extends AbstractUnitTest {
    private CreateTaskTransactionScript createTaskTransactionScript;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        this.createTaskTransactionScript = new CreateTaskTransactionScript(memberService, taskService, reviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() {
        TaskType taskType = IMPLEMENTATION;
        String taskName = TASK_NAME_1;
        String chatId = MEMBER_1_CHAT_ID;

        Review review = getReview(taskType, BF, FIRST_REVIEW_GROUP, UUID_1, taskName, TASK_ID_1, chatId);
        Member member = review.getTask().getAuthor();

        memberServiceMock.mockGetMemberByChatId(member);
        reviewServiceMock.mockSave();
        processAccessorMock.mockStartProcess();

        SendMessage createMessageTask = createTaskTransactionScript.execute(chatId, taskName, JIRA_LINK + taskName, BF, taskType);

        verify(reviewService, times(1)).save(reviewArgumentCaptor.capture());
        assertReview(review, reviewArgumentCaptor.getValue());
        assertEquals(String.format("Задача %s создана", taskName), createMessageTask.getText());
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
