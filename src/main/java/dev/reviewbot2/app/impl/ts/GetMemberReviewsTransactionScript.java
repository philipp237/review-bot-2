package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.transaction.Transactional;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.processor.Command.TAKE_IN_REVIEW;
import static dev.reviewbot2.utils.UpdateUtils.*;

@Component
@RequiredArgsConstructor
public class GetMemberReviewsTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();

        Member member = memberService.getMemberByChatId(chatId);
        List<Task> taskInMemberReview = taskService.getTaskInMemberReview(member);

        InlineKeyboardMarkup keyboard = getKeyboard(taskInMemberReview.size());
        fillKeyboardWithTask(keyboard, taskInMemberReview);

        return sendMessage(chatId, "Список моих ревью:", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void fillKeyboardWithTask(InlineKeyboardMarkup keyboard, List<Task> tasks) {
        int i = 0;

        for (Task task : tasks) {
            keyboard.getKeyboard().get(i).add(getButton(getText(task),"/" + TAKE_IN_REVIEW + "#" + task.getId()));
            i++;
        }
    }

    private String getText(Task task) {
        StringBuilder text = new StringBuilder(task.getName());
        if (task.getTaskType().equals(DESIGN)) {
            text.append(" (дизайн)");
        }
        text.append("\n").append(task.getCreationTime());
        return text.toString();
    }
}
