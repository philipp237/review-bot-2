package dev.reviewbot2;

import org.telegram.telegrambots.meta.api.objects.*;

import java.util.List;

public abstract class AbstractUnitTest {

    protected static final int MESSAGE_ID = 51632862;
    protected static final int UPDATE_ID = 42368635;
    protected static final long CHAT_ID = 57164325;
    protected static final String JIRA_LINK = "https://test.com/";
    protected static final List<String> DASHBOARD = List.of("TEST1", "TEST2");

    protected Update getUpdateWithoutMessage() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        return update;
    }

    protected Update getUpdateWithMessage() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setMessage(getMessage());
        return update;
    }

    protected Update getUpdateWithCallbackQuery() {
        Update update = new Update();
        update.setUpdateId(UPDATE_ID);
        update.setCallbackQuery(getCallbackQuery());
        return update;
    }

    protected CallbackQuery getCallbackQuery() {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setMessage(getMessage());
        return callbackQuery;
    }

    protected Message getMessage() {
        Message message = new Message();
        message.setMessageId(MESSAGE_ID);
        message.setText(JIRA_LINK + DASHBOARD.get(0));
        message.setFrom(getUser());
        message.setChat(getChat());
        return message;
    }

    protected User getUser() {
        User user = new User();
        user.setUserName("testUserName");
        return user;
    }

    protected Chat getChat() {
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        return chat;
    }
}
