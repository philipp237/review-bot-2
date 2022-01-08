package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberReviewService;
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

import static dev.reviewbot2.processor.Utils.*;
import static java.time.Instant.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptReviewTransactionScript {
    private final String TASK_TAKEN_IN_REIVEW = "Задача %s взята в ревью\n%s\n";

    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;
    private final MemberReviewService memberReviewService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String text = getTextFromUpdate(update);

        Member reviewer = memberService.getMemberByChatId(chatId);

        Long taskId = getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);
        Review review = reviewService.getReviewByTask(task);

        MemberReview memberReview = MemberReview.builder()
            .reviewer(reviewer)
            .review(review)
            .startTime(now())
            .build();

        memberReviewService.save(memberReview);

        log.info("{} took task {} in review", reviewer.getLogin(), task.getName());
        processAccessor.takeInReview(task.getUuid());

        return sendMessage(chatId, String.format(TASK_TAKEN_IN_REIVEW, task.getName(), task.getLink()));
    }

    private Long getTaskIdFromText(String text) {
        String[] parsedText = text.split("#");
        return Long.parseLong(parsedText[parsedText.length - 1]);
    }
}
