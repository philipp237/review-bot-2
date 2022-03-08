package dev.reviewbot2.app.impl;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Override
    public boolean isChatIdExists(String chatId) {
        return memberRepository.existsByChatId(chatId);
    }

    @Override
    public boolean isLoginExists(String login) {
        return memberRepository.existsByLogin(login);
    }

    @Override
    public Member getMemberByChatId(String chatId) {
        return memberRepository.getMemberByChatId(chatId);
    }

    @Override
    public Member getMemberByLogin(String login) {
        return memberRepository.getMemberByLogin(login);
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public List<Member> getAllNotOmniMembers() {
        return memberRepository.getMembersByIsOmniFalse();
    }

    @Override
    public List<Member> getMemberByReviewGroup(int reviewGroup) {
        return memberRepository.getAllByReviewGroup(reviewGroup);
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public List<Member> getOmniMembers() {
        return memberRepository.getMembersByIsOmniTrue();
    }
}
