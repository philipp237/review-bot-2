package dev.reviewbot2.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Utils.getChatId;
import static dev.reviewbot2.processor.Utils.sendMessage;

@Slf4j
@ControllerAdvice
public class ReviewBotExceptionHandler {

    @ExceptionHandler(Exception.class)
    public void defaultHandle(Exception e) {
        log.error(e.getMessage(), e);
    }

    @ExceptionHandler(ReviewBotException.class)
    public ResponseEntity<SendMessage> reviewBotExceptionHandle(ReviewBotException e) throws TelegramApiException {
        String message = ExceptionMessage.getByException(e.getClass());
        return ResponseEntity.ok(sendMessage(getChatId(e.update), message));
    }
}
