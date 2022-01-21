package dev.reviewbot2.processor;

import dev.reviewbot2.app.api.UpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Utils.getTextFromUpdate;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final UpdateService updateService;

    public BotApiMethod<?> processCommand(Update update) throws TelegramApiException {
        Command command = parseCommand(getTextFromUpdate(update));

        switch (command) {
            case START:
                return updateService.start(update);
            case TAKE_IN_REVIEW:
                return updateService.takeInReview(update);
            case ACCEPT_REVIEW:
                return updateService.acceptReview(update);
            case APPROVE:
                return updateService.completeReview(update, true);
            case DECLINE:
                return updateService.completeReview(update, false);
            case SUBMIT:
                return updateService.submitForReview(update);
            case CLOSE:
                return updateService.closeTask(update);
            case CREATE_TASK:
                return updateService.createTask(update);
            case MY_REVIEWS:
                return updateService.getMemberReviews(update);
        }
        return null;
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private Command parseCommand(String textFromUpdate) throws TelegramApiException {
        String parsedCommand = textFromUpdate.split("#")[0];
        validateCommand(parsedCommand);

        return Command.valueOf(parsedCommand.split("/")[1].toUpperCase());
    }

    private void validateCommand(String textFromUpdate) throws TelegramApiException {
        if (textFromUpdate.matches("^/[a-zA-Z_]*")) {
            return;
        }
        throw new TelegramApiException("Validations errors: incorrect command " + textFromUpdate);
    }
}
