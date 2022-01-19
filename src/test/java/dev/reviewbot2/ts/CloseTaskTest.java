package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.CloseTaskTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.TaskServiceMock;
import dev.reviewbot2.mock.WebhookRestClientMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.CLOSE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CloseTaskTest extends AbstractUnitTest {
    private CloseTaskTransactionScript closeTask;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.closeTask = new CloseTaskTransactionScript(taskService, memberService, processAccessor, webhookRestClient);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
        this.webhookRestClientMock = new WebhookRestClientMock(webhookRestClient);
    }

    @Test
    void execute() throws TelegramApiException {
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1);
        task.setStatus(APPROVED);
        Member member = task.getAuthor();
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + task.getId(), MEMBER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        processAccessorMock.mockCloseTask();

        closeTask.execute(update);

        verify(taskService, Mockito.times(1)).save(taskArgumentCaptor.capture());

        assertEquals(CLOSED, taskArgumentCaptor.getValue().getStatus());
        assertNotNull(taskArgumentCaptor.getValue().getCloseTime());
    }

    @Test
    void execute_forceClose() throws TelegramApiException {
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1);
        task.setStatus(IN_REVIEW);
        Member member = task.getAuthor();
        List<Member> otherMembers = List.of(
            getMember(REVIEWER_1_CHAT_ID, 1, false, false),
            getMember(REVIEWER_2_CHAT_ID, 2, true, false)
        );
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + task.getId(), MEMBER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        processAccessorMock.mockCloseTask();
        webhookRestClientMock.mockSendMessage();
        memberServiceMock.mockGetAllMembers(otherMembers);

        closeTask.execute(update);

        verify(taskService, Mockito.times(1)).save(taskArgumentCaptor.capture());
        verify(webhookRestClient, times(otherMembers.size())).sendMessage(any());

        assertEquals(CLOSED, taskArgumentCaptor.getValue().getStatus());
        assertNotNull(taskArgumentCaptor.getValue().getCloseTime());
    }

    @Test
    void execute_validationFailed() {
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1);
        task.setStatus(APPROVED);
        Member member = getMember(REVIEWER_1_CHAT_ID, 1, false, false);
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + task.getId(), MEMBER_CHAT_ID);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        assertThrows(TelegramApiException.class, () -> closeTask.execute(update));
    }
}
