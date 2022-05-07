package dev.reviewbot2.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Информация из сообщения
 */
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageInfo {

    /**
     * ID сообщения
     */
    Integer messageId;

    /**
     * Уникальный для каждого участника идентификатор чата
     */
    String chatId;

    /**
     * Текст сообщения, отправленного пользователем или полученного при нажатии на кнопку
     */
    String text;

    /**
     * Логин участника, отправившего сообщение или нажавшего на кнопку
     */
    String login;

    /**
     * Флаг, обозначающий природу сообщения. True - нажатие на кнопку, false - отправка сообщения
     */
    Boolean hasCallbackQuery;
}
