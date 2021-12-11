package dev.reviewbot2.service;

import dev.reviewbot2.app.api.MemberService;
import lombok.RequiredArgsConstructor;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@RequiredArgsConstructor
public class MemberServiceMock {
    private final MemberService memberService;

    public void mockIsExistsByChatId(boolean expectedResult) {
        doReturn(expectedResult).when(memberService).isExists(anyString());
    }
}
