package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.domain.review.Review;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
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
}
