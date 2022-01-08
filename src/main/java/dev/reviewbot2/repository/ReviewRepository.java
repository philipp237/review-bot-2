package dev.reviewbot2.repository;

import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "SELECT r FROM review r " +
        "INNER JOIN task t ON t.id = r.task_id " +
        "WHERE t.status = :status " +
        "AND t.task_type = :task_type " +
        "AND r.review_stage IN :review_stages", nativeQuery = true)
    List<Review> getReviews(@Param("status") String status,
                            @Param("task_type") String taskType,
                            @Param("review_stages") List<Integer> reviewStages);

    Review getReviewByTask(Task task);
}
