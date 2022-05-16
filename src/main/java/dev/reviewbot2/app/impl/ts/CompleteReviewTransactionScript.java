package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberReviewService;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotRequiredTaskStatusException;
import dev.reviewbot2.exceptions.NotSameReviewerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.transaction.Transactional;

import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.utils.UpdateUtils.*;
import static java.time.Instant.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteReviewTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;
    private final MemberReviewService memberReviewService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo, boolean isApproved) {
        String text = messageInfo.getText();
        String chatId = messageInfo.getChatId();

        Long taskId = getTaskIdFromText(text);

        Member reviewer = memberService.getMemberByChatId(chatId);
        Task task = taskService.getTaskById(taskId);
        Review review = reviewService.getReviewByTask(task);
        MemberReview memberReview = memberReviewService.getActiveReview(review);

        validateStatus(messageInfo, reviewer, review);
        validateReviewer(messageInfo, reviewer, review, memberReview);

        memberReview.setEndTime(now());
        memberReviewService.save(memberReview);

        log.info("Review with id={} was completed by {} with resolution={}",
            review.getId(), reviewer.getLogin(), isApproved ? "approved" : "declined");
        processAccessor.completeReview(task.getUuid(), isApproved);

        return sendMessage(chatId, getEndReviewMessage(isApproved));
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateStatus(MessageInfo messageInfo, Member reviewer, Review review) {
        if (isStatusInvalid(review)) {
            log.info("Member {} tries to complete review with id={}, but task not in review",
                reviewer.getLogin(), review.getId());
            throw new NotRequiredTaskStatusException(messageInfo);
        }
    }

    private void validateReviewer(MessageInfo messageInfo, Member reviewer, Review review, MemberReview memberReview) {
        if (isSameReviewer(reviewer, memberReview)) {
            log.info("Member {} tries to complete review with id={} authored by {}",
                reviewer.getLogin(), review.getId(), memberReview.getReviewer().getLogin());
            throw new NotSameReviewerException(messageInfo);
        }
    }

    private boolean isSameReviewer(Member reviewer, MemberReview memberReview) {
        return !reviewer.equals(memberReview.getReviewer());
    }

    private boolean isStatusInvalid(Review review) {
        return !review.getTask().getStatus().equals(IN_REVIEW);
    }

    private String getEndReviewMessage(boolean isApproved) {
        return isApproved ? "Задача одобрена" : "Задача возвращена на доработку";
    }
}
