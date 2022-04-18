package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.GetStartMessageTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.mock.MemberServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.stream.Stream;

import static dev.reviewbot2.processor.Command.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

public class GetStartMessageTest extends AbstractUnitTest {
    protected static final String COMMAND = "/%s";

    private static final List<String> AVAILABLE_COMMANDS =
        Stream.of(String.format(COMMAND, CREATE_TASK.toString()),
        String.format(COMMAND, TAKE_IN_REVIEW.toString()),
        String.format(COMMAND, MY_REVIEWS.toString()),
        String.format(COMMAND, MY_TASKS.toString()))
        .collect(toList());
    private static final List<String> AVAILABLE_COMMANDS_FOR_OMNI =
        Stream.of(String.format(COMMAND, CREATE_TASK.toString()),
        String.format(COMMAND, TAKE_IN_REVIEW.toString()),
        String.format(COMMAND, MY_REVIEWS.toString()),
        String.format(COMMAND, MY_TASKS.toString()),
        String.format(COMMAND, ADD_MEMBER.toString()),
        String.format(COMMAND, UPDATE_MEMBER.toString()))
        .collect(toList());

    private GetStartMessageTransactionScript getStartMessage;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        getStartMessage = new GetStartMessageTransactionScript(memberService);
        memberServiceMock = new MemberServiceMock(memberService);
    }

    @Test
    void happyPath() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);

        Update update = getUpdateWithCallbackQuery("/start", memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);

        SendMessage startMessage = getStartMessage.execute(update);

        List<List<InlineKeyboardButton>> keyboard = ((InlineKeyboardMarkup) startMessage.getReplyMarkup()).getKeyboard();
        assertEquals(4, keyboard.size());
        assertTrue(keyboard.stream().allMatch(row -> AVAILABLE_COMMANDS.contains(row.get(0).getCallbackData())));
    }

    @Test
    void happyPath_omni() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);

        Update update = getUpdateWithCallbackQuery("/start", memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);

        SendMessage startMessage = getStartMessage.execute(update);

        List<List<InlineKeyboardButton>> keyboard = ((InlineKeyboardMarkup) startMessage.getReplyMarkup()).getKeyboard();
        assertEquals(6, keyboard.size());
        assertTrue(keyboard.stream().allMatch(row -> AVAILABLE_COMMANDS_FOR_OMNI.contains(row.get(0).getCallbackData())));
    }
}
