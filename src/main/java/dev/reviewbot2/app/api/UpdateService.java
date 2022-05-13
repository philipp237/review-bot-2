package dev.reviewbot2.app.api;

import dev.reviewbot2.domain.MessageInfo;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Сервис для взаимодействия с telegram bots api
 */
public interface UpdateService {

    /**
     * Удаление предыдущего сообщения
     *
     * @param messageInfo обновление, в котором содержится сообщение для удаления
     */
    void deletePreviousMessage(MessageInfo messageInfo);

    /**
     * Обработка ссылки на задачу
     *
     * @param messageInfo обновление, в котором содержится сообщение с ссылкой на задачу
     * @return сообщение для отправки
     */
    SendMessage processTaskLink(MessageInfo messageInfo);

    /**
     * Взять задачу в ревью
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой взятия задачи в ревью
     * @return сообщение с возможными действиями с задачей или список задач для ревью, если не был передан id задачи,
     * которую берут в ревью
     */
    SendMessage takeInReview(MessageInfo messageInfo);

    /**
     * Подтвердить взятие задачи в ревью
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой подтверждения взятия задачи в ревью
     * @return сообщение о взятии задачи в ревью
     */
    SendMessage acceptReview(MessageInfo messageInfo);

    /**
     * Завершить ревью
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой подтверждения завершения ревью
     * @param isApproved резолюция окончания ревью
     * @return сообщение о завершении ревью
     */
    SendMessage completeReview(MessageInfo messageInfo, boolean isApproved);

    /**
     * Отправить задачу на ревью
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой отправки задачи на ревью
     * @return сообщение об отправке задачи на ревью
     */
    SendMessage submitForReview(MessageInfo messageInfo);

    /**
     * Закрыть задачу
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой закрытия задачи
     * @return сообщение о закрытии задачи
     */
    SendMessage closeTask(MessageInfo messageInfo);

    /**
     * Возвращает список доступных действий
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой вызова возможных действий
     * @return сообщение со списком возможных действий
     */
    SendMessage start(MessageInfo messageInfo);

    /**
     * Отправляет сообщение с подсказкой о создании задачи
     *
     * @param messageInfo обновление, в котором содержится сообщение о создании задачи
     * @return сообщение с подсказкой о создании задачи
     */
    SendMessage createTask(MessageInfo messageInfo);

    /**
     * Получить список ревью пользователя
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой получения своего списка ревью
     * @return сообщение со списком ревью пользователя
     */
    SendMessage getMemberReviews(MessageInfo messageInfo);

    /**
     * Обновить логин пользователя
     *
     * @param messageInfo cообщение с новым логином пользователя
     */
    void updateMemberLogin(MessageInfo messageInfo);

    /**
     * Обновить идентификатор чата пользователя
     *
     * @param messageInfo cообщение с новым ID чата пользователя
     */
    void updateChatId(MessageInfo messageInfo);

    /**
     * Получить список задач пользователя
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой получения своих задач
     * @return список задач пользователя
     */
    SendMessage getMemberTasks(MessageInfo messageInfo);

    /**
     * Добавить пользователя в базу данных
     *
     * @param messageInfo обновление, в котором содержится сообщение с логином пользователя
     * @return сообщение с подсказкой о добавлении пользователя
     */
    SendMessage addMember(MessageInfo messageInfo);

    /**
     * Изменить группу ревью пользователя
     *
     * @param messageInfo обновление, в котором содержится сообщение с логином и необязательно с группой ревью пользователя
     * @return сообщение с подсказкой о изменении группы ревью пользователя или с предложением ввести группу ревью
     */
    SendMessage updateMember(MessageInfo messageInfo);

    /**
     * Получить информацию о задаче
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой получения информации о задаче
     * @return сообщение с информацией о задаче и возможных действиях
     */
    SendMessage getTaskInfo(MessageInfo messageInfo);

    /**
     * Получить список всех закрытых задач, готовых для внедрения в продакшн
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой получения информации о закрытых задачах
     * @return сообщение со списком закрытых задач
     */
    SendMessage getTaskReadyForIncorporation(MessageInfo messageInfo);

    /**
     * Внедрить задачи в продакшн
     *
     * @param messageInfo обновление, в котором содержится сообщение с командой о внедрении задач в продакшн
     * @return сообщение об успехе перевода статуса задач или сообщение со списком задач, для которых это сделать не
     * получилось
     */
    SendMessage incorporateTasks(MessageInfo messageInfo);
}
