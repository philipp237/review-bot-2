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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.SUBMIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SubmitForReviewTest extends AbstractUnitTest {
    private SubmitForReviewTransactionScript submitForReview;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.submitForReview = new SubmitForReviewTransactionScript(taskService, memberService, processAccessor);
        this.taskServiceMock = new TaskServiceMock(taskService);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.processAccessorMock = new ProcessAccessorMock(processAccessor);
    }

    @Test
    void execute() throws TelegramApiException {
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1);
        task.setStatus(IN_PROGRESS);
        Member member = task.getAuthor();
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + task.getId(), MEMBER_CHAT_ID);

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        submitForReview.execute(update);

        verify(taskService, times(1)).save(taskArgumentCaptor.capture());

        assertEquals(READY_FOR_REVIEW, taskArgumentCaptor.getValue().getStatus());
    }

    @Test
    void execute_validationError_notAuthor() {
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1);
        task.setStatus(IN_PROGRESS);
        Member member = getMember(REVIEWER_1_CHAT_ID, 0, false, false);
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + task.getId(), REVIEWER_1_CHAT_ID);

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        assertThrows(TelegramApiException.class, () -> submitForReview.execute(update));
    }

    @Test
    void execute_validationError_invalidStatus() {
        Task task = getTask(IMPLEMENTATION, UUID_1, TASK_NAME_1, TASK_ID_1);
        task.setStatus(IN_REVIEW);
        Member member = task.getAuthor();
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + task.getId(), MEMBER_CHAT_ID);

        taskServiceMock.mockGetTaskById(task);
        taskServiceMock.mockSave(task);
        memberServiceMock.mockGetMemberByChatId(member);
        processAccessorMock.mockSubmitForReview();

        assertThrows(TelegramApiException.class, () -> submitForReview.execute(update));
    }
}
