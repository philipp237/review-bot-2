package dev.reviewbot2.service;

import dev.reviewbot2.app.api.UpdateService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.Mockito.*;

@RequiredArgsConstructor
public class UpdateServiceMock {
    private final UpdateService updateService;

    public void mockDeletePreviousMessage(boolean isDeleteSuccessful) throws TelegramApiException {
        if (isDeleteSuccessful) {
            doNothing().when(updateService).deletePreviousMessage(any());
        } else {
            doThrow(new TelegramApiException()).when(updateService).deletePreviousMessage(any());
        }
    }
}
