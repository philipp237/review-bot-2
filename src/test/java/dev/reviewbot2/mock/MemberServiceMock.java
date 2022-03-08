package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.member.Member;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class MemberServiceMock {
    private boolean switcher = true;

    private final MemberService memberService;

    public void mockGetMemberByChatId(Member member) {
        doReturn(member).when(memberService).getMemberByChatId(anyString());
    }

    public void mockGetMemberByChatId(Member member1, Member member2) {
        if (switcher) {
            doReturn(member1).when(memberService).getMemberByChatId(anyString());
        } else {
            doReturn(member2).when(memberService).getMemberByChatId(anyString());
        }
        switcher = !switcher;
    }

    public void mockGetAllMembers(List<Member> members) {
        doReturn(members).when(memberService).getAllMembers();
    }

    public void mockGetAllNotOmniMembers(List<Member> members) {
        doReturn(members).when(memberService).getAllNotOmniMembers();
    }
}
