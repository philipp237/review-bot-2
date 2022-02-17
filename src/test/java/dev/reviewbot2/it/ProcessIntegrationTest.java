package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.domain.task.TaskType;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessIntegrationTest extends AbstractIntegrationTest {

    @Test
    void happyPath() throws Exception {
        String authorChatId = MEMBER_1_CHAT_ID;
        String firstStageReviewerChatId = MEMBER_2_CHAT_ID;
        String secondStageReviewerChatId = MEMBER_3_CHAT_ID;

        addMemberToDB(authorChatId, 1, false, false);
        addMemberToDB(firstStageReviewerChatId, 1, false, false);
        addMemberToDB(secondStageReviewerChatId, 2, true, false);

        String uuid = memberCreatesTask(authorChatId);
        reviewerTakesTaskInReview(firstStageReviewerChatId, uuid);
        reviewerDeclinesTask(firstStageReviewerChatId, uuid);
        authorSubmitsTaskForReview(authorChatId, uuid);
        reviewerTakesTaskInReview(firstStageReviewerChatId, uuid);
        reviewerApprovesTask(firstStageReviewerChatId, uuid);
        reviewerTakesTaskInReview(secondStageReviewerChatId, uuid);
        reviewerApprovesTask(secondStageReviewerChatId, uuid);
        authorClosesTask(authorChatId, uuid);
    }

    @Test
    void happyPath_forceClose() throws Exception {
        String authorChatId = MEMBER_1_CHAT_ID;
        String firstStageReviewerChatId = MEMBER_2_CHAT_ID;

        addMemberToDB(authorChatId, 1, false, false);
        addMemberToDB(firstStageReviewerChatId, 1, false, false);

        String uuid = memberCreatesTask(authorChatId);
        reviewerTakesTaskInReview(firstStageReviewerChatId, uuid);
        authorClosesTask(authorChatId, uuid);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private String memberCreatesTask(String authorChatId) throws Exception {
        performCreateTask(authorChatId, TaskType.IMPLEMENTATION);
        String uuid = getUuidFromProcess();
        Thread.sleep(100);
        assertEquals(READY_FOR_REVIEW, getTask(uuid).getStatus());

        return uuid;
    }

    private void reviewerTakesTaskInReview(String reviewerChatId, String uuid) throws Exception {
        performAcceptReview(reviewerChatId, TASK_ID_1);
        Thread.sleep(100);
        assertEquals(IN_REVIEW, getTask(uuid).getStatus());
    }

    private void reviewerDeclinesTask(String reviewerChatId, String uuid) throws Exception {
        performDecline(reviewerChatId, TASK_ID_1);
        Thread.sleep(100);
        assertEquals(IN_PROGRESS, getTask(uuid).getStatus());
    }

    private void authorSubmitsTaskForReview(String authorChatId, String uuid) throws Exception {
        performSubmit(authorChatId, TASK_ID_1);
        Thread.sleep(100);
        assertEquals(READY_FOR_REVIEW, getTask(uuid).getStatus());
    }

    private void reviewerApprovesTask(String reviewerChatId, String uuid) throws Exception {
        performApprove(reviewerChatId, TASK_ID_1);

        Member reviewer = getMemberFromDB(reviewerChatId);
        TaskStatus expectedStatus;
        if (reviewer.getReviewGroup() == 1) {
            expectedStatus = READY_FOR_REVIEW;
        } else if (reviewer.getReviewGroup() == 2) {
            expectedStatus = APPROVED;
        } else {
            throw new TelegramApiException();
        }
        Thread.sleep(100);

        assertEquals(expectedStatus, getTask(uuid).getStatus());
    }

    private void authorClosesTask(String authorChatId, String uuid) throws Exception {
        Task task = getTask(uuid);
        TaskStatus expectedStatus;
        if (task.getStatus().equals(APPROVED)) {
            expectedStatus = CLOSED;
        } else {
            expectedStatus = FORCE_CLOSED;
        }

        performClose(authorChatId, TASK_ID_1);
        Thread.sleep(100);
        assertEquals(expectedStatus, getTask(uuid).getStatus());
    }

    private Task getTask(String uuid) {
        return taskRepository.getByUuid(uuid);
    }
}
