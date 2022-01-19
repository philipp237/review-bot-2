package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CloseTaskIntegrationTest extends AbstractIntegrationTest {

    @Test
    void closeTask() throws Exception {
        TaskType taskType = IMPLEMENTATION;

        performCreateTask(MEMBER_CHAT_ID, taskType);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);
        Long taskId = task.getId();

        performTakeInReview(REVIEWER_1_CHAT_ID, taskType, 1, taskId);
        performAcceptReview(REVIEWER_1_CHAT_ID, taskId);
        performApprove(REVIEWER_1_CHAT_ID, taskId);

        Thread.sleep(100);

        performTakeInReview(REVIEWER_2_CHAT_ID, taskType, 2, taskId);
        performAcceptReview(REVIEWER_2_CHAT_ID, taskId);
        performApprove(REVIEWER_2_CHAT_ID, taskId);

        Thread.sleep(100);

        performClose(MEMBER_CHAT_ID, taskId);

        task = taskRepository.getByUuid(uuid);
        assertEquals(CLOSED, task.getStatus());
    }

    @Test
    void forceCloseTask() throws Exception {
        TaskType taskType = IMPLEMENTATION;

        performCreateTask(MEMBER_CHAT_ID, taskType);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);
        Long taskId = task.getId();

        performTakeInReview(REVIEWER_1_CHAT_ID, taskType, 1, taskId);
        performAcceptReview(REVIEWER_1_CHAT_ID, taskId);
        performApprove(REVIEWER_1_CHAT_ID, taskId);

        Thread.sleep(100);

        task = taskRepository.getByUuid(uuid);
        assertEquals(READY_FOR_REVIEW, task.getStatus());

        performClose(MEMBER_CHAT_ID, taskId);

        task = taskRepository.getByUuid(uuid);
        assertEquals(FORCE_CLOSED, task.getStatus());
    }
}
