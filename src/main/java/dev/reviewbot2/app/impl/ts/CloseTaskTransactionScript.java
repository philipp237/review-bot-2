package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.exceptions.NotAuthorException;
import dev.reviewbot2.utils.UpdateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.transaction.Transactional;

import static dev.reviewbot2.domain.task.TaskStatus.APPROVED;
import static dev.reviewbot2.utils.UpdateUtils.*;
import static java.time.Instant.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloseTaskTransactionScript {
    private final TaskService taskService;
    private final MemberService memberService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String text = messageInfo.getText();

        Member member = memberService.getMemberByChatId(chatId);

        Long taskId = UpdateUtils.getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);
        TaskStatus lastTaskStatus = task.getStatus();

        validateAuthor(messageInfo, member, task);

        task.setCloseTime(now());
        taskService.save(task);

        if (!lastTaskStatus.equals(APPROVED)) {
            processAccessor.forceCloseTask(task.getUuid());
            return sendMessage(chatId, "Задача принудительно закрыта");
        }

        processAccessor.closeTask(task.getUuid());
        log.info("{} closed task with uuid={}", member.getLogin(), task.getUuid());
        return sendMessage(chatId, "Задача закрыта");
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateAuthor(MessageInfo messageInfo, Member member, Task task) {
        if (!member.getChatId().equals(task.getAuthor().getChatId())) {
            log.info("{} tries to close task with uuid={} not owned by him/her", member.getLogin(), task.getUuid());
            throw new NotAuthorException(messageInfo);
        }
    }
}
