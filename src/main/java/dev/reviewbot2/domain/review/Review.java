package dev.reviewbot2.domain.review;

import dev.reviewbot2.domain.DomainObject;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.Instant;

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
     * Ревьюер
     */
    @JoinColumn(name = "reviewer_id")
    @OneToOne
    private Member reviewer;
}
