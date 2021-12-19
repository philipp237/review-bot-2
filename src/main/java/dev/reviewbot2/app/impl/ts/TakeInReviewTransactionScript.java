package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.transaction.Transactional;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static dev.reviewbot2.processor.Command.TAKE_IN_REVIEW;
import static dev.reviewbot2.processor.Utils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakeInReviewTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;

    @Transactional
    public SendMessage execute(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String text = getTextFromUpdate(update);

        Member reviewer = memberService.getMemberByChatId(chatId);
        if (reviewer.getReviewGroup() == 0) {
            log.warn("{} unsuccessfully tries to take task in review", reviewer.getLogin());
            return sendMessage(chatId, "Ты не можешь ревьюить задачи");
        }

        if (text.contains("#")) {
            Long taskId = parseTextToGetTaskId(text);
            return getInfoAboutTask(taskId, update);
        }

        List<Review> availableReviews =
            reviewService.getReviewsForTaskReadyForReview(reviewer.getReviewGroup(), reviewer.isCanReviewDesign());

        InlineKeyboardMarkup keyboard = getKeyboard(availableReviews.size());
        fillKeyboardWithTaskForReview(keyboard, availableReviews);

        return sendMessage(chatId, "Нажми на задачу, чтобы изучить её и взять в ревью", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private SendMessage getInfoAboutTask(Long taskId, Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        Task task = taskService.getTaskById(taskId);

        String text = getText(task);
        InlineKeyboardMarkup keyboard = getKeyboard(2);
        fillKeyboardWithTakeInReviewActions(keyboard, taskId);

        return sendMessage(chatId, text, keyboard);
    }

    private String getText(Task task) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(task.getName());
        stringBuilder.append("\n").append(task.getLink());
        if (task.getAuthor().getLogin() == null) {
            stringBuilder.append("Логин автора неизвестен");
        } else {
            stringBuilder.append("\nАвтор: @").append(task.getAuthor().getLogin());
        }
        if (DESIGN.equals(task.getTaskType())) {
            stringBuilder.append("\nТип: дизайн");
        } else {
            stringBuilder.append("\nТип: реализация");
        }
        stringBuilder.append("\nСоздана: ").append(getFormattedTime(task.getCreationTime()));
        return stringBuilder.toString();
    }

    private void fillKeyboardWithTaskForReview(InlineKeyboardMarkup keyboard, List<Review> reviews) {
        int i = 0;

        for(Review review : reviews) {
            String text = review.getTask().getName() + "\n" + getFormattedTime(review.getTask().getLastActionTime());
            String callbackData = "/" + TAKE_IN_REVIEW + "#" + review.getTask().getId();
            keyboard.getKeyboard().get(i).add(getButton(text, callbackData));
            i++;
        }
    }

    private void fillKeyboardWithTakeInReviewActions(InlineKeyboardMarkup keyboard, Long taskId) {
        keyboard.getKeyboard().get(0).add(getButton("Назад", "/" + TAKE_IN_REVIEW));
        keyboard.getKeyboard().get(1).add(getButton("Взять в ревью", "/" + ACCEPT_REVIEW + "#" + taskId));
    }

    private Long parseTextToGetTaskId(String text) {
        String[] parsedText = text.split("#");
        return Long.parseLong(parsedText[parsedText.length - 1]);
    }
}
