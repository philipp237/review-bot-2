package dev.reviewbot2.app.impl.camunda.delegates;

import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@RequiredArgsConstructor
public class CheckReviewStageDelegate implements JavaDelegate {
    private final ProcessAccessor processAccessor;
    private final TaskService taskService;
    private final ReviewService reviewService;

    @Value("${settings.number-of-review-stages}")
    private int numberOfReviewStages;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) throws Exception {
        String taskUuid = processAccessor.getTaskUuid(execution);
        Task task = taskService.getTaskByUuid(taskUuid);
        Review review = reviewService.getReviewByTask(task);

        boolean isLastStage = review.getReviewStage() == numberOfReviewStages;

        if (!isLastStage) {
            review.setReviewStage(review.getReviewStage() + 1);
            reviewService.save(review);
        }

        processAccessor.checkReviewStage(execution.getId(), isLastStage);
    }
}
