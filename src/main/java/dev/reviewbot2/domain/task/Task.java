package dev.reviewbot2.domain.task;

import dev.reviewbot2.domain.DomainObject;
import dev.reviewbot2.domain.member.Member;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.Instant;

/**
 * Задача
 */
@Entity
@Getter
@Setter
public class Task extends DomainObject {

    /**
     * Имя задачи
     */
    private String name;

    /**
     * Ссылка на задачу в джире
     */
    private String link;

    /**
     * Дата создания задачи
     */
    private Instant creationTime;

    /**
     * Дата закрытия задачи
     */
    private Instant closeTime;

    /**
     * Тип задачи
     */
    private TaskType taskType;

    /**
     * Статус задачи
     */
    private TaskStatus status;

    /**
     * Автор задачи
     */
    @JoinColumn(name = "author_id")
    @OneToOne
    private Member author;
}
