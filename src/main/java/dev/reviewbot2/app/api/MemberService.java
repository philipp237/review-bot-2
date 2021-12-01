package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.member.Member;

public interface MemberService {
    /**
     * Существует ли пользователь с указанным id чата
     *
     * @param chatId - id чата
     * @return true - если существует, false - если нет
     */
    boolean isExists(String chatId);
}
