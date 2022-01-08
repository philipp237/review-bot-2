package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.AcceptReviewTransactionScript;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.mock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class AcceptReviewTest extends AbstractUnitTest {
    private AcceptReviewTransactionScript acceptReview;

    @Captor
    ArgumentCaptor<MemberReview> memberReviewArgumentCaptor;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.acceptReview =
             new AcceptReviewTransactionScript(memberService, taskService, reviewService, memberReviewService, processAccessor);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
        this.memberReviewServiceMock = new MemberReviewServiceMock(memberReviewService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() throws TelegramApiException {
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, 1);
        Update update = getUpdateWithCallbackQuery("/" + ACCEPT_REVIEW + "#" + review.getTask().getId(), REVIEWER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(getMember(REVIEWER_CHAT_ID, 1, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        acceptReview.execute(update);

        verify(memberReviewService, times(1)).save(memberReviewArgumentCaptor.capture());

        assertEquals(review, memberReviewArgumentCaptor.getValue().getReview());
    }
}
