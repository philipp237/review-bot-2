package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.CompleteReviewTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.exceptions.NotRequiredTaskStatusException;
import dev.reviewbot2.exceptions.NotSameReviewerException;
import dev.reviewbot2.mock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class CompleteReviewTest extends AbstractUnitTest {
    private CompleteReviewTransactionScript completeReview;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        this.completeReview =
            new CompleteReviewTransactionScript(memberService, taskService, reviewService, memberReviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.memberReviewServiceMock = new MemberReviewServiceMock(memberReviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute_happyPath_approved() {
        String taskUuid = UUID_1;
        String reviewerChatId = MEMBER_2_CHAT_ID;
        Member reviewer = getMember(reviewerChatId, FIRST_REVIEW_GROUP, false, false);
        Review review = getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, taskUuid, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(IN_REVIEW);
        MessageInfo messageInfo = getSimpleMessageInfo(reviewerChatId, getCommand(APPROVE, review.getTask().getId()));

        mockInnerMethods(review, reviewer);

        SendMessage approveMessage = completeReview.execute(messageInfo, true);

        verify(memberReviewService, times(1)).save(memberReviewArgumentCaptor.capture());
        verify(processAccessor, times(1)).completeReview(taskUuid, true);

        assertNotNull(memberReviewArgumentCaptor.getValue().getEndTime());
        assertEquals("Задача одобрена", approveMessage.getText());
    }

    @Test
    void execute_happyPath_declined() {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        Review review = getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(IN_REVIEW);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_2_CHAT_ID, getCommand(DECLINE, review.getTask().getId()));

        mockInnerMethods(review, reviewer);

        SendMessage declineMessage = completeReview.execute(messageInfo, false);

        verify(memberReviewService, times(1)).save(memberReviewArgumentCaptor.capture());
        verify(processAccessor, times(1)).completeReview(UUID_1, false);

        assertNotNull(memberReviewArgumentCaptor.getValue().getEndTime());
        assertEquals("Задача возвращена на доработку", declineMessage.getText());
    }

    @Test
    void execute_validationFailed_invalidStatus() {
        String reviewerChatId = MEMBER_2_CHAT_ID;
        Member reviewer = getMember(reviewerChatId, FIRST_REVIEW_GROUP, false, false);
        Review review = getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(READY_FOR_REVIEW);
        MemberReview memberReview = getMemberReview(review, reviewer);
        MessageInfo messageInfo = getSimpleMessageInfo(reviewerChatId, getCommand(DECLINE, review.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockGetActiveReview(memberReview);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockCompleteReview();

        assertThrows(NotRequiredTaskStatusException.class, () -> completeReview.execute(messageInfo, true));
    }

    @Test
    void execute_validationFailed_notSameReviewer() {
        Member reviewer1 = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        Member reviewer2 = getMember(MEMBER_3_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        Review review = getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(IN_REVIEW);
        MemberReview memberReview = getMemberReview(review, reviewer1);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_3_CHAT_ID, getCommand(DECLINE, review.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(reviewer2);
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockGetActiveReview(memberReview);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockCompleteReview();

        assertThrows(NotSameReviewerException.class, () -> completeReview.execute(messageInfo, true));
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void mockInnerMethods(Review review, Member reviewer) {
        MemberReview memberReview = getMemberReview(review, reviewer);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockGetActiveReview(memberReview);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockCompleteReview();
    }
}
