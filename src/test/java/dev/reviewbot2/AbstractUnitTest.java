package dev.reviewbot2;

import dev.reviewbot2.app.impl.camunda.ProcessAccessor;
import dev.reviewbot2.config.Config;
import org.mockito.Mock;

public abstract class AbstractUnitTest extends AbstractTest {
    protected final Config config;

    @Mock
    protected ProcessAccessor processAccessor;

    public AbstractUnitTest() {
        Config config = new Config();
        config.BOT_NAME = BOT_NAME;
        config.BOT_TOKEN = BOT_TOKEN;
        config.JIRA_LINK = JIRA_LINK;
        config.DASHBOARDS = DASHBOARD;
        this.config = config;
    }
}
