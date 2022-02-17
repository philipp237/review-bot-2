package dev.reviewbot2.repository;

import dev.reviewbot2.domain.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Task getByUuid(String uuid);

    @Query(value = "SELECT * FROM task t " +
        "LEFT JOIN review r on t.id = r.task_id " +
        "LEFT JOIN member_review mr on r.id = mr.review_id " +
        "WHERE r.review_stage = :reviewGroup " +
        "AND t.status NOT IN ('CLOSED', 'FORCE_CLOSED', 'APPROVED') " +
        "AND mr.reviewer_id = :id " +
        "GROUP BY t.id, r.id",
        nativeQuery = true)
    List<Task> getTaskInMemberReview(long id, int reviewGroup);
}
