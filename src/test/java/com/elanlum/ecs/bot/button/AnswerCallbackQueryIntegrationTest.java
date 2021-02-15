package com.elanlum.ecs.bot.button;

import static com.elanlum.ecs.ride.model.values.RideStatus.OPENED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.bot.handler.BotCommandsHandler;
import com.elanlum.ecs.bot.handler.GeneralContextCommandsHandler;
import com.elanlum.ecs.bot.handler.mapper.TelegramUserMapper;
import com.elanlum.ecs.ride.crud.repository.impl.RideRepository;
import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = IntegrationTestsConfig.class)
public class AnswerCallbackQueryIntegrationTest {

  private static final String RED_CROSS_EMOJI = "\u274c";//red cross emoji
  private static final String GREEN_CHECK_MARK_EMOJI = "\u2705";//green check mark emoji
  private static final String ENVELOPE_EMOJI = "\u2709";//envelope emoji

  @Autowired
  BotCommandsHandler handler;
  @Autowired
  RideRepository rideRepository;
  @Autowired
  RideService rideService;
  @Autowired
  UserService userService;
  @Autowired
  TelegramUserMapper telegramUserMapper;
  @Autowired
  GeneralContextCommandsHandler generalContextCommandsHandler;
  @Autowired
  InitialButtons initialButtons;
  @Autowired
  StringParser stringParser;


  /**
   * Test includes following parameters. Clicking occur_button means that user confirms successful
   * ride and receives corresponding message. Cancel_button means cancellation scenario. Any button
   * pressed while feedback for the certain user is already set must provide a warn message.
   *
   * @return list of parameters: feedback value, name of the button and response message
   */
  public static Stream<Arguments> createParameters() {
    return Stream.of(
        Arguments.of(null, "occur_button",
            "Your approval has been successfully sent " + GREEN_CHECK_MARK_EMOJI),
        Arguments.of(null, "cancel_button",
            "Your cancellation has been successfully sent " + RED_CROSS_EMOJI),
        Arguments.of(new Feedback(true), "occur_button",
            "Feedback has been already provided " + ENVELOPE_EMOJI)
    );
  }

  @ParameterizedTest
  @MethodSource("createParameters")
  @DisplayName("Checks clicking feedback buttons with or without provided feedback beforehand")
  void checkFeedbackButtonsBehaviour(Feedback feedback, String buttonPressed,
      String resultMessage) {

    Update update = mock(Update.class);
    CallbackQuery callbackQuery = mock(CallbackQuery.class);
    User driver = new User(null, "login1", "name1", "telegramId1", 1L);
    User passenger = new User(null, "login2", "name2", "telegramId2", 2L);
    User savedDriver = userService.save(driver).block();
    User savedPassenger = userService.save(passenger).block();

    DriverRideRequest driverRideRequest = new DriverRideRequest("id", savedDriver.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("id",
        savedPassenger.getId(), null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(null, driver, passenger, driverRideRequest, passengerRideRequest, OPENED,
        feedback, feedback);
    Mono<Ride> savedRide = rideService.save(ride);
    String savedRideId = savedRide.block().getId();
    ButtonCallback buttonCallback = new ButtonCallback(buttonPressed, savedRideId,
        ride.getDriver().getId());

    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callbackQuery);
    when(callbackQuery.getId()).thenReturn("1");

    when(callbackQuery.getData())
        .thenReturn(
            buttonCallback.getCommand() + ":" + buttonCallback.getRideId() + ":" + buttonCallback
                .getRideUserId());

    StepVerifier.create(handler.handle(update))
        .assertNext(apiMethod -> {
          AnswerCallbackQuery answerCallbackQuery = (AnswerCallbackQuery) apiMethod;
          assertEquals(resultMessage, answerCallbackQuery.getText());
          assertEquals("1", answerCallbackQuery.getCallbackQueryId());
        })
        .expectComplete()
        .verify();
  }
}
