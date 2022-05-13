package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.AcceptReviewTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.exceptions.NotRequiredTaskStatusException;
import dev.reviewbot2.exceptions.TaskInReviewException;
import dev.reviewbot2.mock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskStatus.CLOSED;
import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class AcceptReviewTest extends AbstractUnitTest {
    private AcceptReviewTransactionScript acceptReview;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        this.acceptReview =
             new AcceptReviewTransactionScript(memberService, taskService, reviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.memberReviewServiceMock = new MemberReviewServiceMock(memberReviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() {
        String reviewerChatId = MEMBER_2_CHAT_ID;
        String taskName = TASK_NAME_1;
        Review review = getReview(IMPLEMENTATION, BF,1, UUID_1, taskName, TASK_ID_1, MEMBER_1_CHAT_ID);
        MessageInfo messageInfo = getSimpleMessageInfo(reviewerChatId, getCommand(ACCEPT_REVIEW, review.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(getMember(reviewerChatId, FIRST_REVIEW_GROUP, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        SendMessage acceptReviewMessage = acceptReview.execute(messageInfo);

        verify(reviewService, times(1)).save(reviewArgumentCaptor.capture());

        assertEquals(review, reviewArgumentCaptor.getValue());
        assertEquals(String.format("Задача %s взята в ревью\n%s\n", taskName, JIRA_LINK + taskName),
            acceptReviewMessage.getText());
    }

    @Test
    void execute_taskAlreadyInReview() {
        String reviewerChatId = MEMBER_2_CHAT_ID;
        Review review = getReview(IMPLEMENTATION, BF, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(IN_REVIEW);
        MessageInfo messageInfo = getSimpleMessageInfo(reviewerChatId, getCommand(ACCEPT_REVIEW, review.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(getMember(reviewerChatId, FIRST_REVIEW_GROUP, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        assertThrows(TaskInReviewException.class, () -> acceptReview.execute(messageInfo));
    }

    @Test
    void execute_invalidStatus() {
        String reviewerChatId = MEMBER_2_CHAT_ID;
        Review review = getReview(IMPLEMENTATION, BF, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(CLOSED);
        MessageInfo messageInfo = getSimpleMessageInfo(reviewerChatId, getCommand(ACCEPT_REVIEW, review.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(getMember(reviewerChatId, FIRST_REVIEW_GROUP, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        assertThrows(NotRequiredTaskStatusException.class, () -> acceptReview.execute(messageInfo));
    }
}
