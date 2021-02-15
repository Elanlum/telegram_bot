package com.elanlum.ecs.bot.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.bot.TelegramBotEcs;
import com.elanlum.ecs.bot.TelegramBotProperties;
import com.elanlum.ecs.utils.TestCategory;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = IntegrationTestsConfig.class)
public class BotCommandsHandlerIntegrationTest {

  @Autowired
  private BotCommandsHandler handler;
  @Autowired
  private ReactiveMongoTemplate reactiveMongoTemplate;

  private TelegramBotProperties properties = new TelegramBotProperties();

  {
    properties.setUserName("ECSTelegrambot");
    properties.setToken("684937911:AAHYiAXEHwioMJWGtrc0oiNCzzWYH1zzsjU");
    properties.setBaseUrl("https://api.telegram.org/bot");
  }

  @Test
  @DisplayName("Save in DB test with botCommandsHandler class")
  void whenUpdateStart_thenSaveUser_byCommandHandlerClass() {
    Update update = mock(Update.class);
    Message mockMessage = mock(Message.class);
    User telegramUser = mock(User.class);

    when(mockMessage.getFrom()).thenReturn(telegramUser);
    when(telegramUser.getFirstName()).thenReturn("User");
    when(telegramUser.getUserName()).thenReturn("UserUsername");
    when(telegramUser.getId()).thenReturn(111111111);
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("/start");
    when(mockMessage.hasText()).thenReturn(true);
    when(mockMessage.getChatId()).thenReturn(-0L);

    handler.handle(update).blockFirst();

    StepVerifier.create(reactiveMongoTemplate
        .findAll(com.elanlum.ecs.user.model.User.class))
        .assertNext(userInDB -> assertEquals("111111111", userInDB.getTelegramId()))
        .verifyComplete();
  }

  /* This test may be used for checking real sending by TelegramBot.
     You must create a real chat and pass it ChatID to the mock for getChatId method.
     And then change mock for execute method by moving "doAnswer" after "when".
     Also create SendMessage and pass it to "execute".
     Dot't leave real chat id in test.
    */
  @Test
  @DisplayName("Save in DB test with Telegram")
  void whenUpdateStart_thenSaveUser() throws InterruptedException, TelegramApiException {
    Semaphore telegramSemaphore = new Semaphore(0);
    Update update = mock(Update.class);
    Message mockMessage = mock(Message.class);
    User telegramUser = mock(User.class);

    TelegramBotEcs bot = spy(new TelegramBotEcs(handler, properties));
    when(mockMessage.getFrom()).thenReturn(telegramUser);
    when(telegramUser.getFirstName()).thenReturn("User");
    when(telegramUser.getUserName()).thenReturn("UserUsername");
    when(telegramUser.getId()).thenReturn(111111111);
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("/start");
    when(mockMessage.hasText()).thenReturn(true);
    when(mockMessage.getChatId()).thenReturn(-0L);  // pass real ChatID here
    doAnswer(invocation -> {
      telegramSemaphore.release();
      return mockMessage;
    }).when(bot).execute(any(SendMessage.class));

    bot.onUpdateReceived(update);

    boolean acquired = telegramSemaphore.tryAcquire(2, TimeUnit.SECONDS);

    assertTrue(acquired);

    StepVerifier.create(reactiveMongoTemplate
        .findAll(com.elanlum.ecs.user.model.User.class))
        .assertNext(userInDB -> assertEquals("111111111", userInDB.getTelegramId()))
        .verifyComplete();

    verify(bot).execute(any(SendMessage.class));
  }
}
