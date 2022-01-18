package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.transaction.Transactional;

import static dev.reviewbot2.domain.task.TaskStatus.IN_PROGRESS;
import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.processor.Utils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmitForReviewTransactionScript {
    private final TaskService taskService;
    private final MemberService memberService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(Update update) throws TelegramApiException {
        String text = getTextFromUpdate(update);
        String chatId = getChatId(update);

        Long taskId = getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);

        validateTask(task, chatId);

        task.setStatus(READY_FOR_REVIEW);

        taskService.save(task);
        processAccessor.submitForReview(task.getUuid());

        log.info("{} submitted task with uuid={} for review", task.getAuthor(), task.getUuid());

        return sendMessage(chatId, "Задача отправлена на ревью");
    }

    // ================================================================================================================
    //  Implementation
    // ============================================================ ====================================================

    private void validateTask(Task task, String chatId) throws TelegramApiException {
        Member member = memberService.getMemberByChatId(chatId);
        if (!member.equals(task.getAuthor())) {
            throw new TelegramApiException(String.format("%s unsuccessfully tries to submit not his own task for review. " +
                    "Only author can submit task for review",
                member.getLogin()));
        }
        if (!task.getStatus().equals(IN_PROGRESS)) {
            throw new TelegramApiException(String.format("%s unsuccessfully tries to submit task with status %s for review. " +
                    "Status must be %s",
                member.getLogin(), task.getStatus().toString(), IN_PROGRESS.toString()));
        }
    }
}
