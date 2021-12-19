package dev.reviewbot2;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.app.api.ReviewService;
import dev.reviewbot2.app.api.TaskService;
import dev.reviewbot2.app.api.UpdateService;
import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.processor.CommandProcessor;
import dev.reviewbot2.processor.MessageProcessor;
import dev.reviewbot2.processor.TelegramBot;
import dev.reviewbot2.repository.MemberRepository;
import dev.reviewbot2.repository.ReviewRepository;
import dev.reviewbot2.repository.TaskRepository;
import dev.reviewbot2.webhook.WebhookRestClient;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest("ReveiwBot2Application")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest extends AbstractTest {
    protected final String PROCESS_NAME = "review-process";

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
    private MemberRepository memberRepository;
    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UpdateService updateService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ReviewService reviewService;

    @Autowired
    protected HistoryService historyService;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected RepositoryService repositoryService;

    @MockBean
    WebhookRestClient webhookRestClient;

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

        reviewRepository.deleteAll();
        taskRepository.deleteAll();
        memberRepository.deleteAll();
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

    protected Member getMemberFromDB(int reviewGroup, boolean canReviewDesign, boolean isOmni) {
        return memberRepository.save(getMember(reviewGroup, canReviewDesign, isOmni));
    }

    protected String getUuidFromProcess() {
        return historyService.createHistoricProcessInstanceQuery()
            .processDefinitionName(PROCESS_NAME)
            .singleResult()
            .getBusinessKey();
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
