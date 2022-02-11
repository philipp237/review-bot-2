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

    /**
     * Закрыть задачу
     *
     * @param update - обновление, в котором содержится сообщение с командой закрытия задачи
     * @return - сообщение о закрытии задачи
     */
    SendMessage closeTask(Update update) throws TelegramApiException;

    /**
     * Возвращает список доступных действий
     *
     * @param update - обновление, в котором содержится сообщение с командой вызова возможных действий
     * @return - сообщение со списком возможных действий
     */
    SendMessage start(Update update) throws TelegramApiException;

    /**
     * Отправляет сообщение с подсказкой о создании задачи
     *
     * @param update - обновление, в котором содержится сообщение о создании задачи
     * @return - сообщение с подсказкой о создании задачи
     */
    SendMessage createTask(Update update) throws TelegramApiException;

    /**
     * Получить список ревью пользователя
     *
     * @param update - обновление, в котором содержится сообщение с командой получения своего списка ревью
     * @return - сообщение со списком ревью пользователя
     */
    SendMessage getMemberReviews(Update update) throws TelegramApiException;

    /**
     * Обновить логин пользователя
     *
     * @param chatId - идентификатор чата
     * @param login - новый логин пользователя
     */
    void updateMemberLogin(String chatId, String login);

    /**
     * Обновить идентификатор чата пользователя
     *
     * @param chatId - идентификатор чата
     * @param login - новый логин пользователя
     */
    void updateChatId(String chatId, String login);
}
