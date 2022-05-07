package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateChatIdTransactionScript {
    private final MemberService memberService;

    @Transactional
    public void execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        String login = messageInfo.getLogin();

        Member member = memberService.getMemberByLogin(login);
        member.setChatId(chatId);
        memberService.save(member);

        log.info("Chat id for {} was updated to {}", login, chatId);
    }
}
