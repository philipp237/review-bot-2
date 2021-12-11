package dev.reviewbot2.app.api;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
}
