package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.AcceptReviewTransactionScript;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.mock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.domain.task.TaskStatus.CLOSED;
import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class AcceptReviewTest extends AbstractUnitTest {
    private AcceptReviewTransactionScript acceptReview;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
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
        String reviewerChatId = MEMBER_2_CHAT_ID;
        String taskName = TASK_NAME_1;
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, taskName, TASK_ID_1, MEMBER_1_CHAT_ID);
        Update update = getUpdateWithCallbackQuery("/" + ACCEPT_REVIEW + "#" + review.getTask().getId(), reviewerChatId);

        memberServiceMock.mockGetMemberByChatId(getMember(reviewerChatId, 1, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        SendMessage acceptReviewMessage = acceptReview.execute(update);

        verify(memberReviewService, times(1)).save(memberReviewArgumentCaptor.capture());

        assertEquals(review, memberReviewArgumentCaptor.getValue().getReview());
        assertEquals(String.format("Задача %s взята в ревью\n%s\n", taskName, JIRA_LINK + taskName),
            acceptReviewMessage.getText());
    }

    @Test
    void execute_taskAlreadyInReview() throws TelegramApiException {
        String reviewerChatId = MEMBER_2_CHAT_ID;
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(IN_REVIEW);
        Update update = getUpdateWithCallbackQuery("/" + ACCEPT_REVIEW + "#" + review.getTask().getId(), reviewerChatId);

        memberServiceMock.mockGetMemberByChatId(getMember(reviewerChatId, 1, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        SendMessage taskAlreadyInReview = acceptReview.execute(update);
        assertEquals("Кто-то успел взять задачу на ревью раньше тебя ¯\\_(ツ)_/¯", taskAlreadyInReview.getText());
    }

    @Test
    void execute_invalidStatus() throws TelegramApiException {
        String reviewerChatId = MEMBER_2_CHAT_ID;
        Review review = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        review.getTask().setStatus(CLOSED);
        Update update = getUpdateWithCallbackQuery("/" + ACCEPT_REVIEW + "#" + review.getTask().getId(), reviewerChatId);

        memberServiceMock.mockGetMemberByChatId(getMember(reviewerChatId, 1, false, false));
        taskServiceMock.mockGetTaskById(review.getTask());
        reviewServiceMock.mockGetReviewByTask(review);
        memberReviewServiceMock.mockSave();
        processAccessorMock.mockTakeInReview();

        SendMessage taskInInvalidStatus = acceptReview.execute(update);
        assertEquals("Задачу нельзя взять в ревью", taskInInvalidStatus.getText());
    }
}
