package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.transaction.Transactional;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.IN_REVIEW;
import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.processor.Utils.*;
import static java.time.Instant.now;
import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptReviewTransactionScript {
    private final String TASK_TAKEN_IN_REIVEW = "Задача %s взята в ревью\n%s\n";

    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String text = getTextFromUpdate(update);

        Member reviewer = memberService.getMemberByChatId(chatId);

        Long taskId = getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);
        Review review = reviewService.getReviewByTask(task);

        if (task.getTaskType() == DESIGN && !reviewer.isCanReviewDesign()) {
            log.info("{} unsuccessfully tries to take design task in review", reviewer.getLogin());
            return sendMessage(chatId, "Ты не можешь ревьюить дизайны");
        } else if (task.getTaskType() != DESIGN && reviewer.getReviewGroup() != review.getReviewStage()) {
            log.info("{} unsuccessfully tries to take implementation task in review", reviewer.getLogin());
            return sendMessage(chatId, "На данной стадии ты не можешь взять задачу в ревью");
        }

        if (task.getStatus().equals(IN_REVIEW)) {
            log.info("{} tries to take task with uuid={} in review, but it's already in review",
                reviewer.getLogin(), task.getUuid());
            return sendMessage(chatId,"Кто-то успел взять задачу на ревью раньше тебя ¯\\_(ツ)_/¯");
        } else if (!task.getStatus().equals(READY_FOR_REVIEW)) {
            log.info("{} tries to take task with uuid={} and status={} in review, but it's not ready for review",
                reviewer.getLogin(), task.getUuid(), task.getStatus());
            return sendMessage(chatId,"Задачу нельзя взять в ревью");
        }

        MemberReview memberReview = MemberReview.builder()
            .reviewer(reviewer)
            .review(review)
            .startTime(now())
            .build();

        List<MemberReview> memberReviews;
        memberReviews = review.getMemberReviews();

        if (!isEmpty(memberReviews)) {
            memberReviews.add(memberReview);
            review.setMemberReviews(memberReviews);
        } else {
            review.setMemberReviews(singletonList(memberReview));
        }

        reviewService.save(review);

        log.info("{} took task {} in review", reviewer.getLogin(), task.getName());
        processAccessor.takeInReview(task.getUuid());

        return sendMessage(chatId, String.format(TASK_TAKEN_IN_REIVEW, task.getName(), task.getLink()));
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private Long getTaskIdFromText(String text) {
        String[] parsedText = text.split("#");
        return Long.parseLong(parsedText[parsedText.length - 1]);
    }
}
