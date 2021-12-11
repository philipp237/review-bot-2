package dev.reviewbot2.domain.review;

import dev.reviewbot2.domain.DomainObject;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

/**
 * Ревью
 */
@Entity
@Getter
@Setter
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
