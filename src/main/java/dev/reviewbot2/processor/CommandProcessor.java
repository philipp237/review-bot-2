package dev.reviewbot2.processor;

import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.domain.MessageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final UpdateService updateService;

    public BotApiMethod<?> processCommand(MessageInfo messageInfo) {
        Command command = parseCommand(messageInfo.getText());

        switch (command) {
            case START:
                return updateService.start(messageInfo);
            case TAKE_IN_REVIEW:
                return updateService.takeInReview(messageInfo);
            case ACCEPT_REVIEW:
                return updateService.acceptReview(messageInfo);
            case APPROVE:
                return updateService.completeReview(messageInfo, true);
            case DECLINE:
                return updateService.completeReview(messageInfo, false);
            case SUBMIT:
                return updateService.submitForReview(messageInfo);
            case CLOSE:
                return updateService.closeTask(messageInfo);
            case CREATE_TASK:
                return updateService.createTask(messageInfo);
            case MY_REVIEWS:
                return updateService.getMemberReviews(messageInfo);
            case MY_TASKS:
                return updateService.getMemberTasks(messageInfo);
            case ADD_MEMBER:
                return updateService.addMember(messageInfo);
            case UPDATE_MEMBER:
                return updateService.updateMember(messageInfo);
            case INFO:
                return updateService.getTaskInfo(messageInfo);
            case CLOSED_TASKS:
                return updateService.getTaskReadyForIncorporation(messageInfo);
            case INCORPORATE:
                return updateService.incorporateTasks(messageInfo);
            case SPRINT:
                return updateService.getSprintValue(messageInfo);
        }
        return null;
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private Command parseCommand(String textFromUpdate) {
        String parsedCommand = textFromUpdate.split("#")[0];
        validateCommand(parsedCommand);

        return Command.valueOf(parsedCommand.split("/")[1].toUpperCase());
    }

    private void validateCommand(String textFromUpdate) {
        if (textFromUpdate.matches("^/[a-zA-Z_]*")) {
            return;
        }
        throw new IllegalArgumentException("Validations errors: incorrect command " + textFromUpdate);
    }
}
