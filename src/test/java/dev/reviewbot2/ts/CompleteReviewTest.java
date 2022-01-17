package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.CompleteReviewTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.mock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CompleteReviewTest extends AbstractUnitTest {
    private CompleteReviewTransactionScript completeReview;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.completeReview =
            new CompleteReviewTransactionScript(memberService, taskService, reviewService, memberReviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.memberReviewServiceMock = new MemberReviewServiceMock(memberReviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute_happyPath_approved() throws TelegramApiException {
        Member reviewer = getMember(REVIEWER_1_CHAT_ID, 1, false, false);
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, TASK_ID_1);
        Update update = getUpdateWithCallbackQuery("/" + APPROVE + "#" + review.getTask().getId(), REVIEWER_1_CHAT_ID);

        mockInnerMethods(review, reviewer);

        completeReview.execute(update, true);

        verify(memberReviewService, times(1)).save(memberReviewArgumentCaptor.capture());
        verify(processAccessor, times(1)).completeReview(UUID_1, true);

        assertNotNull(memberReviewArgumentCaptor.getValue().getEndTime());
    }

    @Test
    void execute_happyPath_declined() throws TelegramApiException {
        Member reviewer = getMember(REVIEWER_1_CHAT_ID, 1, false, false);
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, TASK_ID_1);
        Update update = getUpdateWithCallbackQuery("/" + DECLINE + "#" + review.getTask().getId(), REVIEWER_1_CHAT_ID);

        mockInnerMethods(review, reviewer);

        completeReview.execute(update, false);

        verify(memberReviewService, times(1)).save(memberReviewArgumentCaptor.capture());
        verify(processAccessor, times(1)).completeReview(UUID_1, false);

        assertNotNull(memberReviewArgumentCaptor.getValue().getEndTime());
    }

    @Test
    void execute_validationFailed() throws TelegramApiException {
        Member reviewer1 = getMember(REVIEWER_1_CHAT_ID, 1, false, false);
        Member reviewer2 = getMember(REVIEWER_2_CHAT_ID, 1, false, false);
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, TASK_ID_1);
        MemberReview memberReview = getMemberReview(review, reviewer1);
        Update update = getUpdateWithCallbackQuery("/" + DECLINE + "#" + review.getTask().getId(), REVIEWER_2_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(reviewer2);
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockGetActiveReview(memberReview);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockCompleteReview();

        assertThrows(TelegramApiException.class, () -> completeReview.execute(update, true));
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
