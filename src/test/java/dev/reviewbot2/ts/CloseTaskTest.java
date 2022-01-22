package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.CloseTaskTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.APPROVED;
import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.CLOSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class CloseTaskTest extends AbstractUnitTest {
    private CloseTaskTransactionScript closeTask;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        this.closeTask = new CloseTaskTransactionScript(taskService, memberService, processAccessor);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() throws TelegramApiException {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(APPROVED);
        Member member = task.getAuthor();
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + task.getId(), chatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        processAccessorMock.mockCloseTask();

        SendMessage closeTaskMessage = closeTask.execute(update);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertNotNull(taskArgumentCaptor.getValue().getCloseTime());
        assertEquals("Задача закрыта", closeTaskMessage.getText());
    }

    @Test
    void execute_forceClose() throws TelegramApiException {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(IN_REVIEW);
        Member member = task.getAuthor();
        List<Member> otherMembers = List.of(
            getMember(MEMBER_2_CHAT_ID, 1, false, false),
            getMember(MEMBER_3_CHAT_ID, 2, true, false)
        );
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + task.getId(), chatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        processAccessorMock.mockCloseTask();
        memberServiceMock.mockGetAllMembers(otherMembers);

        SendMessage closeTaskMessage = closeTask.execute(update);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertNotNull(taskArgumentCaptor.getValue().getCloseTime());
        assertEquals("Задача принудительно закрыта", closeTaskMessage.getText());
    }

    @Test
    void execute_validationFailed() throws TelegramApiException {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(APPROVED);
        Member member = getMember(MEMBER_2_CHAT_ID, 1, false, false);
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + task.getId(), chatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage notAuthorMessage = closeTask.execute(update);
        assertEquals("Ты не можешь закрыть задачу, которую не заводил", notAuthorMessage.getText());
    }
}
