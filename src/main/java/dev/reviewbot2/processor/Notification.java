package dev.reviewbot2.processor;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.adapter.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static dev.reviewbot2.utils.UpdateUtils.sendMessage;
import static java.util.stream.Collectors.toList;

// TODO Нецелевое решение, в дальнейшем будет переработано для возможности гибкой настройки через переменные окружения
@Slf4j
@Component
@RequiredArgsConstructor
public class Notification {
    private static final String NOTIFY_CRON = "0 30 10,13,16 ? * 1-5";
    public static final String MESSAGE_TITLE_FOR_IMPLEMENTATIONS = "Следующие задачи ожидают ревью:\n";
    public static final String MESSAGE_TITLE_FOR_DESIGNS = "Следующие дизайны ожидают ревью:\n";

    @Value("${settings.number-of-review-stages}")
    private int numberOfReviewStages;

    private final MemberService memberService;
    private final ReviewService reviewService;
    private final WebhookRestClient webhookRestClient;

    @Scheduled(cron = NOTIFY_CRON)
    private void notifyReviewers() {
        List<Member> allMembers = memberService.getAllMembers();

        List<Review> allReviews = reviewService.getAllReviewsForTaskReadyForReview();

        List<Review> implementationReviews = allReviews.stream()
            .filter(review -> IMPLEMENTATION.equals(review.getTask().getTaskType()))
            .collect(toList());
        List<Review> designReviews = allReviews.stream()
            .filter(review -> DESIGN.equals(review.getTask().getTaskType()))
            .collect(toList());

        StringBuilder messageBuilder;
        String message;

        for (int i = 1; i <= numberOfReviewStages; i++) {
            messageBuilder = new StringBuilder(MESSAGE_TITLE_FOR_IMPLEMENTATIONS);

            for (Review implementationReview : implementationReviews) {
                if (implementationReview.getReviewStage() == i) {
                    messageBuilder.append(implementationReview.getTask().getName()).append("\n");
                }
            }

            message = messageBuilder.toString();

            for (Member member : allMembers) {
                if (member.getReviewGroup() == i || member.getIsOmni()) {
                    webhookRestClient.sendMessage(sendMessage(member.getChatId(), message));
                }
            }
        }

        messageBuilder = new StringBuilder(MESSAGE_TITLE_FOR_DESIGNS);
        for (Review designReview : designReviews) {
            messageBuilder.append(designReview.getTask().getName()).append("\n");
        }

        message = messageBuilder.toString();

        for (Member member : allMembers) {
            if (member.isCanReviewDesign() || member.getIsOmni()) {
                webhookRestClient.sendMessage(sendMessage(member.getChatId(), message));
            }
        }
    }
}
