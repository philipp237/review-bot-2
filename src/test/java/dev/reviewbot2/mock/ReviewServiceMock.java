package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.domain.review.Review;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class ReviewServiceMock {
    private final ReviewService reviewService;

    public void mockSave() {
        doReturn(new Review()).when(reviewService).save(any());
    }

    public void mockGetReview(List<Review> reviews) {
        doReturn(reviews).when(reviewService).getReviewsForTaskReadyForReview(anyInt(), anyBoolean());
    }

    public void mockGetReviewByTask(Review review) {
        doReturn(review).when(reviewService).getReviewByTask(any());
    }

    public void mockGetReviewsByTasks(List<Review> reviews) {
        doReturn(reviews).when(reviewService).getReviewsByTasks(anyList());
    }
}
