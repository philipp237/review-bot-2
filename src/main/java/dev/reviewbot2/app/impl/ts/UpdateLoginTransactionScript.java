package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateLoginTransactionScript {
    private final MemberRepository memberRepository;

    @Transactional
    public void execute(String chatId, String login) {
        Member member = memberRepository.getMemberByChatId(chatId);
        String oldLogin = member.getLogin();
        member.setLogin(login);
        memberRepository.save(member);

        log.info("{} changed login to {}", oldLogin, login);
    }
}
