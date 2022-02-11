package dev.reviewbot2.mock;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.member.Member;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class MemberServiceMock {
    private final MemberService memberService;

    public void mockIsExistsByChatId(boolean expectedResult) {
        doReturn(expectedResult).when(memberService).isChatIdExists(anyString());
    }

    public void mockGetMemberByChatId(Member member) {
        doReturn(member).when(memberService).getMemberByChatId(anyString());
    }

    public void mockGetAllMembers(List<Member> members) {
        doReturn(members).when(memberService).getAllMembers();
    }
}
