package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.SubmitForReviewTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotAuthorException;
import dev.reviewbot2.exceptions.NotRequiredTaskStatusException;
import dev.reviewbot2.mock.MemberServiceMock;
import dev.reviewbot2.mock.ProcessAccessorMock;
import dev.reviewbot2.mock.TaskServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.SUBMIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class SubmitForReviewTest extends AbstractUnitTest {
    private SubmitForReviewTransactionScript submitForReview;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        this.submitForReview = new SubmitForReviewTransactionScript(taskService, memberService, processAccessor);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(IN_PROGRESS);
        Member member = task.getAuthor();
        MessageInfo messageInfo = getSimpleMessageInfo(chatId, getCommand(SUBMIT, task.getId()));

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        SendMessage submitForReviewMessage = submitForReview.execute(messageInfo);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertEquals(READY_FOR_REVIEW, taskArgumentCaptor.getValue().getStatus());
        assertEquals("Задача отправлена на ревью", submitForReviewMessage.getText());
    }

    @Test
    void execute_validationError_notAuthor() {
        String chatId = MEMBER_2_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        task.setStatus(IN_PROGRESS);
        Member member = getMember(chatId, NON_REVIEWER, false, false);
        MessageInfo messageInfo = getSimpleMessageInfo(chatId, getCommand(SUBMIT, task.getId()));

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        assertThrows(NotAuthorException.class, () -> submitForReview.execute(messageInfo));
    }

    @Test
    void execute_validationError_invalidStatus() {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, BF, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(IN_REVIEW);
        Member member = task.getAuthor();
        MessageInfo messageInfo = getSimpleMessageInfo(chatId, getCommand(SUBMIT, task.getId()));

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        assertThrows(NotRequiredTaskStatusException.class, () -> submitForReview.execute(messageInfo));
    }
}
