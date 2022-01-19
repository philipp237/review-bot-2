package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.processor.Utils;
import dev.reviewbot2.webhook.WebhookRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.APPROVED;
import static dev.reviewbot2.domain.task.TaskStatus.CLOSED;
import static dev.reviewbot2.processor.Utils.*;
import static java.time.Instant.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloseTaskTransactionScript {
    private final TaskService taskService;
    private final MemberService memberService;
    private final ProcessAccessor processAccessor;
    private final WebhookRestClient restClient;

    public SendMessage execute(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String text = getTextFromUpdate(update);

        Member member = memberService.getMemberByChatId(chatId);

        Long taskId = Utils.getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);
        TaskStatus lastTaskStatus = task.getStatus();

        validateAuthor(member, task);

        task.setCloseTime(now());
        taskService.save(task);

        if (!lastTaskStatus.equals(APPROVED)) {
            processAccessor.forceCloseTask(task.getUuid());
            log.info("{} force closed task with uuid={}", member.getLogin(), task.getUuid());
            List<Member> team = memberService.getAllMembers();
            for (Member eachMemberOfTeam : team) {
                restClient.sendMessage(sendMessage(eachMemberOfTeam.getChatId(),
                    String.format("@%s принудительно закрыл задачу %s", member.getLogin(), task.getName())));
            }
            return sendMessage(chatId, "Задача принудительно закрыта");
        }

        processAccessor.closeTask(task.getUuid());
        log.info("{} closed task with uuid={}", member.getLogin(), task.getUuid());
        return sendMessage(chatId, "Задача закрыта");
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateAuthor(Member member, Task task) throws TelegramApiException {
        if (!member.getChatId().equals(task.getAuthor().getChatId())) {
            throw new TelegramApiException(String.format("%s unsuccessfully tries to close not his own task with uuid=%s",
                member.getLogin(), task.getUuid()));
        }
    }
}
