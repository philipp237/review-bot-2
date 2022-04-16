package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.GetTaskInfoTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotAuthorException;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    void happyPath() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId = TASK_ID_1;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, taskId, memberChatId);

        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, INFO, task.getId()), memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage getInfoMessage = getTaskInfo.execute(update);

        assertEquals(1, ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(String.format(COMMAND, CLOSE, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
    }

    @Test
    void happyPath_taskInProgress() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId = TASK_ID_1;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, taskId, memberChatId);
        task.setStatus(IN_PROGRESS);

        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, INFO, task.getId()), memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage getInfoMessage = getTaskInfo.execute(update);

        assertEquals(2, ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(String.format(COMMAND, SUBMIT, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals(String.format(COMMAND, CLOSE, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void happyPath_omniMember() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        long taskId = TASK_ID_1;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, taskId, MEMBER_2_CHAT_ID);
        task.setStatus(IN_PROGRESS);

        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, INFO, task.getId()), memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        SendMessage getInfoMessage = getTaskInfo.execute(update);

        assertEquals(1, ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals(String.format(COMMAND, CLOSE, taskId),
            ((InlineKeyboardMarkup) getInfoMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
    }

    @Test
    void getTaskInfo_error_notAuthor() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_2_CHAT_ID);
        task.setStatus(IN_PROGRESS);

        Update update = getUpdateWithCallbackQuery(String.format(COMMAND, INFO, task.getId()), memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);
        taskServiceMock.mockGetTaskById(task);

        assertThrows(NotAuthorException.class, () -> getTaskInfo.execute(update));
    }
}
