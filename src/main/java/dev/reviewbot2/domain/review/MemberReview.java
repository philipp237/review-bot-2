package dev.reviewbot2.domain.review;

import dev.reviewbot2.domain.DomainObject;
import dev.reviewbot2.domain.member.Member;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Instant;

/**
 * Отдельно взятое ревью одним из участников команды
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemberReview extends DomainObject {

    /**
     * Время начала ревью
     */
    private Instant startTime;

    /**
     * Время окончания ревью
     */
    private Instant endTime;

    /**
     * Ревьюер
     */
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Member reviewer;

    /**
     * Ревью
     */
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;
}
