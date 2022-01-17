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

import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TakeInReviewIntegrationTest extends AbstractIntegrationTest {

    @Test
    void takeInReview() throws Exception {
        TaskType taskType = IMPLEMENTATION;

        performCreateTask(MEMBER_CHAT_ID, taskType);

        String uuid = getUuidFromProcess();
        Task task = taskRepository.getByUuid(uuid);
        Long taskId = task.getId();

        performTakeInReview(REVIEWER_1_CHAT_ID, taskType, 1, taskId);
        SendMessage acceptReview = performAcceptReview(REVIEWER_1_CHAT_ID, taskId);

        task = taskRepository.getByUuid(uuid);
        Review review = reviewRepository.getReviewByTask(task);
        Member reviewer = memberRepository.getMemberByChatId(REVIEWER_1_CHAT_ID);
        List<MemberReview> memberReviews = memberReviewRepository.getAllByReviewAndReviewer(review, reviewer);

        assertEquals(TASK_NAME_1, task.getName());
        assertEquals(IN_REVIEW, task.getStatus());
        assertEquals(REVIEWER_1_CHAT_ID, acceptReview.getChatId());
        assertEquals(1, review.getReviewStage());
        assertEquals(1, memberReviews.size());
    }
}
