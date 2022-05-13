package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.GetTasksReadyForIncorporationTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.stream.Stream;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskSegment.DEFECT;
import static dev.reviewbot2.domain.task.TaskStatus.CLOSED;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.CLOSED_TASKS;
import static dev.reviewbot2.processor.Command.INCORPORATE;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetTasksReadyForIncorporationTest extends AbstractUnitTest {
    private GetTasksReadyForIncorporationTransactionScript getTasksReadyForIncorporation;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        getTasksReadyForIncorporation = new GetTasksReadyForIncorporationTransactionScript(taskService, memberService);
        taskServiceMock = new TaskServiceMock(taskService);
        memberServiceMock = new MemberServiceMock(memberService);
    }

    @Test
    void happyPath() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);

        long taskId1 = TASK_ID_1;
        long taskId2 = TASK_ID_2;
        List<Review> reviews = Stream.of(
            getReview(IMPLEMENTATION, BF, FIRST_REVIEW_GROUP, UUID_1, TASK_NAME_1, taskId1, memberChatId),
            getReview(IMPLEMENTATION, DEFECT, FIRST_REVIEW_GROUP, UUID_2, TASK_NAME_2, taskId2, memberChatId)
        ).collect(toList());

        List<Task> tasks = reviews.stream().map(Review::getTask)
            .collect(toList());
        tasks.forEach(task -> task.setStatus(CLOSED));

        String updateMessageText = "/" + CLOSED_TASKS;

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, updateMessageText);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetClosedTasks(tasks);

        SendMessage closedTasks = getTasksReadyForIncorporation.execute(messageInfo);

        List<List<InlineKeyboardButton>> keyboard = ((InlineKeyboardMarkup) closedTasks.getReplyMarkup()).getKeyboard();
        String expectedText = String.format("Закрытые задачи (нажми на задачу, чтобы ее закрыть):\n%-16s%8s%n%-16s%8s",
            TASK_NAME_2, DEFECT.getText(), TASK_NAME_1, BF.getText()).trim();
        assertEquals(expectedText, closedTasks.getText());
        assertEquals(3, keyboard.size());
        assertEquals(getCommand(INCORPORATE, taskId2), keyboard.get(0).get(0).getCallbackData());
        assertEquals(getCommand(INCORPORATE, taskId1), keyboard.get(1).get(0).getCallbackData());
        assertEquals("/" + INCORPORATE, keyboard.get(2).get(0).getCallbackData());
    }
}
