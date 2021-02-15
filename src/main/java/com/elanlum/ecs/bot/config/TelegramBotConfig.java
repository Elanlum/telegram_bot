package com.elanlum.ecs.bot.config;

import com.elanlum.ecs.bot.TelegramBotEcs;
import com.elanlum.ecs.bot.TelegramBotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramBotConfig {

  static {
    ApiContextInitializer.init();
  }

  /**
   * Bot bean.
   */
  @Bean
  public TelegramBotsApi getApi(TelegramBotEcs bot) throws TelegramApiRequestException {

    TelegramBotsApi botsApi = new TelegramBotsApi();
    botsApi.registerBot(bot);
    return botsApi;
  }
}
