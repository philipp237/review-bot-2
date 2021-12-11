package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.review.Review;
import dev.reviewbot2.domain.task.Task;
import dev.reviewbot2.domain.task.TaskStatus;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.processor.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

import static dev.reviewbot2.processor.Utils.sendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTaskTransactionalScript {
    private static final String TASK_CREATED = "Задача %s создана";

    private final MemberService memberService;
    private final ReviewService reviewService;
    private final ProcessAccessor processAccessor;

    @Transactional
    public SendMessage execute(String chatId, String taskName, String link, TaskType taskType) {
        Member author = memberService.getMemberByChatId(chatId);

        Task task = Task.builder()
                .uuid(UUID.randomUUID().toString())
                .name(taskName)
                .link(link)
                .creationTime(Instant.now())
                .taskType(taskType)
                .status(TaskStatus.READY_FOR_REVIEW)
                .author(author)
                .build();

        Review review = Review.builder()
                .reviewStage(1)
                .task(task)
                .build();

        processAccessor.startProcess(task.getUuid());

        reviewService.save(review);
        log.info("{} create {} task {} with uuid={}",
                author.getLogin(), taskType.toString().toLowerCase(), taskName, task.getUuid());
        log.info("Review was created for task={}", taskName);

        return sendMessage(chatId, String.format(TASK_CREATED, taskName));
    }
}
