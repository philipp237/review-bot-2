package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.GetMemberTasksTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
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
import java.util.stream.Stream;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetMemberTasksTest extends AbstractUnitTest {
    private GetMemberTasksTransactionScript getMemberTasks;

    private static final String GET_INFO = "/INFO#";

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        getMemberTasks = new GetMemberTasksTransactionScript(memberService, taskService, reviewService);
        memberServiceMock = new MemberServiceMock(memberService);
        taskServiceMock = new TaskServiceMock(taskService);
        reviewServiceMock = new ReviewServiceMock(reviewService);
    }

    @Test
    void happyPath() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId1 = TASK_ID_1;
        long taskId2 = TASK_ID_2;
        long taskId3 = TASK_ID_3;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);

        Update update = getUpdateWithCallbackQuery("/my_tasks", MEMBER_1_CHAT_ID);

        List<Review> reviews = Stream.of(
            getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, taskId1, memberChatId),
            getReview(IMPLEMENTATION, FIRST_REVIEW_GROUP, UUID_2, TASK_NAME_2, taskId2, memberChatId),
            getReview(DESIGN, FIRST_REVIEW_GROUP, UUID_3, TASK_NAME_3, taskId3, memberChatId)
        ).collect(toList());

        List<Task> tasks = reviews.stream().map(Review::getTask).collect(toList());

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetMemberTasks(tasks);
        reviewServiceMock.mockGetReviewsByTasks(reviews);

        SendMessage getTaskMessage = getMemberTasks.execute(update);

        assertEquals(3, ((InlineKeyboardMarkup) getTaskMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(GET_INFO + taskId1, ((InlineKeyboardMarkup) getTaskMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(GET_INFO + taskId2, ((InlineKeyboardMarkup) getTaskMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
        assertEquals(GET_INFO + taskId3, ((InlineKeyboardMarkup) getTaskMessage.getReplyMarkup()).getKeyboard().get(2).get(0).getCallbackData());
    }
}
