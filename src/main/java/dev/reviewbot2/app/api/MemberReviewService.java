package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;

public interface MemberReviewService {

    /**
     * Сохранить ревью отдельного пользователя
     *
     * @param memberReview ревью отдельного пользователя
     * @return ревью отдельного пользователя
     */
    MemberReview save(MemberReview memberReview);

    /**
     * Получить ревью последнего ревьюера
     *
     * @param review само ревью, для которого будет возвращено ревью последнего ревьюера
     * @return ревью отдельного пользователя
     */
    MemberReview getActiveReview(Review review);
}
