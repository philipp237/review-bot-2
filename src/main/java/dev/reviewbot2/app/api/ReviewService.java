package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.review.Review;

public interface ReviewService {

    /**
     * Сохранить ревью
     *
     * @param review - ревью
     * @return ревью
     */
    Review save(Review review);
}
