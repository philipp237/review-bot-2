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
     * @param update обновление, в котором содержится сообщение для удаления
     */
    void deletePreviousMessage(Update update) throws TelegramApiException;

    /**
     * Обработка ссылки на задачу
     *
     * @param update обновление, в котором содержится сообщение с ссылкой на задачу
     * @return сообщение для отправки
     */
    SendMessage processTaskLink(Update update) throws TelegramApiException;

    /**
     * Взять задачу в ревью
     *
     * @param update обновление, в котором содержится сообщение с командой взятия задачи в ревью
     * @return сообщение с возможными действиями с задачей или список задач для ревью, если не был передан id задачи,
     * которую берут в ревью
     */
    SendMessage takeInReview(Update update) throws TelegramApiException;

    /**
     * Подтвердить взятие задачи в ревью
     *
     * @param update обновление, в котором содержится сообщение с командой подтверждения взятия задачи в ревью
     * @return сообщение о взятии задачи в ревью
     */
    SendMessage acceptReview(Update update) throws TelegramApiException;

    /**
     * Завершить ревью
     *
     * @param udpate обновление, в котором содержится сообщение с командой подтверждения завершения ревью
     * @param isApproved резолюция окончания ревью
     * @return сообщение о завершении ревью
     */
    SendMessage completeReview(Update udpate, boolean isApproved) throws TelegramApiException;

    /**
     * Отправить задачу на ревью
     *
     * @param update обновление, в котором содержится сообщение с командой отправки задачи на ревью
     * @return сообщение об отправке задачи на ревью
     */
    SendMessage submitForReview(Update update) throws TelegramApiException;

    /**
     * Закрыть задачу
     *
     * @param update обновление, в котором содержится сообщение с командой закрытия задачи
     * @return сообщение о закрытии задачи
     */
    SendMessage closeTask(Update update) throws TelegramApiException;

    /**
     * Возвращает список доступных действий
     *
     * @param update обновление, в котором содержится сообщение с командой вызова возможных действий
     * @return сообщение со списком возможных действий
     */
    SendMessage start(Update update) throws TelegramApiException;

    /**
     * Отправляет сообщение с подсказкой о создании задачи
     *
     * @param update обновление, в котором содержится сообщение о создании задачи
     * @return сообщение с подсказкой о создании задачи
     */
    SendMessage createTask(Update update) throws TelegramApiException;

    /**
     * Получить список ревью пользователя
     *
     * @param update обновление, в котором содержится сообщение с командой получения своего списка ревью
     * @return сообщение со списком ревью пользователя
     */
    SendMessage getMemberReviews(Update update) throws TelegramApiException;

    /**
     * Обновить логин пользователя
     *
     * @param chatId идентификатор чата
     * @param login новый логин пользователя
     */
    void updateMemberLogin(String chatId, String login);

    /**
     * Обновить идентификатор чата пользователя
     *
     * @param chatId идентификатор чата
     * @param login новый логин пользователя
     */
    void updateChatId(String chatId, String login);

    /**
     * Получить список задач пользователя
     *
     * @param update обновление, в котором содержится сообщение с командой получения своих задач
     * @return список задач пользователя
     */
    SendMessage getMemberTasks(Update update) throws TelegramApiException;

    /**
     * Добавить пользователя в базу данных
     *
     * @param update обновление, в котором содержится сообщение с логином пользователя
     * @return сообщение с подсказкой о добавлении пользователя
     */
    SendMessage addMember(Update update) throws TelegramApiException;

    /**
     * Изменить группу ревью пользователя
     *
     * @param update обновление, в котором содержится сообщение с логином и необязательно с группой ревью пользователя
     * @return сообщение с подсказкой о изменении группы ревью пользователя или с предложением ввести группу ревью
     */
    SendMessage updateMember(Update update) throws TelegramApiException;

    /**
     * Получить информацию о задаче
     *
     * @param update обновление, в котором содержится сообщение с командой получения информации о задаче
     * @return сообщение с информацией о задаче и возможных действиях
     */
    SendMessage getTaskInfo(Update update) throws TelegramApiException;
}
