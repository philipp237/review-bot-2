package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.exceptions.NotRequiredReviewGroupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.transaction.Transactional;
import java.util.List;

import static dev.reviewbot2.domain.task.TaskStatus.READY_FOR_REVIEW;
import static dev.reviewbot2.domain.task.TaskType.DESIGN;
import static dev.reviewbot2.processor.Command.ACCEPT_REVIEW;
import static dev.reviewbot2.processor.Command.TAKE_IN_REVIEW;
import static dev.reviewbot2.utils.UpdateUtils.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakeInReviewTransactionScript {
    private final MemberService memberService;
    private final TaskService taskService;
    private final ReviewService reviewService;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String text = messageInfo.getText();

        Member reviewer = memberService.getMemberByChatId(chatId);

        validateReviewer(messageInfo, reviewer);

        if (text.contains("#")) {
            Long taskId = parseTextToGetTaskId(text);
            return getInfoAboutTask(taskId, messageInfo);
        }

        List<Review> availableReviews =
            getReviewsForTaskReadyForReview(reviewer);

        if (availableReviews.size() == 0) {
            return sendMessage(chatId, "Нет доступных для ревью задач");
        }

        InlineKeyboardMarkup keyboard = getKeyboard(availableReviews.size());
        fillKeyboardWithTaskForReview(keyboard, availableReviews);

        return sendMessage(chatId, "Нажми на задачу, чтобы изучить её и взять в ревью", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateReviewer(MessageInfo messageInfo, Member reviewer) {
        if (reviewer.getReviewGroup() == 0) {
            log.info("{} unsuccessfully tries to take task in review", reviewer.getLogin());
            throw new NotRequiredReviewGroupException(messageInfo);
        }
    }

    private SendMessage getInfoAboutTask(Long taskId, MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        Task task = taskService.getTaskById(taskId);

        String text = getText(task);
        InlineKeyboardMarkup keyboard = getKeyboard(2);
        fillKeyboardWithTakeInReviewActions(keyboard, task);

        return sendMessage(chatId, text, keyboard);
    }

    private String getText(Task task) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(task.getName());
        stringBuilder.append("\n").append(task.getLink());
        if (task.getAuthor().getLogin() == null) {
            stringBuilder.append("\nЛогин автора неизвестен");
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
            StringBuilder textBuilder = new StringBuilder(review.getTask().getName())
                .append("\n")
                .append(getFormattedTime(review.getTask().getLastActionTime()));

            if (DESIGN.equals(review.getTask().getTaskType())) {
                textBuilder.append(" (дизайн)");
            }

            String callbackData = "/" + TAKE_IN_REVIEW + "#" + review.getTask().getId();
            keyboard.getKeyboard().get(i).add(getButton(textBuilder.toString(), callbackData));
            i++;
        }
    }

    private void fillKeyboardWithTakeInReviewActions(InlineKeyboardMarkup keyboard, Task task) {
        keyboard.getKeyboard().get(0).add(getButton("Назад", "/" + TAKE_IN_REVIEW));
        if (READY_FOR_REVIEW.equals(task.getStatus())) {
            keyboard.getKeyboard().get(1).add(getButton("Взять в ревью", "/" + ACCEPT_REVIEW + "#" + task.getId()));
        }
    }

    private Long parseTextToGetTaskId(String text) {
        String[] parsedText = text.split("#");
        return Long.parseLong(parsedText[parsedText.length - 1]);
    }

    private List<Review> getReviewsForTaskReadyForReview(Member reviewer) {
        List<Review> reviews =
            reviewService.getReviewsForTaskReadyForReview(reviewer.getReviewGroup(), reviewer.isCanReviewDesign());
        return reviews.stream()
            .filter(review -> !review.getTask().getAuthor().equals(reviewer))
            .collect(toList());
    }
}
