package dev.reviewbot2.app.api;

public interface MemberService {
    /**
     * Существует ли пользователь с указанным id чата
     *
     * @param chatId - id чата
     * @return true - если существует, false - если нет
     */
    boolean isExists(String chatId);
}
