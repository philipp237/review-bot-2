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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

public class TakeInReviewTest extends AbstractUnitTest {
    private final Review review1 = getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, 1, MEMBER_1_CHAT_ID);
    private final Review review2 = getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_2, TASK_NAME_2, 2, MEMBER_1_CHAT_ID);
    private final Review review3 = getReview(IMPLEMENTATION, SECOND_REVIEW_GROUP, UUID_3, TASK_NAME_3, 3, MEMBER_1_CHAT_ID);
    private final Review review4 = getReview(DESIGN, FIRST_REVIEW_GROUP, UUID_4, TASK_NAME_4, 4, MEMBER_1_CHAT_ID);

    private TakeInReviewTransactionScript takeInReview;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        this.takeInReview = new TakeInReviewTransactionScript(memberService, taskService, reviewService);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.reviewServiceMock = new ReviewServiceMock(reviewService);
    }

    @Test
    void execute_firstGroupReviewer_taskInfo() throws TelegramApiException {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, TAKE_IN_REVIEW, review1.getTask().getId()), MEMBER_2_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review1.getTask());

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(String.format(COMMAND, ACCEPT_REVIEW, review1.getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_firstGroupReviewer_reviewList() throws TelegramApiException {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW, MEMBER_2_CHAT_ID);
        List<Review> reviews = Stream.of(review1, review2).collect(toList());

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(String.format(COMMAND, TAKE_IN_REVIEW, reviews.get(0).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(String.format(COMMAND, TAKE_IN_REVIEW, reviews.get(1).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_secondGroupReviewer_taskInfo() throws TelegramApiException {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, SECOND_REVIEW_GROUP, false, false);
        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, TAKE_IN_REVIEW, review3.getTask().getId()), MEMBER_2_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review3.getTask());

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(String.format(COMMAND, ACCEPT_REVIEW, review3.getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_secondGroupReviewerAndDesign_reviewList() throws TelegramApiException {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, SECOND_REVIEW_GROUP, true, false);
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW, MEMBER_2_CHAT_ID);
        List<Review> reviews = Stream.of(review3, review4).collect(toList());

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(String.format(COMMAND, TAKE_IN_REVIEW, reviews.get(0).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(String.format(COMMAND, TAKE_IN_REVIEW, reviews.get(1).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_nonReviewer() throws TelegramApiException {
        Member member = getMember(MEMBER_2_CHAT_ID, NON_REVIEWER, false, false);
        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, TAKE_IN_REVIEW, review1.getTask().getId()), MEMBER_2_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(review1.getTask());

        SendMessage sendMessage = takeInReview.execute(update);

        assertEquals("Ты не можешь ревьюить задачи", sendMessage.getText());
    }
}