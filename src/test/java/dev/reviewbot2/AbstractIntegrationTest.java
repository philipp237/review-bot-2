package dev.reviewbot2;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.domain.task.TaskType;
import dev.reviewbot2.processor.CommandProcessor;
import dev.reviewbot2.processor.MessageProcessor;
import dev.reviewbot2.processor.TelegramBot;
import dev.reviewbot2.repository.MemberRepository;
import dev.reviewbot2.repository.MemberReviewRepository;
import dev.reviewbot2.repository.ReviewRepository;
import dev.reviewbot2.repository.TaskRepository;
import dev.reviewbot2.webhook.WebhookRestClient;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Collectors;

import static dev.reviewbot2.processor.Command.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest("ReveiwBot2Application")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private MessageProcessor messageProcessor;
    @Autowired
    private CommandProcessor commandProcessor;

    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    protected ReviewRepository reviewRepository;
    @Autowired
    protected MemberReviewRepository memberReviewRepository;

    @Autowired
    protected HistoryService historyService;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected RepositoryService repositoryService;

    @Captor
    protected ArgumentCaptor<SendMessage> sendMessageArgumentCaptor;

    @MockBean
    protected WebhookRestClient webhookRestClient;

    @BeforeEach
    void clearDB() {
        doWithCamundaRetry(() -> {
            List<String> processIds = runtimeService.createProcessInstanceQuery()
                .rootProcessInstances()
                .list()
                .stream()
                .map(Execution::getId)
                .collect(Collectors.toList());
            if (!processIds.isEmpty()) {
                runtimeService.deleteProcessInstances(processIds, "cleanup", true, true);
            }
        });

        doWithCamundaRetry(() -> {
            List<String> historicProcessIds =
                historyService.createHistoricProcessInstanceQuery()
                    .rootProcessInstances()
                    .list()
                    .stream()
                    .map(HistoricProcessInstance::getId)
                    .collect(Collectors.toList());
            if (!historicProcessIds.isEmpty()) {
                historyService.deleteHistoricProcessInstances(historicProcessIds);
            }
        });

        memberReviewRepository.deleteAll();
        reviewRepository.deleteAll();
        taskRepository.deleteAll();
        memberRepository.deleteAll();
    }

    protected SendMessage performCreateTask(String chatId, TaskType taskType) throws Exception {
        Update update = getUpdateWithMessage(JIRA_LINK + TASK_NAME_1 + "#" + taskType, chatId);
        Member member = getMemberFromDB(chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performTakeInReview(String chatId, TaskType taskType, int reviewGroup, Long taskId) throws Exception {
        Update update = getUpdateWithCallbackQuery("/" + TAKE_IN_REVIEW + "#" + taskId, chatId);
        Member reviewer = getMemberFromDB(chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performAcceptReview(String chatId, Long taskId) throws Exception {
        Update update = getUpdateWithCallbackQuery("/" + ACCEPT_REVIEW + "#" + taskId, chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performApprove(String chatId, Long taskId) throws Exception {
        Update update = getUpdateWithCallbackQuery("/" + APPROVE + "#" + taskId, chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performDecline(String chatId, Long taskId) throws Exception {
        Update update = getUpdateWithCallbackQuery("/" + DECLINE + "#" + taskId, chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performSubmit(String chatId, Long taskId) throws Exception {
        Update update = getUpdateWithCallbackQuery("/" + SUBMIT + "#" + taskId, chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performClose(String chatId, Long taskId) throws Exception {
        Update update = getUpdateWithCallbackQuery("/" + CLOSE + "#" + taskId, chatId);

        return performUpdateReceived(update);
    }

    protected SendMessage performUpdateReceived(Update update) throws Exception {
        MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andReturn();

        String content = result.getResponse().getContentAsString();
        return objectMapper.readValue(content, SendMessage.class);
    }

    protected void addMembersToDB(String chatId, int reviewGroup, boolean canReviewDesign, boolean isOmni) {
        memberRepository.save(getMember(chatId, reviewGroup, canReviewDesign, isOmni));
    }

    protected Member getMemberFromDB(String chatId) {
        return memberRepository.getMemberByChatId(chatId);
    }

    protected String getUuidFromProcess() throws InterruptedException {
        Thread.sleep(100);
        return runtimeService.createVariableInstanceQuery()
            .variableName("taskUuid")
            .singleResult()
            .getValue().toString();
    }

    protected void mockSendMessage() throws TelegramApiException {
        Mockito.doNothing().when(webhookRestClient).sendMessage(any());
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void doWithCamundaRetry(Runnable action) {
        int retry = 3;
        while (true) {
            try {
                action.run();
                break;
            } catch (OptimisticLockingException e) {
                if (retry-- <= 0) {
                    throw e;
                }
            }
        }
    }
}
