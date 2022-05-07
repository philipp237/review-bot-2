package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.exceptions.NoPermissionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.transaction.Transactional;

import static dev.reviewbot2.processor.Utils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddMemberTransactionScript {
    private final MemberService memberService;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String text = messageInfo.getText();

        Member member = memberService.getMemberByChatId(chatId);

        validateOmni(messageInfo, member);

        if (text.contains("#")) {
            String newMemberLogin = getNewMemberLogin(text);
            Member newMember = Member.builder()
                .login(newMemberLogin)
                .canReviewDesign(false)
                .isOmni(false)
                .reviewGroup(0)
                .build();
            memberService.save(newMember);

            log.info("{} add new member: {}", member.getLogin(), newMemberLogin);
            return sendMessage(chatId, String.format("Новый пользователь %s добавлен в базу данных", newMemberLogin));
        }

        return sendMessage(chatId, "Используй команду /add_member#логин_нового_пользователя для добавления " +
            "нового пользователя");
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private String getNewMemberLogin(String text) {
        String[] parsedText = text.split("#");
        return parsedText[parsedText.length - 1];
    }

    private void validateOmni(MessageInfo messageInfo, Member member) {
        if (!member.getIsOmni()) {
            throw new NoPermissionException(messageInfo);
        }
    }
}
