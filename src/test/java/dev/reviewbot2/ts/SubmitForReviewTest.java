package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.SubmitForReviewTransactionScript;
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

import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.SUBMIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void execute() throws TelegramApiException {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(IN_PROGRESS);
        Member member = task.getAuthor();
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + task.getId(), chatId);

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        SendMessage submitForReviewMessage = submitForReview.execute(update);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertEquals(READY_FOR_REVIEW, taskArgumentCaptor.getValue().getStatus());
        assertEquals("Задача отправлена на ревью", submitForReviewMessage.getText());
    }

    @Test
    void execute_validationError_notAuthor() throws TelegramApiException {
        String chatId = MEMBER_2_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, MEMBER_1_CHAT_ID);
        task.setStatus(IN_PROGRESS);
        Member member = getMember(chatId, 0, false, false);
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + task.getId(), chatId);

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        SendMessage notAuthorMessage = submitForReview.execute(update);
        assertEquals("Ты не можешь отправить на ревью задачу, которую не создавал", notAuthorMessage.getText());
    }

    @Test
    void execute_validationError_invalidStatus() throws TelegramApiException {
        String chatId = MEMBER_1_CHAT_ID;
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1, chatId);
        task.setStatus(IN_REVIEW);
        Member member = task.getAuthor();
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + task.getId(), chatId);

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        SendMessage invalidStatusMessage = submitForReview.execute(update);
        assertEquals("Задача не на доработке, ее нельзя отправить в ревью", invalidStatusMessage.getText());
    }
}
