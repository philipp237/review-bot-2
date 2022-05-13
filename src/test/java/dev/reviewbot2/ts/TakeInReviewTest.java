package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.TakeInReviewTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.exceptions.NotRequiredReviewGroupException;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ReviewServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.stream.Stream;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static dev.reviewbot2.processor.Command.TAKE_IN_REVIEW;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

public class TakeInReviewTest extends AbstractUnitTest {
    private final Review review1 = getReview(IMPLEMENTATION, BF, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, 1, MEMBER_1_CHAT_ID);
    private final Review review2 = getReview(IMPLEMENTATION, BF, FIRST_REVIEW_GROUP, UUID_2, TASK_NAME_2, 2, MEMBER_1_CHAT_ID);
    private final Review review3 = getReview(IMPLEMENTATION, BF, SECOND_REVIEW_GROUP, UUID_3, TASK_NAME_3, 3, MEMBER_1_CHAT_ID);
    private final Review review4 = getReview(DESIGN, BF, FIRST_REVIEW_GROUP, UUID_4, TASK_NAME_4, 4, MEMBER_1_CHAT_ID);

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
    void execute_firstGroupReviewer_taskInfo() {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_2_CHAT_ID, getCommand(TAKE_IN_REVIEW, review1.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review1.getTask());

        SendMessage sendMessage = takeInReview.execute(messageInfo);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(getCommand(ACCEPT_REVIEW, review1.getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_firstGroupReviewer_reviewList() {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_2_CHAT_ID, "/" + TAKE_IN_REVIEW);
        List<Review> reviews = Stream.of(review1, review2).collect(toList());

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(messageInfo);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(getCommand(TAKE_IN_REVIEW, reviews.get(0).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(getCommand(TAKE_IN_REVIEW, reviews.get(1).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_secondGroupReviewer_taskInfo() {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, SECOND_REVIEW_GROUP, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_2_CHAT_ID, getCommand(TAKE_IN_REVIEW, review3.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(reviewer);
        taskServiceMock.mockGetTaskById(review3.getTask());

        SendMessage sendMessage = takeInReview.execute(messageInfo);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TAKE_IN_REVIEW,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(getCommand(ACCEPT_REVIEW, review3.getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_secondGroupReviewerAndDesign_reviewList() {
        Member reviewer = getMember(MEMBER_2_CHAT_ID, SECOND_REVIEW_GROUP, true, false);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_2_CHAT_ID, "/" + TAKE_IN_REVIEW);
        List<Review> reviews = Stream.of(review3, review4).collect(toList());

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(messageInfo);

        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(getCommand(TAKE_IN_REVIEW, reviews.get(0).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(getCommand(TAKE_IN_REVIEW, reviews.get(1).getTask().getId()),
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void execute_emptyReviewList() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member reviewer = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, "/" + TAKE_IN_REVIEW);
        List<Review> reviews = Stream.of(review1, review2).collect(toList());

        memberServiceMock.mockGetMemberByChatId(reviewer);
        reviewServiceMock.mockGetReview(reviews);

        SendMessage sendMessage = takeInReview.execute(messageInfo);

        assertEquals("Нет доступных для ревью задач", sendMessage.getText());
    }

    @Test
    void execute_nonReviewer() {
        Member member = getMember(MEMBER_2_CHAT_ID, NON_REVIEWER, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(MEMBER_2_CHAT_ID, getCommand(TAKE_IN_REVIEW, review1.getTask().getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(review1.getTask());

        assertThrows(NotRequiredReviewGroupException.class, () -> takeInReview.execute(messageInfo));
    }
}