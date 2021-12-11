package dev.reviewbot2.service;

import dev.reviewbot2.app.api.MemberService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

public class MemberServiceMock {
    private final MemberService memberService;

    public MemberServiceMock(MemberService memberService) {
        this.memberService = memberService;
    }

    public void mockIsExistsByChatId(boolean expectedResult) {
        doReturn(expectedResult).when(memberService).isExists(anyString());
    }
}
