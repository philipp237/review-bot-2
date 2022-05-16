package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotDesignReviewerException;
import dev.reviewbot2.exceptions.NotRequiredReviewGroupException;
import dev.reviewbot2.exceptions.TaskInReviewException;
import dev.reviewbot2.exceptions.NotRequiredTaskStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.utils.UpdateUtils.*;
import static java.time.Instant.now;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptReviewTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String text = messageInfo.getText();

        Member reviewer = memberService.getMemberByChatId(chatId);

        Long taskId = getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);
        Review review = reviewService.getReviewByTask(task);

        validateReviewer(reviewer, review, messageInfo);
        validateTaskStatus(messageInfo, reviewer, task);

        MemberReview memberReview = MemberReview.builder()
            .reviewer(reviewer)
            .review(review)
            .startTime(now())
            .build();

        List<MemberReview> memberReviews = new ArrayList<>();
        if (!isEmpty(review.getMemberReviews())) {
            memberReviews.addAll(review.getMemberReviews());
        }
        memberReviews.add(memberReview);
        review.setMemberReviews(memberReviews);

        reviewService.save(review);

        log.info("{} took task {} in review", reviewer.getLogin(), task.getName());
        processAccessor.takeInReview(task.getUuid());

        return sendMessage(chatId, String.format("Задача %s взята в ревью\n%s\n", task.getName(), task.getLink()));
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private Long getTaskIdFromText(String text) {
        String[] parsedText = text.split("#");
        return Long.parseLong(parsedText[parsedText.length - 1]);
    }

    private void validateReviewer(Member reviewer, Review review, MessageInfo messageInfo) {
        Task task = review.getTask();

        if (task.getTaskType() == DESIGN && !reviewer.isCanReviewDesign()) {
            log.info("{} unsuccessfully tries to take design task in review", reviewer.getLogin());
            throw new NotDesignReviewerException(messageInfo);
        } else if (task.getTaskType() != DESIGN && reviewer.getReviewGroup() != review.getReviewStage()) {
            log.info("{} unsuccessfully tries to take implementation task in review", reviewer.getLogin());
            throw new NotRequiredReviewGroupException(messageInfo);
        }
    }

    private void validateTaskStatus(MessageInfo messageInfo, Member reviewer, Task task) {
        if (task.getStatus().equals(IN_REVIEW)) {
            log.info("{} tries to take task with uuid={} in review, but it's already in review",
                reviewer.getLogin(), task.getUuid());
            throw new TaskInReviewException(messageInfo);
        } else if (!task.getStatus().equals(READY_FOR_REVIEW)) {
            log.info("{} tries to take task with uuid={} and status={} in review, but it's not ready for review",
                reviewer.getLogin(), task.getUuid(), task.getStatus());
            throw new NotRequiredTaskStatusException(messageInfo);
        }
    }
}
