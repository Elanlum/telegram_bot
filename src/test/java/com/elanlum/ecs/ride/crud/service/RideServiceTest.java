package com.elanlum.ecs.ride.crud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.controller.values.RideCriteria;
import com.elanlum.ecs.ride.crud.repository.impl.RideRepository;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.validation.ValidationService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.RideStatus;

import javax.validation.groups.Default;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideServiceTest {

  @Mock
  private RideRepository rideRepository;
  @Mock
  private ValidationService<Ride> validationService;
  @InjectMocks
  RideService rideService;

  @Test
  @DisplayName("Save method for Ride produces Mono object")
  void whenSaveThenReturnMonoRideEntity() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(driver, passenger, driverRideRequest,
        passengerRideRequest);

    doReturn(Mono.just(ride)).when(rideRepository).save(ride);
    doReturn(Mono.just(ride)).when(validationService).entityValidate(ride,
        Default.class);
    Mono<Ride> rideMono = rideService.save(ride);
    assertEquals(rideMono.block().getDriver().getName(), "Driver");
    assertEquals(rideMono.block().getPassenger().getName(), "Passenger");
  }

  @Test
  @DisplayName("FindById method returns Mono with Ride")
  void whenFindByIdThenReturnMonoRideEntity() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(driver, passenger, driverRideRequest,
        passengerRideRequest);

    doReturn(Mono.just(ride)).when(rideRepository).findById(ride.getId());
    Mono<Ride> rideMono = rideService.findById(ride.getId());
    assertEquals(rideMono.block().getDriver().getName(), "Driver");
    assertEquals(rideMono.block().getPassenger().getName(), "Passenger");
    verify(rideRepository, times(1)).findById(ride.getId());
  }

  @Test
  @DisplayName("Update nonnull driver feedback returns unchanged Mono")
  void updateExistingDriverFeedbackTest() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);

    Feedback feedback = new Feedback(true);
    Feedback updatedFeedback = new Feedback(false);
    Ride ride = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, feedback, null);

    doReturn(Mono.just(ride)).when(rideRepository)
        .updateDriverFeedback(ride.getId(), updatedFeedback);

    StepVerifier.create(rideService.updateDriverFeedback(ride.getId(), updatedFeedback))
        .assertNext(ride1 -> assertTrue(ride1.getDriverFeedback().isRideHappened()))
        .verifyComplete();
    verify(rideRepository, times(1)).updateDriverFeedback(ride.getId(), updatedFeedback);
    verifyNoMoreInteractions(rideRepository);
  }

  @Test
  @DisplayName("Update null feedback returns Mono")
  void updateNonExistingDriverFeedbackTest() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);

    Feedback updatedFeedback = new Feedback(false);
    Ride ride = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride updatedRide = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, new Feedback(false), null);

    when(rideRepository.updateDriverFeedback(ride.getId(), updatedFeedback))
        .thenReturn(Mono.just(updatedRide));

    StepVerifier.create(rideService.updateDriverFeedback(ride.getId(), updatedFeedback))
        .assertNext(ride1 -> assertFalse(ride1.getDriverFeedback().isRideHappened()))
        .verifyComplete();
    verify(rideRepository, times(1)).updateDriverFeedback(ride.getId(), updatedFeedback);
    verifyNoMoreInteractions(rideRepository);
  }

  @Test
  @DisplayName("Update nonnull passenger feedback returns unchanged Mono")
  void updateNonExistingPassengerFeedbackTest() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);

    Feedback updatedFeedback = new Feedback(false);
    Ride ride = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride updatedRide = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    when(rideRepository.updatePassengerFeedback(ride.getId(), updatedFeedback))
        .thenReturn(Mono.just(updatedRide));

    StepVerifier.create(rideService.updatePassengerFeedback(ride.getId(), updatedFeedback))
        .assertNext(ride1 -> assertNull(ride1.getPassengerFeedback()))
        .verifyComplete();
    verify(rideRepository, times(1)).updatePassengerFeedback(ride.getId(), updatedFeedback);
    verifyNoMoreInteractions(rideRepository);
  }

  @Test
  @DisplayName("Update feedback with null rideId")
  void updatePassengerFeedbackWithNullRideIdTest() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);

    Feedback updatedFeedback = new Feedback(true);
    Ride ride = new Ride(null, driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    StepVerifier.create(rideService.updatePassengerFeedback(ride.getId(), updatedFeedback))
        .verifyError();
    verify(rideRepository, times(0)).updatePassengerFeedback(ride.getId(), updatedFeedback);
  }

  @Test
  @DisplayName("Get all Rides by RideStatus (e.g. opened) and User id")
  void getAllOpenedRides() {
    User userOne = new User("id", "testlogin", "Vasya", "1", 111L);
    User userTwo = new User("id2", "newtestlogin", "Ivan", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("drvReq", userOne.getId(), null,
        null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("passReq", userTwo.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride rideAsDriver = new Ride("idRide", userOne, userTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride rideAsPassenger = new Ride("idRide", userTwo, userOne, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    doReturn(Flux.just(rideAsDriver, rideAsPassenger)).when(rideRepository)
        .getRidesForUserByStatus(userOne.getId(), RideStatus.OPENED);

    Flux<Ride> gotRides = rideService
        .getRidesForUserByStatus(userOne.getId(), new RideCriteria("opened"));

    StepVerifier.create(gotRides)
        .expectNext(rideAsDriver)
        .expectNext(rideAsPassenger)
        .verifyComplete();

    verify(rideRepository, times(1)).getRidesForUserByStatus(anyString(), any(RideStatus.class));
    verifyNoMoreInteractions(rideRepository);
  }

  @Test
  @DisplayName("Get all Rides by RideStatus (e.g. closed) and User id")
  void getAllClosedRides() {
    User userOne = new User("id", "testlogin", "Vasya", "1", 111L);
    User userTwo = new User("id2", "newtestlogin", "Ivan", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("drvReq", userOne.getId(), null,
        null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("passReq", userTwo.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride rideAsDriver = new Ride("idRide", userOne, userTwo, driverRideRequest,
        passengerRideRequest, RideStatus.CLOSED, null, null);
    Ride rideAsPassenger = new Ride("idRide", userTwo, userOne, driverRideRequest,
        passengerRideRequest, RideStatus.CLOSED, null, null);

    doReturn(Flux.just(rideAsDriver, rideAsPassenger)).when(rideRepository)
        .getRidesForUserByStatus(userOne.getId(), RideStatus.CLOSED);

    Flux<Ride> gotRides = rideService
        .getRidesForUserByStatus(userOne.getId(), new RideCriteria("closed"));

    StepVerifier.create(gotRides)
        .expectNext(rideAsDriver)
        .expectNext(rideAsPassenger)
        .verifyComplete();

    verify(rideRepository, times(1)).getRidesForUserByStatus(anyString(), any(RideStatus.class));
    verifyNoMoreInteractions(rideRepository);
  }

  @Test
  @DisplayName("Get all Rides by RideStatus - null - and User id")
  void getAllRideByNullStatus() {
    User userOne = new User("id", "testlogin", "Vasya", "1", 111L);
    User userTwo = new User("id2", "newtestlogin", "Ivan", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("drvReq", userOne.getId(), null,
        null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("passReq", userTwo.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride rideAsDriver = new Ride("idRide", userOne, userTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride rideAsPassenger = new Ride("idRide", userTwo, userOne, driverRideRequest,
        passengerRideRequest, RideStatus.CLOSED, null, null);

    doReturn(Flux.just(rideAsDriver, rideAsPassenger)).when(rideRepository)
        .getRidesForUserByStatus(userOne.getId(), null);

    Flux<Ride> gotRides = rideService
        .getRidesForUserByStatus(userOne.getId(), new RideCriteria(null));

    StepVerifier.create(gotRides)
        .expectNext(rideAsDriver)
        .expectNext(rideAsPassenger)
        .verifyComplete();

    verify(rideRepository, times(1)).getRidesForUserByStatus(anyString(),
        eq(null));
    verifyNoMoreInteractions(rideRepository);
  }

  @Test
  @DisplayName("Send null parameters to getRidesForUserByStatus method")
  void getAllRideWithNullId() {
    assertThrows(NullPointerException.class, () -> rideService.getRidesForUserByStatus(null,
        null));
  }

  @Test
  @DisplayName("Send wrong status getRidesForUserByStatus method")
  void sendWrongStatus() {
    assertThrows(IllegalArgumentException.class, () -> rideService.getRidesForUserByStatus("id",
        new RideCriteria("unknown")).blockFirst());
  }
}
