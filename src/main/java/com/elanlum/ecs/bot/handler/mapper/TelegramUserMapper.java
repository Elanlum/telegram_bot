package com.elanlum.ecs.bot.handler.mapper;

import com.elanlum.ecs.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class TelegramUserMapper {

  /**
   * Helping method that create User for our App{@link User} from {@link
   * org.telegram.telegrambots.meta.api.objects.User} object.
   *
   * @param telegramUser - class User from Telegram API
   * @param chatId is an id from current chat with our bot
   * @return User from our App with necessary fields from telegramUser and id = null
   */
  public User map(org.telegram.telegrambots.meta.api.objects.User telegramUser, Long chatId) {
    String login = telegramUser.getUserName();
    String name = telegramUser.getFirstName();
    String telegramId = String.valueOf(telegramUser.getId());

    return new User(null, login, name, telegramId, chatId);
  }
}
