package dev.reviewbot2.domain.review;

import dev.reviewbot2.domain.DomainObject;
import dev.reviewbot2.domain.task.Task;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.List;

/**
 * Ревью
 */
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Review extends DomainObject {
    /**
     * Стадия ревью
     */
    private int reviewStage;

    /**
     * Задача
     */
    @JoinColumn(name = "task_id")
    @OneToOne
    private Task task;

    /**
     * Ревью отдельным участником команды
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reviewer")
    private List<MemberReview> reviewers;
}
