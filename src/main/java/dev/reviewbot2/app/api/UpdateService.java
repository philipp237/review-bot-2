package dev.reviewbot2.app.api;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
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

    /**
     * Взять задачу в ревью
     *
     * @param update - обновление, в котором содержится сообщение с командой взятия задачи в ревью
     * @return - сообщение с возможными действиями с задачей или список задач для ревью, если не был передан id задачи,
     * которую берут в ревью
     */
    SendMessage takeInReview(Update update) throws TelegramApiException;

    /**
     * Подтвердить взятие задачи в ревью
     *
     * @param update - обновление, в котором содержится сообщение с командой подтверждения взятия задачи в ревью
     * @return - сообщение о взятии задачи в ревью
     */
    SendMessage acceptReview(Update update) throws TelegramApiException;

    /**
     * Завершить ревью
     *
     * @param udpate - обновление, в котором содержится сообщение с командой подтверждения завершения ревью
     * @param isApproved - резолюция окончания ревью
     * @return - сообщение о завершении ревью
     */
    SendMessage completeReview(Update udpate, boolean isApproved) throws TelegramApiException;

    /**
     * Отправить задачу на ревью
     *
     * @param update - обновление, в котором содержится сообщение с командой отправки задачи на ревью
     * @return - сообщение об отправке задачи на ревью
     */
    SendMessage submitForReview(Update update) throws TelegramApiException;
}
