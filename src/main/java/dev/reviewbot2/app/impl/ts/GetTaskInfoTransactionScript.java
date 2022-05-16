package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotAuthorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.transaction.Transactional;

import static dev.reviewbot2.domain.task.TaskStatus.IN_PROGRESS;
import static dev.reviewbot2.processor.Command.CLOSE;
import static dev.reviewbot2.processor.Command.SUBMIT;
import static dev.reviewbot2.utils.UpdateUtils.*;

@Component
@RequiredArgsConstructor
public class GetTaskInfoTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String text = messageInfo.getText();

        Long taskId = getTaskIdFromText(text);
        Task task = taskService.getTaskById(taskId);
        Member member = memberService.getMemberByChatId(chatId);

        validateAuthor(messageInfo, task, member);

        InlineKeyboardMarkup keyboard = getKeyboard((IN_PROGRESS.equals(task.getStatus()) && (memberIsAuthorOrNonOmni(member, task))) ? 2 : 1);
        fillKeyboardWithActions(keyboard, task, member);

        return sendMessage(chatId, task.getLink() + "\nВыбери действие:", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateAuthor(MessageInfo messageInfo, Task task, Member member) {
        if (!task.getAuthor().equals(member) && !member.getIsOmni()) {
            throw new NotAuthorException(messageInfo);
        }
    }

    private void fillKeyboardWithActions(InlineKeyboardMarkup keyboard, Task task, Member member) {
        int i = 0;

        if (IN_PROGRESS.equals(task.getStatus()) && (memberIsAuthorOrNonOmni(member, task))) {
            keyboard.getKeyboard().get(i).add(getButton(SUBMIT.getButtonText(), "/" + SUBMIT + "#" + task.getId()));
            i += 1;
        }

        keyboard.getKeyboard().get(i).add(getButton(CLOSE.getButtonText(), "/" + CLOSE + "#" + task.getId()));
    }

    private boolean memberIsAuthorOrNonOmni(Member member, Task task) {
        return task.getAuthor().equals(member) || !member.getIsOmni();
    }
}
