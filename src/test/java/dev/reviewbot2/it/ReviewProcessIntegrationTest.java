package dev.reviewbot2.it;

import dev.reviewbot2.AbstractIntegrationTest;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskType;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.*;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReviewProcessIntegrationTest extends AbstractIntegrationTest {

    @Test
    void approveReview() throws Exception {
        TaskType taskType = IMPLEMENTATION;

        performCreateTask(MEMBER_CHAT_ID, taskType);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);
        Long taskId = task.getId();

        performTakeInReview(REVIEWER_1_CHAT_ID, taskType, 1, taskId);
        performAcceptReview(REVIEWER_1_CHAT_ID, taskId);
        SendMessage approveReview = performApprove(REVIEWER_1_CHAT_ID, taskId);

        Thread.sleep(100);

        task = taskRepository.getByUuid(uuid);
        Review review = reviewRepository.getReviewByTask(task);
        Member reviewer = memberRepository.getMemberByChatId(REVIEWER_1_CHAT_ID);
        List<MemberReview> memberReviews = memberReviewRepository.getAllByReviewAndReviewer(review, reviewer);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(READY_FOR_REVIEW, task.getStatus());
        assertEquals(REVIEWER_1_CHAT_ID, approveReview.getChatId());
        assertEquals(2, review.getReviewStage());
        assertEquals(1, memberReviews.size());

        performTakeInReview(REVIEWER_2_CHAT_ID, taskType, 2, taskId);
        performAcceptReview(REVIEWER_2_CHAT_ID, taskId);
        approveReview = performApprove(REVIEWER_2_CHAT_ID, taskId);

        task = taskRepository.getByUuid(uuid);
        review = reviewRepository.getReviewByTask(task);
        reviewer = memberRepository.getMemberByChatId(REVIEWER_2_CHAT_ID);
        memberReviews = memberReviewRepository.getAllByReviewAndReviewer(review, reviewer);

        assertEquals(APPROVED, task.getStatus());
        assertEquals(REVIEWER_2_CHAT_ID, approveReview.getChatId());
        assertEquals(2, review.getReviewStage());
        assertEquals(1, memberReviews.size());

        for (MemberReview memberReview : memberReviews) {
            assertNotNull(memberReview.getEndTime());
        }
    }

    @Test
    void declineAndResubmitReview() throws Exception {
        TaskType taskType = IMPLEMENTATION;

        performCreateTask(MEMBER_CHAT_ID, taskType);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);
        Long taskId = task.getId();

        performTakeInReview(REVIEWER_1_CHAT_ID, taskType, 1, taskId);
        performAcceptReview(REVIEWER_1_CHAT_ID, taskId);
        SendMessage declineReview = performDecline(REVIEWER_1_CHAT_ID, taskId);

        Thread.sleep(100);

        task = taskRepository.getByUuid(uuid);
        Review review = reviewRepository.getReviewByTask(task);
        Member reviewer = memberRepository.getMemberByChatId(REVIEWER_1_CHAT_ID);
        List<MemberReview> memberReviews = memberReviewRepository.getAllByReviewAndReviewer(review, reviewer);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(IN_PROGRESS, task.getStatus());
        assertEquals(REVIEWER_1_CHAT_ID, declineReview.getChatId());
        assertEquals(1, review.getReviewStage());
        assertEquals(1, memberReviews.size());

        performSubmit(MEMBER_CHAT_ID, taskId);

        task = taskRepository.getByUuid(uuid);

        assertEquals(READY_FOR_REVIEW, task.getStatus());

        Thread.sleep(100);

        performTakeInReview(REVIEWER_1_CHAT_ID, taskType, 1, taskId);
        performAcceptReview(REVIEWER_1_CHAT_ID, taskId);

        task = taskRepository.getByUuid(uuid);

        assertEquals(IN_REVIEW, task.getStatus());
    }
}
