package com.elanlum.ecs.bot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "telegrambot")
public class TelegramBotProperties {

  private String userName;

  private String token;

  private String baseUrl;


}
