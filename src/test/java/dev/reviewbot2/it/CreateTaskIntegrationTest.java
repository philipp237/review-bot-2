package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTaskIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createTask_withoutTaskType() throws Exception {
        Update update = getUpdateWithMessage(JIRA_LINK + TASK_NAME_1);
        Member member = getMemberFromDB(0, false, false);

        SendMessage sendMessage = performUpdateReceived(update);

        assertEquals(member.getChatId(), sendMessage.getChatId());
        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
    }

    @Test
    void createTask() throws Exception {
        Update update = getUpdateWithCallbackQuery(JIRA_LINK + TASK_NAME_1 + "#" + IMPLEMENTATION);
        Member member = getMemberFromDB(0, false, false);

        SendMessage sendMessage = performUpdateReceived(update);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(member.getChatId(), sendMessage.getChatId());
    }

    @Test
    void createTask_manually() throws Exception {
        Update update = getUpdateWithMessage(JIRA_LINK + TASK_NAME_1 + "#" + IMPLEMENTATION);
        Member member = getMemberFromDB(0, false, false);

        SendMessage sendMessage = performUpdateReceived(update);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(member.getChatId(), sendMessage.getChatId());
    }
}
