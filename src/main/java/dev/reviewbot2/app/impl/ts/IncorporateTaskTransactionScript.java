package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.adapter.WebhookRestClient;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NoPermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

import static dev.reviewbot2.processor.Utils.getTaskIdFromText;
import static dev.reviewbot2.processor.Utils.sendMessage;

@Component
@RequiredArgsConstructor
public class IncorporateTaskTransactionScript {
    private final TaskService taskService;
    private final MemberService memberService;
    private final ProcessAccessor processAccessor;
    private final WebhookRestClient webhookRestClient;

    @Transactional
    public void execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String text = messageInfo.getText();

        Member member = memberService.getMemberByChatId(chatId);

        validateOmni(messageInfo, member);

        if (text.contains("#")) {
            Long taskId = getTaskIdFromText(text);
            Task task = taskService.getTaskById(taskId);
            processAccessor.sendIntoProduction(task.getUuid());

            webhookRestClient.sendMessage(sendMessage(chatId, String.format("%s отправлена на прод", task.getName())));
            return;
        }

        List<Task> tasks = taskService.getClosedTasks();
        for (Task task : tasks) {
            processAccessor.sendIntoProduction(task.getUuid());
        }

        webhookRestClient.sendMessage(sendMessage(chatId, "Все закрытые задачи отправлены на прод"));
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateOmni(MessageInfo messageInfo, Member member) {
        if (!member.getIsOmni()) {
            throw new NoPermissionException(messageInfo);
        }
    }
}
