package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.MemberReviewService;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.repository.MemberReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberReviewServiceImpl implements MemberReviewService {
    private final MemberReviewRepository memberReviewRepository;

    @Override
    public MemberReview save(MemberReview memberReview) {
        return memberReviewRepository.save(memberReview);
    }

    @Override
    public MemberReview getActiveReview(Review review) {
        return memberReviewRepository.getByReviewAndEndTimeIsNull(review);
    }
}
