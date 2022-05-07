package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.AddMemberTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.exceptions.NoPermissionException;
import dev.reviewbot2.mock.MemberServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static dev.reviewbot2.processor.Command.ADD_MEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void happyPath() {
        String memberChatId = MEMBER_1_CHAT_ID;
        String newUserLogin = "newUser";
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, "/" + ADD_MEMBER + "#" + newUserLogin);

        memberServiceMock.mockGetMemberByChatId(member);

        addMember.execute(messageInfo);

        verify(memberService, times(1)).save(memberArgumentCaptor.capture());
        assertEquals(newUserLogin, memberArgumentCaptor.getValue().getLogin());
    }

    @Test
    void happyPath_withoutParameter() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, "/" + ADD_MEMBER);

        memberServiceMock.mockGetMemberByChatId(member);

        SendMessage addMemberMessage = addMember.execute(messageInfo);

        assertEquals("Используй команду /add_member#логин_нового_пользователя для добавления " +
            "нового пользователя", addMemberMessage.getText());
    }

    @Test
    void addMember_error_notOmniMember() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, "/" + ADD_MEMBER);

        memberServiceMock.mockGetMemberByChatId(member);

        assertThrows(NoPermissionException.class, () -> addMember.execute(messageInfo));
    }
}
