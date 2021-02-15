package com.elanlum.ecs.bot.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.bot.button.ButtonCallback;
import com.elanlum.ecs.bot.button.InitialButtons;
import com.elanlum.ecs.bot.button.StringParser;
import com.elanlum.ecs.bot.context.exceptions.UnparsableInputException;
import com.elanlum.ecs.bot.handler.mapper.TelegramUserMapper;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.RideStatus;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.PublisherProbe;

@Tag(TestCategory.UNIT)
class BotCommandsHandlerTest {

  private static final String RED_CROSS_EMOJI = "\u274c";//red cross emoji
  private static final String GREEN_CHECK_MARK_EMOJI = "\u2705";//green check mark emoji

  private TelegramUserMapper telegramUserMapper = mock(TelegramUserMapper.class);
  private GeneralContextCommandsHandler generalContextCommandsHandler = mock(
      GeneralContextCommandsHandler.class);
  private Update update = mock(Update.class);
  private Message mockMessage = mock(Message.class);
  private UserService userService = mock(UserService.class);
  private InitialButtons initialButtons = mock(InitialButtons.class);
  private RideService rideService = mock(RideService.class);
  private CallbackQuery callbackQuery = mock(CallbackQuery.class);
  private StringParser parser = mock(StringParser.class);
  private BotCommandsHandler handler = new BotCommandsHandler(userService, telegramUserMapper,
      generalContextCommandsHandler, initialButtons, rideService, parser);

  @Test
  @DisplayName("Check message from update")
  void getSendMessageTest() {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("aaaaa");
    when(mockMessage.hasText()).thenReturn(true);
    when(generalContextCommandsHandler.processCommand(update))
        .thenReturn(Mono.just(new HashSet<>()));
    Flux<? extends BotApiMethod> messages = handler.handle(update);
    BotApiMethod botApiMethod = messages.blockFirst();
    SendMessage sendMessage = (SendMessage) botApiMethod;
    assertThat(messages.count().block(), is(1L));
    assertThat(sendMessage.getText(), is(notNullValue()));
  }

  @Test
  @DisplayName("Handle start command with valid User")
  void handleStartTestWithExistingTelegramUser() {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.hasText()).thenReturn(true);
    when(mockMessage.getText()).thenReturn("/start");

    var telegramUser = mock(
        org.telegram.telegrambots.meta.api.objects.User.class);
    when(mockMessage.getFrom()).thenReturn(telegramUser);
    when(mockMessage.getChatId()).thenReturn(2L);
    when(telegramUser.getId()).thenReturn(1);

    User user = new User(
        null, "vasya", "Vasiliy", "1", 1L);
    Mono<User> userMono = Mono.just(new User(
        "1",
        user.getLogin(),
        user.getName(),
        user.getTelegramId(),
        user.getTelegramChatId()));
    doReturn(userMono).when(userService).findByTelegramId(anyString());

    Mono<User> updatedUserMono = Mono.just(new User(
        "1",
        user.getLogin(),
        user.getName(),
        user.getTelegramId(),
        2L));
    doReturn(updatedUserMono).when(userService).updateTelegramChatId(anyString(), anyLong());
    doReturn(user).when(telegramUserMapper).map(eq(telegramUser), anyLong());

    PublisherProbe<User> probe = PublisherProbe.of(userMono);
    doReturn(probe.mono()).when(userService).save(eq(user));
    probe.assertWasNotSubscribed();
    probe.assertWasNotRequested();

    var messages = handler.handle(update);
    SendMessage result = (SendMessage) messages.blockFirst();
    assertThat(result.getChatId(), is("2"));
    assertThat(result.getText(), is("Hello, " + user.getName()));
  }

  @Test
  @DisplayName("Handle start command with invalid User")
  void handleStartTestWithNonExistingTelegramUser() {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.hasText()).thenReturn(true);
    when(mockMessage.getText()).thenReturn("/start");

    var telegramUser = mock(
        org.telegram.telegrambots.meta.api.objects.User.class);
    when(mockMessage.getFrom()).thenReturn(telegramUser);
    when(mockMessage.getChatId()).thenReturn(1L);
    when(telegramUser.getId()).thenReturn(1);

    User user = new User(
        null, "vasya", "Vasiliy", "1", 1L);
    Mono<User> userMono = Mono.just(new User(
        "1",
        user.getLogin(),
        user.getName(),
        user.getTelegramId(),
        user.getTelegramChatId()));

    when(userService.findByTelegramId(anyString())).thenReturn(Mono.empty());

    doReturn(user).when(telegramUserMapper).map(eq(telegramUser), anyLong());
    doReturn(userMono).when(userService).save(eq(user));

    var messages = handler.handle(update);
    SendMessage result = (SendMessage) messages.blockFirst();

    assertThat(result.getText(), is("Hello, " + user.getName()));
    assertThat(result.getChatId(), is("1"));
    assertThat(result.getText(), is("Hello, " + user.getName()));
    verify(userService, times(1)).save(user);
  }

  @Test
  @DisplayName("Handle message with text")
  void handleTest() {
    Set<String> commands = new HashSet<>();
    commands.add("com1");
    commands.add("com2");
    commands.add("com3");

    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("aaaaa");
    when(mockMessage.hasText()).thenReturn(true);
    when(generalContextCommandsHandler.processCommand(update))
        .thenReturn(Mono.just(commands));
    var messages = handler.handle(update);
    SendMessage result = (SendMessage) messages.blockFirst();

    assertTrue(result.getText().contains("com1"));
    assertTrue(result.getText().contains("com2"));
    assertTrue(result.getText().contains("com3"));
  }

  @Test
  @DisplayName("Handle message with Location")
  void handleLocation() {
    Set<String> commands = new HashSet<>();
    commands.add("com1");
    commands.add("com2");
    commands.add("com3");

    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.hasText()).thenReturn(false);
    when(mockMessage.hasLocation()).thenReturn(true);
    when(generalContextCommandsHandler.processCommand(update))
        .thenReturn(Mono.just(commands));
    var messages = handler.handle(update);
    SendMessage result = (SendMessage) messages.blockFirst();

    assertTrue(result.getText().contains("com1"));
    assertTrue(result.getText().contains("com2"));
    assertTrue(result.getText().contains("com3"));
  }

  @Test
  @DisplayName("Handle help command")
  void handleHelpTest() {
    Update update = mock(Update.class);
    Message mockMessage = mock(Message.class);
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("/help");
    when(mockMessage.hasText()).thenReturn(true);

    var messages = handler.handle(update);
    SendMessage result = (SendMessage) messages.blockFirst();

    assertThat(result.getText(), is(notNullValue()));
  }

  @Test
  @DisplayName("Returned Message contains no text")
  void handleFailTest() {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.hasText()).thenReturn(false);
    var messages = handler.handle(update);
    assertThat(messages.count().block(), is(0L));
  }

  @Test
  @DisplayName("Returned Update contains null message")
  void handleFailMesTest() {
    when(update.getMessage()).thenReturn(null);

    var messages = handler.handle(update);
    assertThat(messages.count().block(), is(0L));
  }

  @Test
  @DisplayName("Message is unparsable")
  void handleErrorsTest() {
    when(update.getMessage()).thenReturn(mockMessage);
    when(mockMessage.getText()).thenReturn("aaaaa");
    when(mockMessage.hasText()).thenReturn(true);
    when(generalContextCommandsHandler.processCommand(update))
        .thenReturn(Mono.error(new UnparsableInputException("error occurred")));
    var messages = handler.handle(update);
    SendMessage result = (SendMessage) messages.blockFirst();

    assertEquals(result.getText(), "error occurred");
  }

  @Test
  @DisplayName("Handle driver feedback")
  void handleOccurredDriverFeedbackTest() {
    Feedback feedback = new Feedback(true);
    ButtonCallback buttonCallback = new ButtonCallback("occur_button", "rideId", "rideUserId");
    User driver = new User("driverId", "login1", "Driver", "1", 111L);
    User passenger = new User("passengerId", "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride("1", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride updatedDriverRide = new Ride(null, driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, feedback, null);
    Mono<ButtonCallback> buttonCallbackMono = Mono.just(buttonCallback);

    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callbackQuery);
    when(callbackQuery.getData()).thenReturn("call");
    when(callbackQuery.getId()).thenReturn("callbackId");
    when(parser.parse(anyString())).thenReturn(buttonCallbackMono);
    when(rideService.findById(anyString())).thenReturn(Mono.just(ride));
    when(userService.findById(anyString())).thenReturn(Mono.just(driver));
    when(rideService.updateDriverFeedback(anyString(), any(Feedback.class)))
        .thenReturn(Mono.just(updatedDriverRide));

    AnswerCallbackQuery answerCallbackQuery = (AnswerCallbackQuery) handler.handle(update)
        .blockFirst();

    assertEquals("Your approval has been successfully sent " + GREEN_CHECK_MARK_EMOJI,
        answerCallbackQuery.getText());

  }

  @Test
  @DisplayName("Handle passenger feedback")
  void handleOccurredPassengerFeedbackTest() {
    Feedback feedback = new Feedback(true);
    ButtonCallback buttonCallback = new ButtonCallback("cancel_button", "rideId", "rideUserId");
    User driver = new User("driverId", "login1", "Driver", "1", 111L);
    User passenger = new User("passengerId", "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride("1", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride updatedPassengerRide = new Ride("1", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, feedback, feedback);
    Mono<ButtonCallback> buttonCallbackMono = Mono.just(buttonCallback);

    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callbackQuery);
    when(callbackQuery.getData()).thenReturn("call");
    when(callbackQuery.getId()).thenReturn("callbackId");
    when(parser.parse(anyString())).thenReturn(buttonCallbackMono);
    when(rideService.findById(anyString())).thenReturn(Mono.just(ride));
    when(userService.findById(anyString())).thenReturn(Mono.just(passenger));
    when(rideService.updatePassengerFeedback(anyString(), any(Feedback.class)))
        .thenReturn(Mono.just(updatedPassengerRide));

    AnswerCallbackQuery answerCallbackQuery = (AnswerCallbackQuery) handler.handle(update)
        .blockFirst();

    assertEquals("Your cancellation has been successfully sent "
        + RED_CROSS_EMOJI, answerCallbackQuery.getText());
  }

  @Test
  @DisplayName("Command does not match any supported")
  void givenCommandIsUnsupportedTest() {
    Feedback feedback = new Feedback(true);
    ButtonCallback buttonCallback = new ButtonCallback("another_button", "rideId", "rideUserId");
    User driver = new User("driverId", "login1", "Driver", "1", 111L);
    User passenger = new User("passengerId", "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride("1", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride updatedPassengerRide = new Ride("1", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, feedback, feedback);
    Mono<ButtonCallback> buttonCallbackMono = Mono.just(buttonCallback);

    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callbackQuery);
    when(callbackQuery.getData()).thenReturn("call");
    when(callbackQuery.getId()).thenReturn("callbackId");
    when(parser.parse(anyString())).thenReturn(buttonCallbackMono);
    when(rideService.findById(anyString())).thenReturn(Mono.just(ride));
    when(userService.findById(anyString())).thenReturn(Mono.just(passenger));
    when(rideService.updatePassengerFeedback(anyString(), any(Feedback.class)))
        .thenReturn(Mono.just(updatedPassengerRide));

    AnswerCallbackQuery answerCallbackQuery = (AnswerCallbackQuery) handler.handle(update)
        .blockFirst();

    assertEquals("Unsupported operation", answerCallbackQuery.getText());
  }

  @Test
  @DisplayName("UserId does not match passenger or driver Id")
  void givenUserIdIsIncorrectTest() {
    Feedback feedback = new Feedback(true);
    User driver = new User("driverId", "login1", "Driver", "1", 111L);
    User passenger = new User("passengerId", "login2", "Passenger", "2", 222L);
    User anotherPassenger = new User("anotherPassengerId", "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req",
        "anotherPassengerId",
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride("1", driver, passenger, null,
        null, RideStatus.OPENED, null, null);
    Ride updatedPassengerRide = new Ride("1", driver, anotherPassenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, feedback, feedback);

    ButtonCallback buttonCallback = new ButtonCallback("cancel_button", "rideId",
        ride.getPassenger().getId());
    Mono<ButtonCallback> buttonCallbackMono = Mono.just(buttonCallback);

    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callbackQuery);
    when(callbackQuery.getData()).thenReturn("call");
    when(callbackQuery.getId()).thenReturn("callbackId");
    when(parser.parse(anyString())).thenReturn(buttonCallbackMono);
    when(rideService.findById(anyString())).thenReturn(Mono.just(ride));
    when(userService.findById(anyString())).thenReturn(Mono.just(anotherPassenger));
    when(rideService.updatePassengerFeedback(anyString(), any(Feedback.class)))
        .thenReturn(Mono.just(updatedPassengerRide));

    AnswerCallbackQuery answerCallbackQuery = (AnswerCallbackQuery) handler.handle(update)
        .blockFirst();
    assertEquals("Feedback can not be updated", answerCallbackQuery.getText());
  }
}