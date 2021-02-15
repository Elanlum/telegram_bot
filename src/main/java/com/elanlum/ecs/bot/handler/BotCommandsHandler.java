package com.elanlum.ecs.bot.handler;

import static com.elanlum.ecs.bot.util.ConstantStorage.helpCommand;
import static com.elanlum.ecs.bot.util.ConstantStorage.startCommand;

import com.elanlum.ecs.bot.handler.mapper.TelegramUserMapper;
import com.elanlum.ecs.bot.button.ButtonCallback;
import com.elanlum.ecs.bot.button.InitialButtons;
import com.elanlum.ecs.bot.button.StringParser;
import com.elanlum.ecs.bot.util.ConstantStorage;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.ride.exceptions.FeedbackUpdateException;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BotCommandsHandler {

  private static final String RED_CROSS_EMOJI = "\u274c";//red cross emoji
  private static final String GREEN_CHECK_MARK_EMOJI = "\u2705";//green check mark emoji
  private static final String ENVELOPE_EMOJI = "\u2709";//envelope emoji
  private final UserService userService;
  private final TelegramUserMapper telegramUserMapper;
  private final GeneralContextCommandsHandler generalContextCommandsHandler;
  private final InitialButtons initialButtons;
  private final RideService rideService;
  private final StringParser parser;

  /**
   * Handle method for bot.
   */
  public Flux<? extends BotApiMethod> handle(Update update) {
    List<Mono<? extends BotApiMethod>> listMes = new ArrayList<>();
    if (update.hasCallbackQuery()) {
      listMes.add(handleFeedback(update));
      return Flux.merge(listMes).onErrorResume(error -> Mono.just(new AnswerCallbackQuery()
          .setText(error.getMessage())));
    }

    var message = update.getMessage();

    if (Objects.equals(message.getText(), helpCommand)) {
      var sendMessage = handleHelp(update);
      listMes.add(sendMessage);
    }
    if (Objects.equals(message.getText(), startCommand)) {
      var sendMessage = handleStart(update);
      listMes.add(sendMessage);
    }
    var sendMessage = handleOtherCommand(update);
    listMes.add(sendMessage);

    return Flux.merge(listMes)
        .onErrorResume(error -> Mono.just(new SendMessage()
            .setChatId(update.getMessage().getChatId())
            .setText(error.getMessage())));
  }

  protected Mono<? extends BotApiMethod> handleFeedback(Update update) {
    CallbackQuery callbackQuery = update.getCallbackQuery();
    return Mono.fromSupplier(callbackQuery::getData)
        .flatMap(parser::parse)
        .flatMap(buttonCallback -> buttonChooser(buttonCallback, callbackQuery));
  }

  protected Mono<BotApiMethod> handleHelp(Update update) {

    return Mono.just(new SendMessage()
        .setChatId(update.getMessage().getChatId())
        .setText("Available commands - /start"));
  }

  protected Mono<BotApiMethod> handleStart(Update update) {
    var telegramUser = update.getMessage().getFrom();
    var chatId = update.getMessage().getChatId();

    return userService.findByTelegramId(String.valueOf(telegramUser.getId()))
        .flatMap(currentUser -> userService
            .updateTelegramChatId(currentUser.getId(), chatId))
        .switchIfEmpty(userService.save(telegramUserMapper.map(telegramUser, chatId)))
        .map(currentUser -> new SendMessage()
            .setChatId(update.getMessage().getChatId())
            .setText("Hello, " + currentUser.getName())
            .setReplyMarkup(initialButtons.createInitialButtons()));
  }

  protected Mono<BotApiMethod> handleOtherCommand(Update update) {
    return generalContextCommandsHandler.processCommand(update)
        .map(commands -> {
          StringBuilder stringBuilder = new StringBuilder("Available actions:\n");
          commands.forEach(command -> stringBuilder.append(command + "\n"));
          return new SendMessage()
              .setChatId(update.getMessage().getChatId())
              .setText(stringBuilder.toString());
        });
  }

  private Mono<? extends BotApiMethod> buttonChooser(ButtonCallback buttonCallback,
      CallbackQuery callbackQuery) {
    switch (buttonCallback.getCommand()) {
      case "occur_button":
        return updateFeedback(new Feedback(true), buttonCallback)
            .map(ride -> createAnswerCallback("Your approval has been successfully sent "
                + GREEN_CHECK_MARK_EMOJI, callbackQuery.getId()))
            .switchIfEmpty(
                Mono.fromSupplier(() -> createAnswerCallback("Feedback has been already provided "
                        + ENVELOPE_EMOJI,
                    callbackQuery.getId())));
      case "cancel_button":
        return updateFeedback(new Feedback(false), buttonCallback)
            .map(ride -> createAnswerCallback("Your cancellation has been successfully sent "
                + RED_CROSS_EMOJI, callbackQuery.getId()))
            .switchIfEmpty(
                Mono.fromSupplier(() -> createAnswerCallback("Feedback has been already provided "
                        + ENVELOPE_EMOJI,
                    callbackQuery.getId())));
      default:
        return Mono.just(createAnswerCallback("Unsupported operation", callbackQuery.getId()));

    }
  }

  private AnswerCallbackQuery createAnswerCallback(String text, String callbackQueryId) {
    return new AnswerCallbackQuery()
        .setText(text)
        .setCallbackQueryId(callbackQueryId);
  }

  private Mono<Ride> updateFeedback(Feedback feedback, ButtonCallback buttonCallback) {
    return rideService.findById(buttonCallback.getRideId())
        .zipWith(userService.findById(buttonCallback.getRideUserId()))
        .flatMap(rideAndUser -> {
          Ride ride = rideAndUser.getT1();
          User user = rideAndUser.getT2();

          if (user.getId().equals(ride.getDriver().getId())) {
            return rideService.updateDriverFeedback(ride.getId(), feedback);
          }

          if (user.getId().equals(ride.getPassenger().getId())) {
            return rideService.updatePassengerFeedback(ride.getId(), feedback);
          }

          return Mono.error(new FeedbackUpdateException("Feedback can not be updated"));
        });
  }
}
