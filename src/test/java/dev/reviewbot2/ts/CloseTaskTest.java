package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.CloseTaskTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotAuthorException;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.stream.Stream;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskStatus.APPROVED;
import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.CLOSE;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
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
    void execute() {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(APPROVED);
        Member member = task.getAuthor();
        MessageInfo messageInfo = getSimpleMessageInfo(chatId, getCommand(CLOSE, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        processAccessorMock.mockCloseTask();

        SendMessage closeTaskMessage = closeTask.execute(messageInfo);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertNotNull(taskArgumentCaptor.getValue().getCloseTime());
        assertEquals("Задача закрыта", closeTaskMessage.getText());
    }

    @Test
    void execute_forceClose() {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(IN_REVIEW);
        Member member = task.getAuthor();
        List<Member> otherMembers = Stream.of(
            getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false),
            getMember(MEMBER_3_CHAT_ID, SECOND_REVIEW_GROUP, true, false)
        ).collect(toList());
        MessageInfo messageInfo = getSimpleMessageInfo(chatId, getCommand(CLOSE, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        processAccessorMock.mockCloseTask();
        memberServiceMock.mockGetAllMembers(otherMembers);

        SendMessage closeTaskMessage = closeTask.execute(messageInfo);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertNotNull(taskArgumentCaptor.getValue().getCloseTime());
        assertEquals("Задача принудительно закрыта", closeTaskMessage.getText());
    }

    @Test
    void execute_validationFailed() {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(APPROVED);
        Member member = getMember(MEMBER_2_CHAT_ID, FIRST_REVIEW_GROUP, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(chatId, getCommand(CLOSE, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        assertThrows(NotAuthorException.class, () -> closeTask.execute(messageInfo));
    }
}
