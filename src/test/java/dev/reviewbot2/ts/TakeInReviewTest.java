package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.TakeInReviewTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ReviewServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static dev.reviewbot2.processor.Command.TAKE_IN_REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TakeInReviewTest extends AbstractUnitTest {
    private final Review review1 = getReview(IMPLEMENTATION, 1, UUID_1, TASK_NAME_1, 1);
    private final Review review2 = getReview(IMPLEMENTATION, 1, UUID_2, TASK_NAME_2, 2);
    private final Review review3 = getReview(IMPLEMENTATION, 2, UUID_3, TASK_NAME_3, 3);
    private final Review review4 = getReview(DESIGN, 1, UUID_4, TASK_NAME_4, 4);

    private TakeInReviewTransactionScript takeInReview;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.takeInReview = new TakeInReviewTransactionScript(memberService, taskService, reviewService);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
    }

    @Test
    void execute_firstGroupReviewer_taskInfo() throws TelegramApiException {
        Member reviewer = getMember(REVIEWER_CHAT_ID, 1, false, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW + "#" + review1.getTask().getId(), REVIEWER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review1.getTask());

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("/" + ACCEPT_REVIEW + "#" + review1.getTask().getId(),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_firstGroupReviewer_reviewList() throws TelegramApiException {
        Member reviewer = getMember(REVIEWER_CHAT_ID, 1, false, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW, REVIEWER_CHAT_ID);
        List<Review> reviews = List.of(review1, review2);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW + "#" + reviews.get(0).getTask().getId(),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("/" + TAKE_IN_REVIEW + "#" + reviews.get(1).getTask().getId(),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_secondGroupReviewer_taskInfo() throws TelegramApiException {
        Member reviewer = getMember(REVIEWER_CHAT_ID, 2, false, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW + "#" + review3.getTask().getId(), REVIEWER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review3.getTask());

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("/" + ACCEPT_REVIEW + "#" + review3.getTask().getId(),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_secondGroupReviewerAndDesign_reviewList() throws TelegramApiException {
        Member reviewer = getMember(REVIEWER_CHAT_ID, 2, true, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW, REVIEWER_CHAT_ID);
        List<Review> reviews = List.of(review3, review4);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW + "#" + reviews.get(0).getTask().getId(),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("/" + TAKE_IN_REVIEW + "#" + reviews.get(1).getTask().getId(),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_nonReviewer() throws TelegramApiException {
        Member member = getMember(REVIEWER_CHAT_ID, 0, false, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW + "#" + review1.getTask().getId(), REVIEWER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(review1.getTask());

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals("Ты не можешь ревьюить задачи", sendMessage.getText());
    }
}