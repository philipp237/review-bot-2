package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotAuthorException;
import dev.reviewbot2.exceptions.NotRequiredTaskStatusException;
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
        Member member = memberService.getMemberByChatId(chatId);

        validateAuthor(update, task, member);
        validateStatus(update, task, member);

        task.setStatus(READY_FOR_REVIEW);

        taskService.save(task);
        processAccessor.submitForReview(task.getUuid());

        log.info("{} submitted task with uuid={} for review", task.getAuthor(), task.getUuid());

        return sendMessage(chatId, "Задача отправлена на ревью");
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateAuthor(Update update, Task task, Member member) {
        if (!member.equals(task.getAuthor())) {
            log.info("{} unsuccessfully tries to submit not his own task for review. Only author can submit task for review",
                member.getLogin());
            throw new NotAuthorException(update);
        }
    }

    private void validateStatus(Update update, Task task, Member member) {
        if (!task.getStatus().equals(IN_PROGRESS)) {
            log.info("{} unsuccessfully tries to submit task with status {} for review",
                member.getLogin(), task.getStatus());
            throw new NotRequiredTaskStatusException(update);
        }
    }
}
