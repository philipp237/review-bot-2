package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.review.Review;

import java.util.List;

public interface ReviewService {

    /**
     * Сохранить ревью
     *
     * @param review - ревью
     * @return ревью
     */
    Review save(Review review);

    /**
     * Получить список ревью задач, готовых для ревью, для заданной группы ревью
     *
     * @param reviewStage - стадия ревью
     * @return ревью
     */
    List<Review> getReviewsForTaskReadyForReview(int reviewStage, boolean isReviewDesigner);
}
