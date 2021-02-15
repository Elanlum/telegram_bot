package com.elanlum.ecs.ride.crud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.controller.values.RideCriteria;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.RideStatus;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IntegrationTestsConfig.class)
class RideServiceIntegrationTest {

  @Autowired
  private RideService rideService;
  @Autowired
  private DriverRideRequestService driverRideRequestService;
  @Autowired
  private PassengerRideRequestService passengerRideRequestService;
  @Autowired
  private UserService userService;

  public static Stream<Arguments> createInvalidRide() {
    User driver = new User("driverId", "login1", "Driver", "1", 111L);
    User passenger = new User("passengerId", "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);

    return Stream.of(
        Arguments.of(new Ride("id", driver, passenger, driverRideRequest,
            passengerRideRequest, RideStatus.OPENED, null, null)),
        Arguments.of(new Ride(null, null, passenger, driverRideRequest,
            passengerRideRequest, RideStatus.OPENED, null, null)),
        Arguments.of(new Ride(null, driver, null, driverRideRequest,
            passengerRideRequest, RideStatus.OPENED, null, null)),
        Arguments.of(new Ride(null, driver, passenger, null,
            passengerRideRequest, RideStatus.OPENED, null, null)),
        Arguments.of(new Ride(null, driver, passenger, driverRideRequest,
            null, RideStatus.OPENED, null, null))
    );
  }

  @Test
  @DisplayName("Saving a valid Ride")
  void checkValidRide() {
    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();

    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", savedUserOne.getId(), null,
        null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req",
        savedUserTwo.getId(), null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(null, savedUserOne, savedUserTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    StepVerifier.create(rideService.save(ride))
        .expectNextMatches(ride1 -> {
          assertNotNull(ride1.getId());
          return true;
        })
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @MethodSource("createInvalidRide")
  @DisplayName("Saving invalid Rides")
  void checkInvalidRideEntity(Ride ride) {
    StepVerifier.create(rideService.save(ride))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  @DisplayName("Checking that a Ride is saved")
  void save() {
    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();

    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", savedUserOne.getId(), null,
        null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req",
        savedUserTwo.getId(), null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(null, savedUserOne, savedUserTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    assertNotNull(rideService.save(ride).block());
  }

  @Test
  @DisplayName("Checking that a Ride is found by id")
  void findById() {
    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();

    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", savedUserOne.getId(), null,
        null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req",
        savedUserTwo.getId(), null, null, null, RideRequestStatus.AVAILABLE);
    Ride rideEntity = new Ride(null, savedUserOne, savedUserTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    Ride rideSaved = rideService.save(rideEntity).block();
    assertNotNull(rideSaved);

    Ride fromMono = rideService.findById(rideSaved.getId()).block();
    assertEquals(fromMono.getDriver().getName(), "Boka");
    assertEquals(fromMono.getPassenger().getName(), "Zhoka");
  }

  @Test
  @DisplayName("Trying to save a Ride with invalid Driver")
  void checkInvalidDriverForRide() {
    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User savedUserTwo = userService.save(passenger).block();
    User driver = new User(null, "login1", "Driver", "1", 111L);

    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req",
        savedUserTwo.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(null, driver, savedUserTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED,
        null, null);

    StepVerifier.create(rideService.save(ride))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  @DisplayName("Trying to save a Ride with invalid Passenger")
  void checkInvalidPassengerForRide() {
    User driver = new User(null, "driverLogin", "Boka", null,
        null);
    User savedUserOne = userService.save(driver).block();
    User passenger = new User(null, "login2", "Passenger", "2", 222L);

    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", savedUserOne.getId(), null,
        null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(null, savedUserOne, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    StepVerifier.create(rideService.save(ride))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  @DisplayName("Trying to create a Ride in the past")
  void creatingRideRequestInThePast_returnsError() {
    Position departure = new Position(30.000000f, 60.000000f);
    Position destination = new Position(31.000000f, 61.000000f);

    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();

    Interval invalidInterval = new Interval(LocalDateTime.now().minusMinutes(20),
        LocalDateTime.now());
    DriverRideRequest driverRideRequest = new DriverRideRequest(null, savedUserOne.getId(),
        invalidInterval, departure, destination, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(null,
        savedUserTwo.getId(), invalidInterval, departure, destination,
        RideRequestStatus.AVAILABLE);

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectErrorSatisfies(exception -> {
          assertTrue(exception instanceof ConstraintViolationException);
          assertEquals("Invalid ride request time parameters. ", exception.getMessage());
        }).verify();
    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectErrorSatisfies(exception -> {
          assertTrue(exception instanceof ConstraintViolationException);
          assertEquals("Invalid ride request time parameters. ", exception.getMessage());
        }).verify();
  }

  @Test
  @DisplayName("Trying to create a Ride with start time later than end time")
  void creatingRideRequestWhereStartLaterThanEnd_returnsError() {
    Position departure = new Position(30.000000f, 60.000000f);
    Position destination = new Position(31.000000f, 61.000000f);

    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();

    Interval invalidInterval = new Interval(LocalDateTime.now().plusMinutes(20),
        LocalDateTime.now().plusMinutes(10));
    DriverRideRequest driverRideRequest = new DriverRideRequest(null, savedUserOne.getId(),
        invalidInterval, departure, destination, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(null,
        savedUserTwo.getId(), invalidInterval, departure, destination,
        RideRequestStatus.AVAILABLE);

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectErrorSatisfies(exception -> {
          assertTrue(exception instanceof ConstraintViolationException);
          assertEquals("Invalid ride request time parameters. ", exception.getMessage());
        }).verify();
    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectErrorSatisfies(exception -> {
          assertTrue(exception instanceof ConstraintViolationException);
          assertEquals("Invalid ride request time parameters. ", exception.getMessage());
        }).verify();
  }

  @Test
  @DisplayName("Get all Rides with opened status by User id")
  void getAllOpenedRide() {
    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);
    User anotherDriver = new User(null, "driverLogin2", "NuBoka",
        null, null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();
    User savedUserThree = userService.save(anotherDriver).block();

    DriverRideRequest driverRideRequestFromOne = new DriverRideRequest("1req", savedUserOne.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest = new DriverRideRequest("2req", savedUserTwo.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("3req",
        savedUserTwo.getId(), null, null, null,
        RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequestFromOne = new PassengerRideRequest("4req",
        savedUserOne.getId(), null, null, null,
        RideRequestStatus.AVAILABLE);

    Ride rideAsDriver = new Ride(null, savedUserOne, savedUserTwo, driverRideRequestFromOne,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride rideAsPassenger = new Ride(null, savedUserTwo, savedUserOne, driverRideRequest,
        passengerRideRequestFromOne, RideStatus.OPENED, null, null);
    Ride closedRide = new Ride(null, savedUserOne, savedUserTwo, driverRideRequestFromOne,
        passengerRideRequest, RideStatus.CLOSED, null, null);
    Ride otherRide = new Ride(null, savedUserThree, savedUserTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    final Ride rideSavedDrvr = rideService.save(rideAsDriver).block();
    final Ride rideSavedPass = rideService.save(rideAsPassenger).block();
    final Ride rideSavedClsd = rideService.save(closedRide).block();
    final Ride rideSavedOther = rideService.save(otherRide).block();
    assertNotNull(rideSavedDrvr);
    assertNotNull(rideSavedPass);
    assertNotNull(rideSavedClsd);
    assertNotNull(rideSavedOther);

    Flux<Ride> gotRides = rideService.getRidesForUserByStatus(savedUserOne.getId(),
        new RideCriteria("opened"));

    StepVerifier.create(gotRides)
        .assertNext(ride -> assertEquals("Boka", ride.getDriver().getName()))
        .assertNext(ride -> assertEquals("Boka", ride.getPassenger().getName()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Get all Rides with null status by User id")
  void getAllRideByNullStatus() {
    User passenger = new User(null, "passengerLogin", "Zhoka", null,
        null);
    User driver = new User(null, "driverLogin", "Boka", null,
        null);
    User anotherDriver = new User(null, "driverLogin2", "NuBoka",
        null, null);

    User savedUserOne = userService.save(driver).block();
    User savedUserTwo = userService.save(passenger).block();
    User savedUserThree = userService.save(anotherDriver).block();

    DriverRideRequest driverRideRequestFromOne = new DriverRideRequest("1req", savedUserOne.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest = new DriverRideRequest("2req", savedUserTwo.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("3req",
        savedUserTwo.getId(), null, null, null,
        RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequestFromOne = new PassengerRideRequest("4req",
        savedUserOne.getId(), null, null, null,
        RideRequestStatus.AVAILABLE);

    Ride rideAsDriver = new Ride(null, savedUserOne, savedUserTwo, driverRideRequestFromOne,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride rideAsPassenger = new Ride(null, savedUserTwo, savedUserOne, driverRideRequest,
        passengerRideRequestFromOne, RideStatus.OPENED, null, null);
    Ride closedRide = new Ride(null, savedUserOne, savedUserTwo, driverRideRequestFromOne,
        passengerRideRequest, RideStatus.CLOSED, null, null);
    Ride otherRide = new Ride(null, savedUserThree, savedUserTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    final Ride rideSavedDrvr = rideService.save(rideAsDriver).block();
    final Ride rideSavedPass = rideService.save(rideAsPassenger).block();
    final Ride rideSavedClsd = rideService.save(closedRide).block();
    final Ride rideSavedOther = rideService.save(otherRide).block();
    assertNotNull(rideSavedDrvr);
    assertNotNull(rideSavedPass);
    assertNotNull(rideSavedClsd);
    assertNotNull(rideSavedOther);

    Flux<Ride> gotRides = rideService.getRidesForUserByStatus(savedUserOne.getId(),
        new RideCriteria(null));

    StepVerifier.create(gotRides)
        .expectNextCount(4L)
        .verifyComplete();
  }
}
