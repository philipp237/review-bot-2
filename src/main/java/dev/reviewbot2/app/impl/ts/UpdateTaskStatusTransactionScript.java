package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.adapter.WebhookRestClient;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.transaction.Transactional;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.getTaskStatusFromActivityId;
import static dev.reviewbot2.utils.UpdateUtils.sendMessage;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateTaskStatusTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;
    private final WebhookRestClient webhookRestClient;

    @Transactional
    public void execute(DelegateExecution execution, String taskUuid) throws TelegramApiException {
        Task task = taskService.getTaskByUuid(taskUuid);
        Review review = reviewService.getReviewByTask(task);

        TaskStatus status = getTaskStatusFromActivityId(execution.getCurrentActivityId());
        sendNotification(review, status);

        task.setStatus(status);
        taskService.save(task);
        log.info("Status was changed to {} for task with uuid={}", status, taskUuid);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void sendNotification(Review review, TaskStatus status) {
        switch (status) {
            case READY_FOR_REVIEW:
                taskReadyForReviewNotify(review);
                break;
            case IN_REVIEW:
                taskInReviewAuthorNotify(review);
                break;
            case IN_PROGRESS:
                taskInProgressAuthorNotify(review);
                break;
            case APPROVED:
                taskApprovedAuthorNotify(review);
                break;
            case FORCE_CLOSED:
                taskForceClosedNotify(review);
                break;
            case CLOSED:
                break;
        }
    }

    private void taskReadyForReviewNotify(Review review) {
        String authorChatId = review.getTask().getAuthor().getChatId();
        int reviewGroupToNotify = review.getReviewStage();
        List<Member> membersToNotify = memberService.getMembersByReviewGroup(reviewGroupToNotify).stream()
            .filter(reviewer -> !reviewer.getChatId().equals(authorChatId))
            .collect(toList());
        List<Member> omniMembers = memberService.getOmniMembers();
        membersToNotify.addAll(omniMembers);

        for (Member reviewer : membersToNotify) {
            webhookRestClient.sendMessage(sendMessage(reviewer.getChatId(),
                String.format("Задача %s готова к ревью", review.getTask().getName())));
        }
    }

    private void taskInReviewAuthorNotify(Review review) {
        String authorChatId = review.getTask().getAuthor().getChatId();
        String reviewerLogin = review.getMemberReviews().stream()
            .filter(memberReview -> isNull(memberReview.getEndTime()))
            .findFirst()
            .get().getReviewer().getLogin();

        List<String> membersChatIdsToNotify = memberService.getOmniMembers().stream().map(Member::getChatId).collect(toList());
        membersChatIdsToNotify.add(authorChatId);

        for (String chatId : membersChatIdsToNotify) {
            webhookRestClient.sendMessage(sendMessage(chatId,
                String.format("%s взял в ревью задачу %s", reviewerLogin, review.getTask().getName())));
        }

    }

    private void taskInProgressAuthorNotify(Review review) {
        String authorChatId = review.getTask().getAuthor().getChatId();

        List<String> membersChatIdsToNotify = memberService.getOmniMembers().stream().map(Member::getChatId).collect(toList());
        membersChatIdsToNotify.add(authorChatId);

        for (String chatId : membersChatIdsToNotify) {
            webhookRestClient.sendMessage(sendMessage(chatId,
                String.format("Задача %s вернулась с ревью", review.getTask().getName())));
        }
    }

    private void taskApprovedAuthorNotify(Review review) {
        String authorChatId = review.getTask().getAuthor().getChatId();

        List<String> membersChatIdsToNotify = memberService.getOmniMembers().stream().map(Member::getChatId).collect(toList());
        membersChatIdsToNotify.add(authorChatId);

        for (String chatId : membersChatIdsToNotify) {
            webhookRestClient.sendMessage(sendMessage(chatId,
                String.format("Задача %s одобрена", review.getTask().getName())));
        }
    }

    private void taskForceClosedNotify(Review review) {
        String authorLogin = review.getTask().getAuthor().getLogin();
        List<Member> allMembers = memberService.getAllMembers();
        for (Member member : allMembers) {
            if (member.getChatId().equals(review.getTask().getAuthor().getChatId())) {
                continue;
            }
            webhookRestClient.sendMessage(sendMessage(member.getChatId(),
                String.format("@%s принудительно закрыл задачу %s", authorLogin, review.getTask().getName())));
        }
    }
}
