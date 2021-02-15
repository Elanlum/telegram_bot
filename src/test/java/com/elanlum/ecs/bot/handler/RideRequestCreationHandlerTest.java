package com.elanlum.ecs.bot.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.bot.context.ContextFactory;
import com.elanlum.ecs.bot.context.exceptions.UnparsableInputException;
import com.elanlum.ecs.bot.context.exceptions.WrongCommandException;
import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.RideRequestFieldParser;
import com.elanlum.ecs.bot.context.model.UserContext;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.mapper.RideRequestMapper;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RideRequestCreationHandlerTest {

  private static final String DEPARTURE = "59.906842 30.298719";
  private static final String DESTINATION = "59.888854 30.322629";
  private static final String DATE = "02-01-2093";
  private static final String TIME = "4:20";
  private static final String DURATION = "15";
  private static final String INIT_DRIVER_COMMAND = "Create driver request";
  private static final String CANCEL_COMMAND = "/cancel";
  private static final String CREATE_COMMAND = "/create";
  private static final String UNKNOWN_COMMAND = "/feedMe";
  private static final String INVALID_PARAMETERS = "invalid parameters";

  @Mock
  UserService userService;
  @Mock
  DriverRideRequestService driverRideRequestService;
  @Mock
  PassengerRideRequestService passengerRideRequestService;
  @Mock
  RideRequestFieldParser parser;
  @Mock
  RideRequestMapper mapper;
  @InjectMocks
  RideRequestCreationHandler rrCreationHandler;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Update update;
  @Mock
  Location location;

  private LocalDate testDate = LocalDate.of(2093, 1, 2);
  private LocalTime testTime = LocalTime.of(4, 20);
  private Duration testDuration = Duration.ofMinutes(15);
  private Position testDeparture = new Position(59.906842f, 30.298719f);
  private Position testDestination = new Position(59.888854f, 30.322629f);
  private Interval testInterval = new Interval(LocalDateTime.of(testDate, testTime),
      LocalDateTime.of(testDate, testTime)
          .plusMinutes(15));

  private User testUser = new User("5", "xDummyx", "Dummy", "1", null);
  private DriverRideRequest testDriverRideRequest = new DriverRideRequest(null, "5", testInterval,
      testDeparture, testDestination, RideRequestStatus.AVAILABLE);
  private PassengerRideRequest testPassengerRideRequest = new PassengerRideRequest(null, "5",
      testInterval, testDeparture, testDestination, RideRequestStatus.AVAILABLE);
  private ContextFactory factory = new ContextFactory();
  private UserContext testContext;

  @BeforeEach
  void before_each() {
    testContext = factory.createUserContext("1", ContextType.CREATE_DRIVER_REQUEST);
  }

  @Test
  void givenCancelCommand_contextIsCanceled_returnsTrue() {
    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(CANCEL_COMMAND);
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext -> assertTrue(userContext.isCanceled()))
        .verifyComplete();
  }

  @Test
  void whileProcessingParameters_cancelCommand_setsContextCanceled() {
    testContext.setState(1);
    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(CANCEL_COMMAND);
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext -> assertTrue(userContext.isCanceled()))
        .verifyComplete();
  }

  @Test
  void givenCreateCommand_withUnknownRole_throwsException() {
    Map<FieldName, String> fieldsToFill = testContext.getFieldsToFill();
    fieldsToFill.put(FieldName.DEPARTURE_POSITION, DEPARTURE);
    fieldsToFill.put(FieldName.DESTINATION_POSITION, DESTINATION);
    fieldsToFill.put(FieldName.RIDE_DATE, DATE);
    fieldsToFill.put(FieldName.RIDE_TIME, TIME);
    fieldsToFill.put(FieldName.EXPECTATION_PERIOD, DURATION);
    fieldsToFill.put(FieldName.ROLE, "How did i get here?");
    Set<String> commands = testContext.getAvailableCommands();
    commands.add("/create");

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(CREATE_COMMAND);

    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectErrorMatches(
            exception -> {
              assertTrue(exception instanceof UnsupportedOperationException);
              return true;
            })
        .verify();
  }

  @Test
  void givenCreateCommand_driverRideRequest_isCreated() {
    Map<FieldName, String> fieldsToFill = testContext.getFieldsToFill();
    fieldsToFill.put(FieldName.DEPARTURE_POSITION, DEPARTURE);
    fieldsToFill.put(FieldName.DESTINATION_POSITION, DESTINATION);
    fieldsToFill.put(FieldName.RIDE_DATE, DATE);
    fieldsToFill.put(FieldName.RIDE_TIME, TIME);
    fieldsToFill.put(FieldName.EXPECTATION_PERIOD, DURATION);
    Set<String> commands = testContext.getAvailableCommands();
    commands.add("/create");

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(CREATE_COMMAND);
    when(userService.findByTelegramId("1")).thenReturn(Mono.just(testUser));
    when(parser.getParsedDate(DATE)).thenReturn(testDate);
    when(parser.getParsedTime(TIME)).thenReturn(testTime);
    when(parser.getParsedExpectationTime(DURATION))
        .thenReturn(15L);
    doReturn(testDeparture).when(parser).getParsedPosition(DEPARTURE);
    doReturn(testDestination).when(parser).getParsedPosition(DESTINATION);
    doReturn(testDriverRideRequest).when(mapper).mapDriverRideRequest(any());
    doReturn(Mono.just(testDriverRideRequest)).when(driverRideRequestService)
        .save(any(DriverRideRequest.class));

    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext -> assertTrue(userContext.isCompleted()))
        .verifyComplete();
    verify(driverRideRequestService, times(1)).save(any(DriverRideRequest.class));
    verifyNoMoreInteractions(driverRideRequestService);
  }

  @Test
  void givenCreateCommand_passengerRideRequest_isCreated() {
    UserContext testPassengerContext = factory
        .createUserContext("1", ContextType.CREATE_PASSENGER_REQUEST);
    Map<FieldName, String> fieldsToFill = testPassengerContext.getFieldsToFill();
    fieldsToFill.put(FieldName.DEPARTURE_POSITION, DEPARTURE);
    fieldsToFill.put(FieldName.DESTINATION_POSITION, DESTINATION);
    fieldsToFill.put(FieldName.RIDE_DATE, DATE);
    fieldsToFill.put(FieldName.RIDE_TIME, TIME);
    fieldsToFill.put(FieldName.EXPECTATION_PERIOD, DURATION);
    Set<String> commands = testPassengerContext.getAvailableCommands();
    commands.add("/create");

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(CREATE_COMMAND);
    when(userService.findByTelegramId("1")).thenReturn(Mono.just(testUser));
    when(parser.getParsedDate(DATE)).thenReturn(testDate);
    when(parser.getParsedTime(TIME)).thenReturn(testTime);
    when(parser.getParsedExpectationTime(DURATION))
        .thenReturn(15L);
    doReturn(testDeparture).when(parser).getParsedPosition(DEPARTURE);
    doReturn(testDestination).when(parser).getParsedPosition(DESTINATION);
    doReturn(testPassengerRideRequest).when(mapper).mapPassengerRideRequest(any());
    doReturn(Mono.just(testPassengerRideRequest)).when(passengerRideRequestService)
        .save(any(PassengerRideRequest.class));

    StepVerifier.create(rrCreationHandler.handle(testPassengerContext, update))
        .assertNext(userContext -> assertTrue(userContext.isCompleted()))
        .verifyComplete();
    verify(passengerRideRequestService, times(1)).save(any(PassengerRideRequest.class));
    verifyNoMoreInteractions(passengerRideRequestService);
  }

  @Test
  void createCommandIsAdded_afterProcessingLastParameter() {
    Map<FieldName, String> fieldsToFill = testContext.getFieldsToFill();
    fieldsToFill.put(FieldName.DEPARTURE_POSITION, DEPARTURE);
    fieldsToFill.put(FieldName.DESTINATION_POSITION, DESTINATION);
    fieldsToFill.put(FieldName.RIDE_DATE, DATE);
    fieldsToFill.put(FieldName.EXPECTATION_PERIOD, DURATION);
    testContext.setState(4);

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(TIME);
    when(userService.findByTelegramId("1")).thenReturn(Mono.just(testUser));

    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext ->
            assertTrue(userContext.getAvailableCommands().contains("/create")))
        .verifyComplete();
  }

  @Test
  void givenInitCommand_contextIsHandled() {
    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(INIT_DRIVER_COMMAND);

    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectNextMatches(userContext -> {
          Set<String> testSet = userContext.getAvailableCommands();
          assertTrue(testSet.contains(FieldName.DEPARTURE_POSITION.getCommand()));
          assertTrue(testSet.contains(FieldName.DESTINATION_POSITION.getCommand()));
          assertTrue(testSet.contains(FieldName.RIDE_DATE.getCommand()));
          assertTrue(testSet.contains(FieldName.EXPECTATION_PERIOD.getCommand()));
          return true;
        })
        .verifyComplete();
  }

  @Test
  void givenCreateCommand_notAllFieldsAreFilled() {
    Map<FieldName, String> fieldsToFill = testContext.getFieldsToFill();
    fieldsToFill.put(FieldName.DEPARTURE_POSITION, DEPARTURE);
    fieldsToFill.put(FieldName.DESTINATION_POSITION, DESTINATION);
    fieldsToFill.put(FieldName.RIDE_TIME, TIME);
    fieldsToFill.put(FieldName.EXPECTATION_PERIOD, DURATION);

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(CREATE_COMMAND);

    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectErrorMatches(exception -> {
          assertTrue(exception instanceof WrongCommandException);
          assertEquals("Not all the parameters are set",
              exception.getMessage());
          return true;
        }).verify();
  }

  @ParameterizedTest
  @EnumSource(value = FieldName.class, mode = Mode.EXCLUDE, names = {"TELEGRAM_ID",
      "ROLE"})
  void givenValidCommand_handle_setsState(FieldName testFieldName) {
    if (testFieldName.equals(FieldName.RIDE_TIME)) {
      testContext.getFieldsToFill().put(FieldName.RIDE_DATE, DATE);
    }
    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(testFieldName.getCommand());
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext -> assertEquals(testFieldName.getState(), userContext.getState()))
        .verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(value = FieldName.class, mode = Mode.EXCLUDE, names = {"TELEGRAM_ID",
      "ROLE"})
  void givenValidCommand_handle_saysFieldIsFilled(FieldName testFieldName) {
    Map<FieldName, String> fieldsToFill = testContext.getFieldsToFill();
    fieldsToFill.put(FieldName.DEPARTURE_POSITION, DEPARTURE);
    fieldsToFill.put(FieldName.DESTINATION_POSITION, DESTINATION);
    fieldsToFill.put(FieldName.RIDE_DATE, DATE);
    fieldsToFill.put(FieldName.RIDE_TIME, TIME);
    fieldsToFill.put(FieldName.EXPECTATION_PERIOD, DURATION);

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(testFieldName.getCommand());
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectErrorMatches(exception -> {
          assertTrue(exception instanceof WrongCommandException);
          assertEquals("Parameter <" + testFieldName.name() + "> is already set",
              exception.getMessage());
          return true;
        }).verify();
  }

  @Test
  void givenUnknownCommand_handle_throwsException() {
    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(UNKNOWN_COMMAND);
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectErrorMatches(exception -> {
          assertTrue(exception instanceof WrongCommandException);
          assertEquals("Unknown command: " + UNKNOWN_COMMAND, exception.getMessage());
          return true;
        }).verify();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5})
  void givenValidParameters_handle_editsInternalCollection(int state) {
    testContext.setState(state);
    String parameters;
    FieldName fieldName;

    switch (state) {
      case 1:
        parameters = DEPARTURE;
        fieldName = FieldName.DEPARTURE_POSITION;
        doReturn(true).when(parser).validateCoordinate(DEPARTURE);
        break;
      case 2:
        parameters = DESTINATION;
        fieldName = FieldName.DESTINATION_POSITION;
        doReturn(true).when(parser).validateCoordinate(DESTINATION);
        break;
      case 3:
        parameters = DATE;
        fieldName = FieldName.RIDE_DATE;
        doReturn(true).when(parser).validateDate(DATE);
        break;
      case 4:
        parameters = TIME;
        fieldName = FieldName.RIDE_TIME;
        when(parser.validateTime(eq(TIME), anyString())).thenReturn(true);
        break;
      case 5:
        parameters = DURATION;
        fieldName = FieldName.EXPECTATION_PERIOD;
        break;
      default:
        parameters = UNKNOWN_COMMAND;
        fieldName = null;
    }

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(parameters);
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext -> {
          if (state == 3 || state == 4) {
            assertEquals(5, userContext.getAvailableCommands().size());
          } else {
            assertEquals(4, userContext.getAvailableCommands().size());
          }
          assertThat(userContext.getAvailableCommands(), not(hasItem(fieldName.getCommand())));
          assertTrue(userContext.getFieldsToFill().containsKey(fieldName));
          assertEquals(0, userContext.getState());
        })
        .verifyComplete();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5})
  void givenInvalidParameters_handle_throwsException(int state) {
    testContext.setState(state);
    testContext.getFieldsToFill().put(FieldName.RIDE_DATE, INVALID_PARAMETERS);
    String parameters = INVALID_PARAMETERS;

    doThrow(new UnparsableInputException("Incorrect input")).when(parser)
        .validateCoordinate(INVALID_PARAMETERS);
    doThrow(new UnparsableInputException("Incorrect input")).when(parser)
        .validateDate(INVALID_PARAMETERS);
    doThrow(new UnparsableInputException("Incorrect input")).when(parser)
        .validateTime(INVALID_PARAMETERS, INVALID_PARAMETERS);

    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(parameters);
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectErrorMatches(exception -> {
          assertTrue(exception instanceof UnparsableInputException);
          return true;
        }).verify();
  }

  @Test
  void givenTelegramMap_handle_processesCoordinates() {
    testContext.setState(1);

    when(update.getMessage().getLocation()).thenReturn(location);
    when(update.getMessage().getText()).thenReturn(null);
    when(location.getLatitude()).thenReturn(59.906842f);
    when(location.getLongitude()).thenReturn(30.298719f);

    doReturn(true).when(parser)
        .validateCoordinate(Float.toString(59.906842f) + " " + Float.toString(30.298719f));
    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .assertNext(userContext -> {
          assertEquals(4, userContext.getAvailableCommands().size());
          assertThat(userContext.getAvailableCommands(), not(hasItem("/setDeparture")));
          assertTrue(
              userContext.getFieldsToFill().containsKey(FieldName.DEPARTURE_POSITION));
          assertEquals(0, userContext.getState());
        })
        .verifyComplete();
  }

  @Test
  void tryingToSetTimeBeforeDate_throwsException() {
    when(update.getMessage().getLocation()).thenReturn(null);
    when(update.getMessage().getText()).thenReturn(FieldName.RIDE_TIME.getCommand());

    StepVerifier.create(rrCreationHandler.handle(testContext, update))
        .expectErrorMatches(throwable -> {
          assertTrue(throwable instanceof WrongCommandException);
          assertEquals("You can't set ride time before date", throwable.getMessage());
          return true;
        }).verify();
  }
}
