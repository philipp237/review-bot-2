package dev.reviewbot2.app.impl.camunda.delegates;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.processor.Utils;
import dev.reviewbot2.webhook.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static dev.reviewbot2.processor.Utils.sendMessage;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class SendNotificationDelegate implements JavaDelegate {
    private final ProcessAccessor processAccessor;
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;
    private final WebhookRestClient webhookRestClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String taskUuid = processAccessor.getTaskUuid(execution);
        Task task = taskService.getTaskByUuid(taskUuid);
        Review review = reviewService.getReviewByTask(task);
        List<Member> reviewersToNotificate = memberService.getMemberByReviewGroup(review.getReviewStage()).stream()
            .filter(member -> !member.getChatId().equals(task.getAuthor().getChatId()))
            .collect(toList());

        for (Member reviewer : reviewersToNotificate) {
            webhookRestClient.sendMessage(sendMessage(reviewer.getChatId(), String.format("Задача %s готова к ревью", task.getName())));
        }
    }
}
