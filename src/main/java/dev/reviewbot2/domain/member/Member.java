package dev.reviewbot2.domain.member;

import dev.reviewbot2.domain.DomainObject;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;

/**
 * Участник команды
 */
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Member extends DomainObject {

    /**
     * Логин
     */
    private String login;

    /**
     * ID чата
     */
    @EqualsAndHashCode.Include
    private String chatId;

    /**
     * Группа ревью
     *
     * 0 - не может ревьюить
     * 1,2,... - могут ревьюить. От младшей роли к старшей
     */
    private int reviewGroup;

    /**
     * Может ли ревьюить дизайны
     */
    private boolean canReviewDesign;

    /**
     * Есть ли доступ к админке
     */
    private Boolean isOmni;
}
