package dev.reviewbot2.processor;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.service.MemberServiceMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageProcessorTest extends AbstractUnitTest {
    @Mock
    private MemberService memberService;

    @Captor
    private ArgumentCaptor<String> stringCaptor;
    private AutoCloseable closeable;

    private MemberServiceMock memberServiceMock;
    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.messageProcessor = new MessageProcessor(memberService);
        this.messageProcessor.setJiraLink(JIRA_LINK);
        this.messageProcessor.setDashboards(DASHBOARD);
        this.memberServiceMock = new MemberServiceMock(memberService);
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
    void processMessage_withExistingMember_withCallbackQuery() throws TelegramApiException {
        memberServiceMock.mockIsExistsByChatId(true);

        Update update = getUpdateWithCallbackQuery();
        //TODO Доделать тест, когда появится логика обработки сообщений
        assertNull(messageProcessor.processMessage(update));
    }

    @Test
    void processMessage_withoutMember() throws TelegramApiException {
        memberServiceMock.mockIsExistsByChatId(false);

        Update update = getUpdateWithMessage();
        assertNull(messageProcessor.processMessage(update));
    }
}