package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.domain.task.TaskType.IMPLEMENTATION;
import static java.util.Collections.singletonList;

@Component
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsForTaskReadyForReview(int reviewStage, boolean isDesignReviewer) {
        List<Review> reviews = new ArrayList<>(reviewRepository.getReviews(READY_FOR_REVIEW,
            IMPLEMENTATION, singletonList(reviewStage)));

        if (isDesignReviewer) {
            reviews.addAll(reviewRepository.getReviews(READY_FOR_REVIEW,
                DESIGN, singletonList(1)));
        }

        return reviews;
    }

    @Override
    public Review getReviewByTask(Task task) {
        return reviewRepository.getReviewByTask(task);
    }

    @Override
    public List<Review> getReviewsByTasks(List<Task> tasks) {
        return reviewRepository.getReviewsByTaskIn(tasks);
    }
}
