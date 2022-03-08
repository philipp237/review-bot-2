package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.AddMemberTransactionScript;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.mock.MemberServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static dev.reviewbot2.processor.Command.ADD_MEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class AddMemberTest extends AbstractUnitTest {
    private AddMemberTransactionScript addMember;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        addMember = new AddMemberTransactionScript(memberService);
        memberServiceMock = new MemberServiceMock(memberService);
    }

    @Test
    void happyPath() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        String newUserLogin = "newUser";
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);

        Update update = getUpdateWithCallbackQuery("/" + ADD_MEMBER + "#" + newUserLogin, memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);

        addMember.execute(update);

        verify(memberService, times(1)).save(memberArgumentCaptor.capture());
        assertEquals(newUserLogin, memberArgumentCaptor.getValue().getLogin());
    }

    @Test
    void happyPath_withoutParameter() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);

        Update update = getUpdateWithCallbackQuery("/" + ADD_MEMBER, memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);

        SendMessage addMemberMessage = addMember.execute(update);

        assertEquals("Используй команду /add_member#логин_нового_пользователя для добавления " +
            "нового пользователя", addMemberMessage.getText());
    }

    @Test
    void addMember_error_notOmniMember() throws TelegramApiException {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);

        Update update = getUpdateWithCallbackQuery("/" + ADD_MEMBER, memberChatId);

        memberServiceMock.mockGetMemberByChatId(member);

        SendMessage addMemberMessage = addMember.execute(update);

        assertEquals("Нет прав", addMemberMessage.getText());
    }
}
