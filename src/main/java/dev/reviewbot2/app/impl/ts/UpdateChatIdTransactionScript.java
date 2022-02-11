package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateChatIdTransactionScript {
    private final MemberService memberService;

    public void execute(String chatId, String login) {
        Member member = memberService.getMemberByLogin(login);
        member.setChatId(chatId);
        memberService.save(member);

        log.info("Chat id for {} was updated to {}", login, chatId);
    }
}
