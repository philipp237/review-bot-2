package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Override
    public boolean isExists(String chatId) {
        return memberRepository.existsByChatId(chatId);
    }

    @Override
    public Member getMemberByChatId(String chatId) {
        return memberRepository.getMemberByChatId(chatId);
    }
}
