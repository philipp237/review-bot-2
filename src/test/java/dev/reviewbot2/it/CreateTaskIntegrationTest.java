package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTaskIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createTask_withoutTaskType() throws Exception {
        Update update = getUpdateWithMessage(TASK_LINK);
        Member member = getMemberFromDB(0, false, false);

        SendMessage sendMessage = performUpdateReceived(update);

        assertEquals(member.getChatId(), sendMessage.getChatId());
        assertEquals(2, ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().size());
    }

    @Test
    void createTask() throws Exception {
        Update update = getUpdateWithMessage(TASK_LINK + "#" + TaskType.IMPLEMENTATION);
        Member member = getMemberFromDB(0, false, false);

        SendMessage sendMessage = performUpdateReceived(update);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);

        assertEquals(TASK_NAME, task.getName());
        assertEquals(member.getChatId(), sendMessage.getChatId());
    }
}
