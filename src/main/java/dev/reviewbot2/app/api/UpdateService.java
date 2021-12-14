package dev.reviewbot2.app.api;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Сервис для взаимодействия с telegram bots api
 */
public interface UpdateService {

    /**
     * Удаление предыдущего сообщения
     *
     * @param update - обновление, в котором содержится сообщение для удаления
     */
    void deletePreviousMessage(Update update) throws TelegramApiException;

    /**
     * Обработка ссылки на задачу
     *
     * @param update - обновление, в котором содержится сообщение с ссылкой на задачу
     * @return - сообщение для отправки
     */
    SendMessage processTaskLink(Update update) throws TelegramApiException;
}