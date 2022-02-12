package dev.reviewbot2.repository;

import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "SELECT r FROM Review r " +
        "INNER JOIN Task t ON t = r.task " +
        "WHERE t.status = :status " +
        "AND t.taskType = :taskType " +
        "AND r.reviewStage IN :reviewStages")
    List<Review> getReviews(@Param("status") String status,
                            @Param("taskType") String taskType,
                            @Param("reviewStages") List<Integer> reviewStages);

    Review getReviewByTask(Task task);
}
