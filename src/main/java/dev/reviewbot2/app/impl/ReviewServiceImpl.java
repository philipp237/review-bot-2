package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }
}
