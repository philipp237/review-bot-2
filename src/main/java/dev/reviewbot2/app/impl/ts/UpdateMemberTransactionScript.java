package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.exceptions.NoPermissionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static dev.reviewbot2.processor.Command.UPDATE_MEMBER;
import static dev.reviewbot2.processor.Utils.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateMemberTransactionScript {
    private final MemberService memberService;

    @Value("${settings.number-of-review-stages}")
    private int numberOfReviewStages;

    public SendMessage execute(Update update) throws TelegramApiException {
        String chatId = getChatId(update);
        String text =  getTextFromUpdate(update);

        Member member = memberService.getMemberByChatId(chatId);

        validateOmni(update, member);

        if (text.contains("#")) {
            String[] parsedText = text.split("#");
            String chatIdForUpdateMember = parsedText[1];
            Member memberForUpdate = memberService.getMemberByChatId(chatIdForUpdateMember);

            if (parsedText.length == 2) {
                InlineKeyboardMarkup keyboard = getKeyboard(numberOfReviewStages + 1);
                fillKeyboardWithReviewStages(keyboard, text);
                return sendMessage(chatId, "Выбери группу ревью для @" + memberForUpdate.getLogin(), keyboard);
            } else {
                int reviewGroup = getReviewGroup(parsedText);

                member.setReviewGroup(reviewGroup);
                memberService.save(member);

                log.info("{} transfered member {} to {} review group", member.getLogin(), memberForUpdate.getLogin(), reviewGroup);
                return sendMessage(chatId, String.format("@%s переведен в %d группу ревью", memberForUpdate.getLogin(), reviewGroup));
            }
        }

        List<Member> members = memberService.getAllNotOmniMembers();
        InlineKeyboardMarkup keyboard = getKeyboard(members.size());
        fillKeyboardWithMembers(keyboard, members);

        return sendMessage(chatId, "Выбери пользователя для изменения его группы ревью", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private void validateOmni(Update update, Member member) {
        if (!member.getIsOmni()) {
            throw new NoPermissionException(update);
        }
    }

    private int getReviewGroup(String[] parsedText) {
        return Integer.parseInt(parsedText[parsedText.length - 1]);
    }

    private void fillKeyboardWithReviewStages(InlineKeyboardMarkup keyboard, String text) {
        keyboard.getKeyboard().get(0).add(getButton("Не может ревьюить", text + "#0"));

        for (int i = 1; i <= numberOfReviewStages; i++) {
            keyboard.getKeyboard().get(i).add(getButton(i + " группа", text + "#" + i));
        }
    }

    private void fillKeyboardWithMembers(InlineKeyboardMarkup keyboard, List<Member> members) {
        int i = 0;

        for (Member member : members) {
            keyboard.getKeyboard().get(i).add(getButton(member.getLogin(), "/" + UPDATE_MEMBER + "#" + member.getChatId()));
            i++;
        }
    }
}
