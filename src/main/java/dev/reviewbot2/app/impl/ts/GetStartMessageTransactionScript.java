package dev.reviewbot2.app.impl.ts;

import dev.reviewbot2.app.api.MemberService;
import dev.reviewbot2.domain.MessageInfo;
import dev.reviewbot2.domain.member.Member;
import dev.reviewbot2.processor.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.reviewbot2.processor.Command.*;
import static dev.reviewbot2.utils.UpdateUtils.*;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class GetStartMessageTransactionScript {
    private static final List<Command> AVAILABLE_COMMANDS_FROM_START =
        Stream.of(CREATE_TASK, TAKE_IN_REVIEW, MY_REVIEWS, MY_TASKS, SPRINT)
        .collect(toList());
    private static final List<Command> ADDITIONAL_COMMANDS_FOR_OMNI = Stream.of(ADD_MEMBER, UPDATE_MEMBER,
        CLOSED_TASKS, INCORPORATE)
        .collect(toList());

    private final MemberService memberService;

    @Transactional
    public SendMessage execute(MessageInfo messageInfo) {
        String chatId = messageInfo.getChatId();
        Member member = memberService.getMemberByChatId(chatId);

        List<Command> availableCommandsFromStart = getAvailableCommands(member.getIsOmni());

        InlineKeyboardMarkup keyboard = getKeyboard(availableCommandsFromStart.size());
        fillKeyboardWithCommands(keyboard, availableCommandsFromStart);
        return sendMessage(chatId, "Выбери действие:", keyboard);
    }

    // ================================================================================================================
    //  Implementation
    // ================================================================================================================

    private List<Command> getAvailableCommands(Boolean isOmni) {
        List<Command> availableCommandsFromStart = new ArrayList<>(AVAILABLE_COMMANDS_FROM_START);

        if (isOmni) {
            availableCommandsFromStart.addAll(ADDITIONAL_COMMANDS_FOR_OMNI);
        }

        return availableCommandsFromStart;
    }

    private void fillKeyboardWithCommands(InlineKeyboardMarkup keyboard, List<Command> commands) {
        int i = 0;

        for (Command command : commands) {
            keyboard.getKeyboard().get(i).add(getButton(command.getButtonText(), "/" + command));
            i++;
        }
    }
}
