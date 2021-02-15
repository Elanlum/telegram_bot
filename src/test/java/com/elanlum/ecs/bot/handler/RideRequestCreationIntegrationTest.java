package com.elanlum.ecs.bot.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.bot.context.exceptions.UnparsableInputException;
import com.elanlum.ecs.bot.context.exceptions.WrongCommandException;
import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.UserContext;
import com.elanlum.ecs.bot.handler.repository.KeyValueStorageRepo;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.matcher.RideRequestStatusUpdater;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = {IntegrationTestsConfig.class})
public class RideRequestCreationIntegrationTest {

  private static final String INIT_DRIVER_COMMAND = ContextType.CREATE_DRIVER_REQUEST
      .getCommandName();
  private static final String INIT_PASSENGER_COMMAND =
      ContextType.CREATE_PASSENGER_REQUEST.getCommandName();
  private static final String DEPARTURE_COMMAND = FieldName.DEPARTURE_POSITION
      .getCommand();
  private static final String DESTINATION_COMMAND = FieldName.DESTINATION_POSITION
      .getCommand();
  private static final String DATE_COMMAND = FieldName.RIDE_DATE.getCommand();
  private static final String TIME_COMMAND = FieldName.RIDE_TIME.getCommand();
  private static final String DURATION_COMMAND = FieldName.EXPECTATION_PERIOD
      .getCommand();
  private static final String CANCEL_COMMAND = "/cancel";
  private static final String CREATE_COMMAND = "/create";
  private static final String DEPARTURE_PARAMS = "59.833562 30.347907";
  private static final String DESTINATION_PARAMS = "59.888854 30.322629";
  private static final String DATE_PARAMS = "02-01-2093";
  private static final String PAST_DATE_PARAMS = "23-02-2012";
  private static final String CURRENT_DATE_PARAMS = LocalDate.now()
      .format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
  private static final String TIME_PARAMS = "4:20";
  private static final String PAST_TIME_PARAMS = LocalTime.now().minusMinutes(5).format(
      DateTimeFormatter.ofPattern("HH:mm"));
  private static final String DURATION_PARAMS = "15";
  private static final String INVALID_COMMAND = "balderdash";

  @Autowired
  private UserService userService;
  @Autowired
  private DriverRideRequestService driverRideRequestService;
  @Autowired
  private PassengerRideRequestService passengerRideRequestService;
  @Autowired
  private RideRequestStatusUpdater rideRequestStatusUpdater;
  @Autowired
  private KeyValueStorageRepo kvStorage;
  @Autowired
  private GeneralContextCommandsHandler generalHandler;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Update telegramUpdate;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Update mapTelegramUpdate;
  @Mock
  Location location;

  User user = new User(null, "xDummyx", "Dummy", "1", null);

  @Test
  @DisplayName("Testing full driver ride request creation cycle through telegram")
  void fullDriverRideRequestCreationCycle() {
    final User savedUser = userService.save(user).block();
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);

    /*
    After we created UserContext, telegramUpdate.getMessage().getText()
    is invoked twice per iteration
     */
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(DEPARTURE_COMMAND).thenReturn(DEPARTURE_COMMAND)
        .thenReturn(DEPARTURE_PARAMS).thenReturn(DEPARTURE_PARAMS)
        .thenReturn(DESTINATION_COMMAND).thenReturn(DESTINATION_COMMAND)
        .thenReturn(DESTINATION_PARAMS).thenReturn(DESTINATION_PARAMS)
        .thenReturn(DATE_COMMAND).thenReturn(DATE_COMMAND)
        .thenReturn(DATE_PARAMS).thenReturn(DATE_PARAMS)
        .thenReturn(TIME_COMMAND).thenReturn(TIME_COMMAND)
        .thenReturn(TIME_PARAMS).thenReturn(TIME_PARAMS)
        .thenReturn(DURATION_COMMAND).thenReturn(DURATION_COMMAND)
        .thenReturn(DURATION_PARAMS).thenReturn(DURATION_PARAMS)
        .thenReturn(CREATE_COMMAND).thenReturn(CREATE_COMMAND);
    for (int i = 0; i < 6; i++) {
      assertNotNull(generalHandler.processCommand(telegramUpdate).block());
    }
    StepVerifier.create(kvStorage.findByKey("1", UserContext.class))
        .expectNextMatches(userContext -> {
          assertEquals(3, userContext.getState());
          assertEquals(4, userContext.getFieldsToFill().size());
          return true;
        }).verifyComplete();

    for (int i = 0; i < 5; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }

    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .assertNext(commands -> assertTrue(commands.contains("Ride request creation is finished")))
        .verifyComplete();

    StepVerifier.create(kvStorage.findByKey("1", UserContext.class))
        .verifyComplete();

    StepVerifier.create(driverRideRequestService.findByUserId(user.getId()))
        .expectNextMatches(driverRideRequest -> {
          assertNotNull(driverRideRequest);
          assertEquals(savedUser.getId(), driverRideRequest.getUserId());
          return true;
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Testing full driver ride request creation cycle through telegram")
  void fullPassengerRideRequestCreationCycle_locationIsPassedUsingMap() {
    final User savedUser = userService.save(user).block();
    when(location.getLatitude()).thenReturn(59.906842f).thenReturn(59.888854f);
    when(location.getLongitude()).thenReturn(30.298719f).thenReturn(30.322629f);
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);
    when(mapTelegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(mapTelegramUpdate.getMessage().getLocation()).thenReturn(location);

    /*
    After we created UserContext, telegramUpdate.getMessage().getText()
    is invoked twice per iteration
     */
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_PASSENGER_COMMAND).thenReturn(INIT_PASSENGER_COMMAND)
        .thenReturn(DEPARTURE_COMMAND).thenReturn(DEPARTURE_COMMAND)
        .thenReturn(DESTINATION_COMMAND).thenReturn(DESTINATION_COMMAND)
        .thenReturn(DATE_COMMAND).thenReturn(DATE_COMMAND)
        .thenReturn(DATE_PARAMS).thenReturn(DATE_PARAMS)
        .thenReturn(TIME_COMMAND).thenReturn(TIME_COMMAND)
        .thenReturn(TIME_PARAMS).thenReturn(TIME_PARAMS)
        .thenReturn(DURATION_COMMAND).thenReturn(DURATION_COMMAND)
        .thenReturn(DURATION_PARAMS).thenReturn(DURATION_PARAMS)
        .thenReturn(CREATE_COMMAND).thenReturn(CREATE_COMMAND);

    generalHandler.processCommand(telegramUpdate).block();
    for (int i = 0; i < 2; i++) {
      generalHandler.processCommand(telegramUpdate).block();
      generalHandler.processCommand(mapTelegramUpdate).block();
    }
    StepVerifier.create(kvStorage.findByKey("1", UserContext.class))
        .expectNextMatches(userContext -> {
          assertEquals(0, userContext.getState());
          assertEquals(4, userContext.getFieldsToFill().size());
          return true;
        }).verifyComplete();

    for (int i = 0; i < 6; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }

    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .assertNext(commands -> assertTrue(commands.contains("Ride request creation is finished")))
        .verifyComplete();

    StepVerifier.create(kvStorage.findByKey("1", UserContext.class))
        .verifyComplete();

    StepVerifier.create(passengerRideRequestService.findByUserId(user.getId()))
        .expectNextMatches(passengerRideRequest -> {
          assertNotNull(passengerRideRequest);
          assertEquals(savedUser.getId(), passengerRideRequest.getUserId());
          assertEquals(59.906842f,
              passengerRideRequest.getDeparturePoint().getLatitude(), 0.000005);
          assertEquals(30.298719f,
              passengerRideRequest.getDeparturePoint().getLongitude(), 0.000005);
          assertEquals(59.888854f,
              passengerRideRequest.getDestinationPoint().getLatitude(), 0.000005);
          assertEquals(30.322629f,
              passengerRideRequest.getDestinationPoint().getLongitude(), 0.000005);
          return true;
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Testing that ride request creation can be canceled at any point of process")
  void rideRequestCreationWasCanceledHalfwayThrough() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);

    /*
    After we created UserContext, telegramUpdate.getMessage().getText()
    is invoked twice per iteration
     */
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(DEPARTURE_COMMAND).thenReturn(DEPARTURE_COMMAND)
        .thenReturn(DEPARTURE_PARAMS).thenReturn(DEPARTURE_PARAMS)
        .thenReturn(DESTINATION_COMMAND).thenReturn(DESTINATION_COMMAND)
        .thenReturn(DESTINATION_PARAMS).thenReturn(DESTINATION_PARAMS)
        .thenReturn(DATE_COMMAND).thenReturn(DATE_COMMAND)
        .thenReturn(CANCEL_COMMAND).thenReturn(CANCEL_COMMAND);

    for (int i = 0; i < 3; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }
    StepVerifier.create(kvStorage.findByKey("1", UserContext.class))
        .expectNextMatches(userContext -> {
          assertEquals(0, userContext.getState());
          assertEquals(3, userContext.getFieldsToFill().size());
          return true;
        }).verifyComplete();
    for (int i = 0; i < 3; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }
    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .assertNext(commands -> assertTrue(commands.contains("Ride request creation is finished")))
        .verifyComplete();
    StepVerifier.create(kvStorage.findByKey("1", UserContext.class))
        .verifyComplete();
  }

  @Test
  @DisplayName("Exception is thrown when attempting to send \"/create\" command "
      + "when some parameters of the ride request are not set")
  void createWhenNotAllTheParametersAreSet() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(CREATE_COMMAND).thenReturn(CREATE_COMMAND);
    generalHandler.processCommand(telegramUpdate).block();
    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(exception -> {
          assertTrue(exception instanceof WrongCommandException);
          assertEquals("Not all the parameters are set",
              exception.getMessage());
          return true;
        }).verify();
  }

  @Test
  @DisplayName("Exception is thrown when trying to set parameter that is already set")
  void tryingToFillFilledField() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(DEPARTURE_COMMAND).thenReturn(DEPARTURE_COMMAND)
        .thenReturn(DEPARTURE_PARAMS).thenReturn(DEPARTURE_PARAMS)
        .thenReturn(DEPARTURE_COMMAND).thenReturn(DEPARTURE_COMMAND);
    for (int i = 0; i < 3; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }
    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(exception -> {
          assertTrue(exception instanceof WrongCommandException);
          assertEquals("Parameter <" + FieldName.DEPARTURE_POSITION.name() + "> is already set",
              exception.getMessage());
          return true;
        }).verify();
  }

  @Test
  @DisplayName("Exception is thrown when trying to pass invalid parameters")
  void passingInvalidParameters() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(DEPARTURE_COMMAND).thenReturn(DEPARTURE_COMMAND)
        .thenReturn(INVALID_COMMAND).thenReturn(INVALID_COMMAND);
    for (int i = 0; i < 2; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }
    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(exception -> {
          System.out.println(exception);
          assertTrue(exception instanceof UnparsableInputException);
          assertTrue(exception.getMessage().contains("Incorrect"));
          return true;
        }).verify();
  }

  @Test
  @DisplayName("Exception is thrown when trying to pass invalid command")
  void passingInvalidCommand() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(INVALID_COMMAND).thenReturn(INVALID_COMMAND);
    generalHandler.processCommand(telegramUpdate).block();
    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(exception -> {
          System.out.println(exception);
          assertTrue(exception instanceof WrongCommandException);
          assertTrue(exception.getMessage().contains("Unknown command"));
          return true;
        }).verify();
  }

  @Test
  @DisplayName("Exception is thrown when trying to send the date that is in the past")
  void givenRideDateInThePast_exceptionIsThrown() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);

    /*
    After we created UserContext, telegramUpdate.getMessage().getText()
    is invoked twice per iteration
     */
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(DATE_COMMAND).thenReturn(DATE_COMMAND)
        .thenReturn(PAST_DATE_PARAMS).thenReturn(PAST_DATE_PARAMS);

    for (int i = 0; i < 2; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }

    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(throwable -> {
          assertTrue(throwable instanceof WrongCommandException);
          assertTrue(throwable.getMessage().contains("You can't create rides in the past"));
          return true;
        }).verify();
  }

  @Test
  @DisplayName("Exception is thrown when trying to pass ride time in the past when date is set")
  void givenCurrentDate_whenSendingRideTimeFromThePast_exceptionIsThrown() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);

    /*
    After we created UserContext, telegramUpdate.getMessage().getText()
    is invoked twice per iteration
     */
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(DATE_COMMAND).thenReturn(DATE_COMMAND)
        .thenReturn(CURRENT_DATE_PARAMS).thenReturn(CURRENT_DATE_PARAMS)
        .thenReturn(TIME_COMMAND).thenReturn(TIME_COMMAND)
        .thenReturn(PAST_TIME_PARAMS).thenReturn(PAST_TIME_PARAMS);

    for (int i = 0; i < 4; i++) {
      generalHandler.processCommand(telegramUpdate).block();
    }

    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(throwable -> {
          assertTrue(throwable instanceof WrongCommandException);
          assertTrue(throwable.getMessage().contains("You can't create rides in the past"));
          return true;
        }).verify();
  }

  @Test
  @DisplayName("Exception is thrown when trying to set ride time before date")
  void whenAttemptingToSetTimeBeforeDate_exceptionIsThrown() {
    when(telegramUpdate.getMessage().getFrom().getId()).thenReturn(1);
    when(telegramUpdate.getMessage().getLocation()).thenReturn(null);

    /*
    After we created UserContext, telegramUpdate.getMessage().getText()
    is invoked twice per iteration
     */
    when(telegramUpdate.getMessage().getText())
        .thenReturn(INIT_DRIVER_COMMAND).thenReturn(INIT_DRIVER_COMMAND)
        .thenReturn(TIME_COMMAND).thenReturn(TIME_COMMAND);

    generalHandler.processCommand(telegramUpdate).block();

    StepVerifier.create(generalHandler.processCommand(telegramUpdate))
        .expectErrorMatches(throwable -> {
          assertTrue(throwable instanceof WrongCommandException);
          assertTrue(throwable.getMessage().contains("You can't set ride time before date"));
          return true;
        }).verify();
  }
}

