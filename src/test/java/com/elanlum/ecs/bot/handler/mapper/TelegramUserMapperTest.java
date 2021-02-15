package com.elanlum.ecs.bot.handler.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestCategory.UNIT)
class TelegramUserMapperTest {

  @Test
  void map() {
    var telegramUser = mock(org.telegram.telegrambots.meta.api.objects.User.class);

    when(telegramUser.getId()).thenReturn(1);
    when(telegramUser.getUserName()).thenReturn("Vasya");
    when(telegramUser.getFirstName()).thenReturn("Vasiliy");

    TelegramUserMapper telegramUserMapper = new TelegramUserMapper();
    User returnedUser = telegramUserMapper.map(telegramUser, 1L);

    assertEquals(returnedUser.getTelegramId(), "1");
    assertEquals(returnedUser.getLogin(), "Vasya");
    assertEquals(returnedUser.getName(), "Vasiliy");
    assertEquals(returnedUser.getTelegramChatId(), Long.valueOf(1L));
    assertNull(returnedUser.getId());
  }
}