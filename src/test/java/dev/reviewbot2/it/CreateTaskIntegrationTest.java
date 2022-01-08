package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTaskIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createTask_withoutTaskType() throws Exception {
        Update update = getUpdateWithMessage(JIRA_LINK + TASK_NAME_1, MEMBER_CHAT_ID);
        Member member = getMemberFromDB(MEMBER_CHAT_ID, 0, false, false);

        SendMessage sendMessage = performUpdateReceived(update);

        assertEquals(member.getChatId(), sendMessage.getChatId());
        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + TASK_NAME_1 + "#" + IMPLEMENTATION,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("/" + TASK_NAME_1 + "#" + DESIGN,
            ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());

    }

    @Test
    void createTask() throws Exception {
        SendMessage sendMessage = performCreateTask(MEMBER_CHAT_ID, IMPLEMENTATION);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(READY_FOR_REVIEW, task.getStatus());
        assertEquals(MEMBER_CHAT_ID, sendMessage.getChatId());
    }

    @Test
    void createTask_manually() throws Exception {
        SendMessage sendMessage = performCreateTask(MEMBER_CHAT_ID, IMPLEMENTATION);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(READY_FOR_REVIEW, task.getStatus());
        assertEquals(MEMBER_CHAT_ID, sendMessage.getChatId());
    }
}
