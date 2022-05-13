package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.GetTaskInfoTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotAuthorException;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskStatus.IN_PROGRESS;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetTaskInfoTest extends AbstractUnitTest {
    private GetTaskInfoTransactionScript getTaskInfo;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        getTaskInfo = new GetTaskInfoTransactionScript(memberService, taskService);
        memberServiceMock = new MemberServiceMock(memberService);
        taskServiceMock = new TaskServiceMock(taskService);
    }

    @Test
    void happyPath() {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId = TASK_ID_1;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, taskId, memberChatId);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, getCommand(INFO, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage getInfoMessage = getTaskInfo.execute(messageInfo);

        assertEquals(1, ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(getCommand(CLOSE, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
    }

    @Test
    void happyPath_taskInProgress() {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId = TASK_ID_1;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, taskId, memberChatId);
        task.setStatus(IN_PROGRESS);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, getCommand(INFO, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage getInfoMessage = getTaskInfo.execute(messageInfo);

        assertEquals(2, ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(getCommand(SUBMIT, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(getCommand(CLOSE, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void happyPath_omniMember() {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId = TASK_ID_1;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, taskId, MEMBER_2_CHAT_ID);
        task.setStatus(IN_PROGRESS);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, getCommand(INFO, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage getInfoMessage = getTaskInfo.execute(messageInfo);

        assertEquals(1, ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(getCommand(CLOSE, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
    }

    @Test
    void getTaskInfo_error_notAuthor() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_2_CHAT_ID);
        task.setStatus(IN_PROGRESS);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, getCommand(INFO, task.getId()));

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        assertThrows(NotAuthorException.class, () -> getTaskInfo.execute(messageInfo));
    }
}
