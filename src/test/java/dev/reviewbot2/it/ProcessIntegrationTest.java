package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.exceptions.ReviewBotExceptionHandler;
import dev.reviewbot2.utils.DateUtils;
import dev.reviewbot2.utils.UpdateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static dev.reviewbot2.domain.task.TaskSegment.BF;
import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static dev.reviewbot2.processor.Command.SPRINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProcessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    ReviewBotExceptionHandler reviewBotExceptionHandler;

    @Test
    void happyPath() throws Exception {
        String authorChatId = MEMBER_1_CHAT_ID;
        String firstStageReviewerChatId = MEMBER_2_CHAT_ID;
        String secondStageReviewerChatId = MEMBER_3_CHAT_ID;
        String omniChatId = MEMBER_4_CHAT_ID;

        addMemberToDB(authorChatId, 1, false, false);
        addMemberToDB(firstStageReviewerChatId, 1, false, false);
        addMemberToDB(secondStageReviewerChatId, 2, true, false);
        addMemberToDB(omniChatId, 2, true, true);

        String uuid = memberCreatesTask(authorChatId, IMPLEMENTATION);
        reviewerTakesTaskInReview(firstStageReviewerChatId, uuid);
        reviewerDeclinesTask(firstStageReviewerChatId, uuid);
        authorSubmitsTaskForReview(authorChatId, uuid);
        reviewerTakesTaskInReview(firstStageReviewerChatId, uuid);
        reviewerApprovesTask(firstStageReviewerChatId, uuid);
        reviewerTakesTaskInReview(secondStageReviewerChatId, uuid);
        reviewerApprovesTask(secondStageReviewerChatId, uuid);
        authorClosesTask(authorChatId, uuid);
        omniSendsTaskInProduction(omniChatId, uuid);
    }

    @Test
    void happyPath_design() throws Exception {
        String authorChatId = MEMBER_1_CHAT_ID;
        String designReviewerChatId = MEMBER_2_CHAT_ID;
        String omniChatId = MEMBER_4_CHAT_ID;

        addMemberToDB(authorChatId, 1, false, false);
        addMemberToDB(designReviewerChatId, 2, true, false);
        addMemberToDB(omniChatId, 2, true, true);

        String uuid = memberCreatesTask(authorChatId, DESIGN);
        reviewerTakesTaskInReview(designReviewerChatId, uuid);
        reviewerDeclinesTask(designReviewerChatId, uuid);
        authorSubmitsTaskForReview(authorChatId, uuid);
        reviewerTakesTaskInReview(designReviewerChatId, uuid);
        reviewerApprovesTask(designReviewerChatId, uuid);
        authorClosesTask(authorChatId, uuid);
        omniSendsTaskInProduction(omniChatId, uuid);
    }

    @Test
    void happyPath_forceClose() throws Exception {
        String authorChatId = MEMBER_1_CHAT_ID;
        String firstStageReviewerChatId = MEMBER_2_CHAT_ID;
        String omniChatId = MEMBER_4_CHAT_ID;

        addMemberToDB(authorChatId, 1, false, false);
        addMemberToDB(firstStageReviewerChatId, 1, false, false);
        addMemberToDB(omniChatId, 2, true, true);

        String uuid = memberCreatesTask(authorChatId, IMPLEMENTATION);
        reviewerTakesTaskInReview(firstStageReviewerChatId, uuid);
        authorClosesTask(authorChatId, uuid);
        omniSendsTaskInProduction(omniChatId, uuid);
    }

    @Test
    void error_notReviewerTriesReview() throws Exception {
        String authorChatId = MEMBER_1_CHAT_ID;
        String notReviewerChatId = MEMBER_2_CHAT_ID;

        addMemberToDB(authorChatId, 1, false, false);
        addMemberToDB(notReviewerChatId, 0, false, false);

        String uuid = memberCreatesTask(authorChatId, IMPLEMENTATION);

        Long taskId = getTask(uuid).getId();
        Update update = getUpdateWithCallbackQuery(getCommand(ACCEPT_REVIEW, taskId), notReviewerChatId);
        SendMessage sendMessage = performWithAnswer(update);

        assertEquals("Ты не можешь взять эту задачу в ревью, ты не в той группе ревью", sendMessage.getText());
    }

    @Test
    void getSprintValue() throws Exception {
        String memberChatId = MEMBER_1_CHAT_ID;

        addMemberToDB(memberChatId, 1, false, false);

        mockGetTodayDate();
        Update update = getUpdateWithCallbackQuery("/" + SPRINT, memberChatId);
        SendMessage springValueMessage = performWithAnswer(update);

        assertEquals("Сейчас идёт спринт 2.3 (52)", springValueMessage.getText());
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private String memberCreatesTask(String authorChatId, TaskType taskType) throws Exception {
        performCreateTask(authorChatId, BF, taskType);
        String uuid = getUuidFromProcess();
        Thread.sleep(100);
        assertEquals(READY_FOR_REVIEW, getTask(uuid).getStatus());

        return uuid;
    }

    private void reviewerTakesTaskInReview(String reviewerChatId, String uuid) throws Exception {
        Long taskId = getTask(uuid).getId();
        performAcceptReview(reviewerChatId, taskId);
        Thread.sleep(100);
        assertEquals(IN_REVIEW, getTask(uuid).getStatus());
    }

    private void reviewerDeclinesTask(String reviewerChatId, String uuid) throws Exception {
        Long taskId = getTask(uuid).getId();
        performDecline(reviewerChatId, taskId);
        Thread.sleep(100);
        assertEquals(IN_PROGRESS, getTask(uuid).getStatus());
    }

    private void authorSubmitsTaskForReview(String authorChatId, String uuid) throws Exception {
        Long taskId = getTask(uuid).getId();
        performSubmit(authorChatId, taskId);
        Thread.sleep(100);
        assertEquals(READY_FOR_REVIEW, getTask(uuid).getStatus());
    }

    private void reviewerApprovesTask(String reviewerChatId, String uuid) throws Exception {
        Long taskId = getTask(uuid).getId();
        performApprove(reviewerChatId, taskId);

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

        performClose(authorChatId, task.getId());
        Thread.sleep(100);
        assertEquals(expectedStatus, getTask(uuid).getStatus());
    }

    private void omniSendsTaskInProduction(String omniChatId, String uuid) throws Exception {
        Long taskId = getTask(uuid).getId();
        performSendingToProduction(omniChatId, taskId);
        Thread.sleep(100);
        assertEquals(IN_PRODUCTION, getTask(uuid).getStatus());
    }

    private Task getTask(String uuid) {
        return taskRepository.getByUuid(uuid);
    }

    private void mockGetTodayDate() {
        mockStatic(DateUtils.class)
            .when(DateUtils::getTodayDate)
            .thenReturn(LocalDate.of(2022, 5, 16));
    }

    private SendMessage performWithAnswer(Update update) throws Exception {
        MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), SendMessage.class);
    }
}
