package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NoPermissionException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

import static dev.reviewbot2.processor.Command.INCORPORATE;
import static dev.reviewbot2.utils.UpdateUtils.*;

@Component
@AllArgsConstructor
public class GetTasksReadyForIncorporationTransactionScript {
    private final TaskService taskService;
    private final MemberService memberService;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        Member member = memberService.getMemberByChatId(chatId);

        validateOmni(messageInfo, member);

        List<Task> tasks = taskService.getClosedTasks();
        Collections.sort(tasks);

        if (tasks.isEmpty()) {
            return sendMessage(chatId, "Нет закрытых задач");
        }

        StringBuilder textBuilder = new StringBuilder("Закрытые задачи (нажми на задачу, чтобы ее закрыть):\n");
        InlineKeyboardMarkup keyboard = getKeyboard(tasks.size() + 1);
        int i = 0;
        for (Task task : tasks) {
            String taskName = task.getName();

            textBuilder.append(String.format("%-16s%8s%n", taskName, task.getSegment().getText()));
            keyboard.getKeyboard().get(i).add(getButton(taskName, "/" + INCORPORATE + "#" + task.getId()));
            i++;
        }
        keyboard.getKeyboard().get(i).add(getButton("Закрыть все задачи", "/" + INCORPORATE));

        return sendMessage(chatId, textBuilder.toString().trim(), keyboard);
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
