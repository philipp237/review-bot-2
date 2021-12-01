package dev.reviewbot2.config;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class Config {

    @Value("bot.name")
    public static String BOT_NAME;

    @Value("bot.token")
    public static String BOT_TOKEN;

    @Value("jira.link")
    public static String JIRA_LINK;

    @Value("jira.dashboards")
    public static List<String> DASHBOARDS;

}
