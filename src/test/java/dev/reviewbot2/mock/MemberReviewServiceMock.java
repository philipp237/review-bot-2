package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.MemberReviewService;
import dev.reviewbot2.domain.review.MemberReview;
import lombok.RequiredArgsConstructor;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class MemberReviewServiceMock {
    private final MemberReviewService memberReviewService;

    public void mockSave() {
        doReturn(new MemberReview()).when(memberReviewService).save(any());
    }
}
