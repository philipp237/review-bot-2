package dev.reviewbot2.repository;

import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.MemberReview;
import dev.reviewbot2.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberReviewRepository extends JpaRepository<MemberReview, Long> {
    List<MemberReview> getAllByReviewAndReviewer(Review review, Member member);
    MemberReview getByReviewAndEndTimeIsNull(Review review);
}
