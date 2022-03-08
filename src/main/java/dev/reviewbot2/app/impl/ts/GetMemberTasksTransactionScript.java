package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.processor.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static dev.reviewbot2.processor.Command.INFO;
import static dev.reviewbot2.processor.Utils.*;

@Component
@RequiredArgsConstructor
public class GetMemberTasksTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;

    public SendMessage execute(Update update) throws TelegramApiException {
        String chatId = getChatId(update);

        Member member = memberService.getMemberByChatId(chatId);
        List<Task> tasks = taskService.getMemberTasks(member);

        InlineKeyboardMarkup keyboard = getKeyboard(tasks.size());
        fillKeyboardWithTasks(keyboard, tasks);

        return sendMessage(chatId, "Список задач:", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void fillKeyboardWithTasks(InlineKeyboardMarkup keyboard, List<Task> tasks) {
        int i = 0;

        List<Review> reviews = reviewService.getReviewsByTasks(tasks);

        for (Review review : reviews) {
            Task task = review.getTask();
            int reviewStage = review.getReviewStage();
            keyboard.getKeyboard().get(i).add(getButton(String.format("%s (%s) %d stage",
                task.getName(),
                task.getStatus().toString().toLowerCase().replace("_", " "),
                reviewStage),
                "/" + INFO + "#" + task.getId()));
            i++;
        }
    }
}
