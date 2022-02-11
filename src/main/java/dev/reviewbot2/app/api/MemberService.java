package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.member.Member;

import java.util.List;

public interface MemberService {
    /**
     * Существует ли пользователь с указанным id чата
     *
     * @param chatId - id чата
     * @return true - если существует, false - если нет
     */
    boolean isChatIdExists(String chatId);

    /**
     * Существует ли пользователь с указанным логином
     *
     * @param login - логин пользователя
     * @return true - если существует, false - если нет
     */
    boolean isLoginExists(String login);

    /**
     * Получить пользователя по id чата
     *
     * @param chatId - id чата
     * @return - пользователь
     */
    Member getMemberByChatId(String chatId);

    /**
     * Получить пользователя по его логину
     *
     * @param login - логин пользователя
     * @return - пользователь
     */
    Member getMemberByLogin(String login);

    /**
     * Получить всех пользователей
     *
     * @return - список из всех пользователей
     */
    List<Member> getAllMembers();

    /**
     * Получить пользователей из заданной группы ревью
     *
     * @param reviewGroup - группа ревью
     * @return - список пользователей из заданной группы ревью
     */
    List<Member> getMemberByReviewGroup(int reviewGroup);

    /**
     * Сохранить пользователя
     *
     * @param member - пользователь
     * @return - пользователь
     */
    Member save(Member member);
}
