package dev.reviewbot2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Config {

    @Value("bot.name")
    public String BOT_NAME;

    @Value("bot.token")
    public String BOT_TOKEN;

    @Value("jira.link")
    public String JIRA_LINK;

    @Value("jira.dashboards")
    public List<String> DASHBOARDS;
}
