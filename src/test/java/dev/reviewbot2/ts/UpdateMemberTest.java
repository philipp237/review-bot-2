package dev.reviewbot2.ts;

import dev.reviewbot2.AbstractUnitTest;
import dev.reviewbot2.app.impl.ts.UpdateMemberTransactionScript;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.exceptions.NoPermissionException;
import dev.reviewbot2.mock.MemberServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.reviewbot2.processor.Command.UPDATE_MEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class UpdateMemberTest extends AbstractUnitTest {
    private UpdateMemberTransactionScript updateMember;

    @BeforeEach
    void setUp() {
        closeable = openMocks(this);
        updateMember = new UpdateMemberTransactionScript(memberService);
        memberServiceMock = new MemberServiceMock(memberService);
    }

    @Test
    void happyPath() {
        String memberChatId = MEMBER_1_CHAT_ID;
        String updatingMemberChatId = MEMBER_2_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);
        Member memberForUpdate = getMember(updatingMemberChatId, FIRST_REVIEW_GROUP, false, false);

        String updateMessageText = "/" + UPDATE_MEMBER + "#" + updatingMemberChatId + "#" + 2;

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, updateMessageText);

        memberServiceMock.mockGetMemberByChatId(member, memberForUpdate);

        updateMember.execute(messageInfo);

        verify(memberService, times(1)).save(memberArgumentCaptor.capture());
        assertEquals(2, memberArgumentCaptor.getValue().getReviewGroup());
    }

    @Test
    void happyPath_messageWithoutReviewGroup() {
        String memberChatId = MEMBER_1_CHAT_ID;
        String updatingMemberChatId = MEMBER_2_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);
        Member memberForUpdate = getMember(updatingMemberChatId, FIRST_REVIEW_GROUP, false, false);

        String updateMessageText = "/" + UPDATE_MEMBER + "#" + updatingMemberChatId;

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, updateMessageText);

        memberServiceMock.mockGetMemberByChatId(member, memberForUpdate);

        SendMessage updateMessage = updateMember.execute(messageInfo);
        assertEquals("Выбери группу ревью для @" + LOGIN, updateMessage.getText());
    }

    @Test
    void happyPath_messageWithoutMember() {
        String memberChatId = MEMBER_1_CHAT_ID;
        String member2ChatId = MEMBER_2_CHAT_ID;
        String member3ChatId = MEMBER_3_CHAT_ID;

        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, true);
        List<Member> notOmniMembers = Stream.of(
            getMember(member2ChatId, FIRST_REVIEW_GROUP, false, false),
            getMember(member3ChatId, SECOND_REVIEW_GROUP, false, false)
        ).collect(Collectors.toList());

        String updateMessageText = "/" + UPDATE_MEMBER;

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, updateMessageText);

        memberServiceMock.mockGetMemberByChatId(member);
        memberServiceMock.mockGetAllNotOmniMembers(notOmniMembers);

        SendMessage updateMessage = updateMember.execute(messageInfo);
        assertEquals(2, ((InlineKeyboardMarkup) updateMessage.getReplyMarkup()).getKeyboard().size());
        assertEquals("/" + UPDATE_MEMBER + "#" + member2ChatId,
            ((InlineKeyboardMarkup) updateMessage.getReplyMarkup()).getKeyboard().get(0).get(0).getCallbackData());
        assertEquals("/" + UPDATE_MEMBER + "#" + member3ChatId,
            ((InlineKeyboardMarkup) updateMessage.getReplyMarkup()).getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void updateMember_notOmniMember() {
        String memberChatId = MEMBER_1_CHAT_ID;
        Member member = getMember(memberChatId, FIRST_REVIEW_GROUP, false, false);

        MessageInfo messageInfo = getSimpleMessageInfo(memberChatId, "/" + UPDATE_MEMBER);

        memberServiceMock.mockGetMemberByChatId(member);

        assertThrows(NoPermissionException.class, () -> updateMember.execute(messageInfo));
    }
}
