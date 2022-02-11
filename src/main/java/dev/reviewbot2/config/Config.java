package dev.reviewbot2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Config {

    @Value("${bot.name}")
    public String BOT_NAME;

    @Value("${bot.token}")
    public String BOT_TOKEN;

    @Value("${jira.link}")
    public String JIRA_LINK;

    @Setter
    @Value("${jira.dashboards}")
    private String DASHBOARDS;

    public List<String> getDASHBOARDS() {
        return Arrays.asList(DASHBOARDS.split(" "));
    }
}
