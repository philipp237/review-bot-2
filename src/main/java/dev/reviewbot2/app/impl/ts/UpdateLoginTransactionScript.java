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
public class UpdateLoginTransactionScript {
    private final MemberService memberService;

    @Transactional
    public void execute(MessageInfo messageInfo) {
        String login = messageInfo.getLogin();

        Member member = memberService.getMemberByChatId(messageInfo.getChatId());
        String oldLogin = member.getLogin();
        member.setLogin(login);
        memberService.save(member);

        log.info("{} changed login to {}", oldLogin, login);
    }
}
