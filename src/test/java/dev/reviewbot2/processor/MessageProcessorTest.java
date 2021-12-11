package dev.reviewbot2.processor;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.service.MemberServiceMock;
import dev.reviewbot2.service.UpdateServiceMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class MessageProcessorTest extends AbstractUnitTest {
    @Mock
    private MemberService memberService;
    @Mock
    private UpdateService updateService;

    @Captor
    private ArgumentCaptor<String> stringCaptor;
    private AutoCloseable closeable;

    private MemberServiceMock memberServiceMock;
    private UpdateServiceMock updateServiceMock;
    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.messageProcessor = new MessageProcessor(memberService, updateService);
        this.messageProcessor.setJiraLink(JIRA_LINK);
        this.messageProcessor.setDashboards(DASHBOARD);
        this.memberServiceMock = new MemberServiceMock(memberService);
        this.updateServiceMock = new UpdateServiceMock(updateService);
    }

    @AfterEach
    void close() throws Exception {
        closeable.close();
    }

    @Test
    void processMessage_updateHasNoMessage() {
        memberServiceMock.mockIsExistsByChatId(true);

        Update update = getUpdateWithoutMessage();
        assertThrows(TelegramApiException.class, () -> messageProcessor.processMessage(update));
    }

    @Test
    void processMessage_withExistingMember_withMessage() throws TelegramApiException {
        memberServiceMock.mockIsExistsByChatId(true);

        Update update = getUpdateWithMessage();
        //TODO Доделать тест, когда появится логика обработки сообщений
        assertNull(messageProcessor.processMessage(update));
    }

    @Test
    void processMessage_withExistingMember_withCallbackQuery_successful() throws TelegramApiException {
        memberServiceMock.mockIsExistsByChatId(true);
        updateServiceMock.mockDeletePreviousMessage(true);

        Update update = getUpdateWithCallbackQuery();
        //TODO Доделать тест, когда появится логика обработки сообщений
        assertNull(messageProcessor.processMessage(update));

        Mockito.verify(updateService, times(1)).deletePreviousMessage(any());
    }

    @Test
    void processMessage_withExistingMember_withCallbackQuery_failed() throws TelegramApiException {
        memberServiceMock.mockIsExistsByChatId(true);
        updateServiceMock.mockDeletePreviousMessage(false);

        Update update = getUpdateWithCallbackQuery();
        assertThrows(TelegramApiException.class, () -> messageProcessor.processMessage(update));

        Mockito.verify(updateService, times(1)).deletePreviousMessage(any());
    }

    @Test
    void processMessage_withoutMember() throws TelegramApiException {
        memberServiceMock.mockIsExistsByChatId(false);

        Update update = getUpdateWithMessage();
        assertNull(messageProcessor.processMessage(update));
    }
}