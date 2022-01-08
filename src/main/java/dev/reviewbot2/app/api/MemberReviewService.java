package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.review.MemberReview;

public interface MemberReviewService {

    /**
     * Сохранить ревью отдельного пользователя
     *
     * @param memberReview - ревью отдельного пользователя
     * @return ревью отдельного пользователя
     */
    MemberReview save(MemberReview memberReview);
}
