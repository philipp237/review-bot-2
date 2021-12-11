package dev.reviewbot2.config;

import org.springframework.beans.factory.annotation.Value;

public class Config {

    @Value("bot.name")
    public static String BOT_NAME;

    @Value("bot.token")
    public static String BOT_TOKEN;
}
