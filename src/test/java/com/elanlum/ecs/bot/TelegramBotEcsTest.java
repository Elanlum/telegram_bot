package com.elanlum.ecs.bot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.bot.handler.BotCommandsHandler;
import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;

@Tag(TestCategory.UNIT)
class TelegramBotEcsTest {

  private String botname = "ECSTelegrambot";
  private String bottoken = "684937911:AAHYiAXEHwioMJWGtrc0oiNCzzWYH1zzsjU";
  private String botUrl = "https://api.telegram.org/bot";
  private Update update = mock(Update.class);
  private Message mockMessage = mock(Message.class);
  private BotCommandsHandler handler = mock(BotCommandsHandler.class);
  private TelegramBotProperties properties = new TelegramBotProperties();

  {
    properties.setUserName("ECSTelegrambot");
    properties.setToken("684937911:AAHYiAXEHwioMJWGtrc0oiNCzzWYH1zzsjU");
    properties.setBaseUrl("https://api.telegram.org/bot");
  }

  private TelegramBotEcs bot = Mockito.spy(new TelegramBotEcs(handler, properties));

  @Test
  void onUpdateReceivedTest() {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("aaaaa");
    when(mockMessage.hasText()).thenReturn(true);
    BotApiMethod sendMessage = new SendMessage()
        .setText("This command not supported, please type /help for a list of available commands");
    doReturn(Flux.just(sendMessage)).when(handler).handle(update);
    try {
      doReturn(mockMessage).when(bot).execute(sendMessage);

      bot.onUpdateReceived(update);
      verify(handler).handle(update);
      verify(bot).execute(sendMessage);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Test
  void onUpdateReceivedTestFailed() throws TelegramApiException {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("aaaaa");
    when(mockMessage.hasText()).thenReturn(true);
    SendMessage sendMessage = new SendMessage()
        .setChatId(1L)
        .setText("This command not supported, please type /help for a list of available commands");
    doReturn(Flux.just(sendMessage)).when(handler).handle(update);
    doThrow(new TelegramApiException()).when(bot).execute(sendMessage);

    bot.onUpdateReceived(update);
    verify(handler).handle(update);
    verify(bot, times(1)).execute(any(SendMessage.class));
  }

  @Test
  void getBotUsernameTest() {
    assertThat(bot.getBotUsername(), is(botname));
  }

  @Test
  void getBotTokenTest() {
    assertThat(bot.getBotToken(), is(bottoken));
  }

  @Test
  void getBotUrlTest() {
    assertThat(bot.getOptions().getBaseUrl(), is(botUrl));
  }
}
