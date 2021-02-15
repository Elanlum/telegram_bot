package com.elanlum.ecs.ride.crud.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.RideStatus;
import com.elanlum.ecs.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideRepositoryTest {

  @Mock
  private ReactiveMongoTemplate template;
  @InjectMocks
  private RideRepository rideRepository;


  @Test
  void whenSaveThenReturnMonoRide() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(driver, passenger, driverRideRequest,
        passengerRideRequest);

    doReturn(Mono.just(ride)).when(template).save(ride);
    Mono<Ride> monoEntity = rideRepository.save(ride);
    assertEquals(monoEntity.block().getDriver().getName(), "Driver");
    assertEquals(monoEntity.block().getPassenger().getName(), "Passenger");
    verify(template, times(1)).save(ride);
  }

  @Test
  void whenFindByIdThenReturnMonoRide() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    doReturn(Mono.just(ride)).when(template).findById("id", Ride.class);
    Mono<Ride> monoEntity = rideRepository.findById("id");
    assertEquals(monoEntity.block().getDriver().getName(), "Driver");
    assertEquals(monoEntity.block().getPassenger().getName(), "Passenger");
    verify(template, times(1)).findById("id", Ride.class);
  }

  @Test
  @SuppressWarnings("Duplicates")
  void whenUpdateDriverFeedbackReturnMonoRide() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);

    Feedback feedback = new Feedback(true);
    Ride ride = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, feedback, null);

    doReturn(Mono.just(ride)).when(template).save(ride);

    Ride savedRide = template.save(ride).block();

    doReturn(Mono.just(savedRide)).when(template)
        .findAndModify(eq(Query.query(Criteria.where("_id").is(savedRide.getId())
                .and("driverFeedback").exists(false))),
            eq(Update.update("driverFeedback", feedback)),
            any(FindAndModifyOptions.class), eq(Ride.class));

    StepVerifier.create(rideRepository.updateDriverFeedback("id", feedback))
        .assertNext(ride1 -> assertEquals(true, ride1.getDriverFeedback().isRideHappened()))
        .verifyComplete();
    verify(template, times(1))
        .findAndModify(any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Ride.class));
    verifyNoMoreInteractions(template);
  }

  @Test
  @SuppressWarnings("Duplicates")
  void whenUpdatePassengerFeedbackReturnMonoRide() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Feedback feedback = new Feedback(true);
    Ride ride = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, feedback);

    doReturn(Mono.just(ride)).when(template).save(ride);

    Ride savedRide = template.save(ride).block();

    doReturn(Mono.just(ride)).when(template)
        .findAndModify(eq(Query.query(
            Criteria.where("_id").is(savedRide.getId()).and("passengerFeedback").exists(false))),
            eq(Update.update("passengerFeedback", feedback)),
            any(FindAndModifyOptions.class), eq(Ride.class));

    StepVerifier.create(rideRepository.updatePassengerFeedback(savedRide.getId(), feedback))
        .assertNext(ride1 -> assertEquals(true, ride1.getPassengerFeedback().isRideHappened()))
        .verifyComplete();
    verify(template, times(1))
        .findAndModify(any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Ride.class));
    verifyNoMoreInteractions(template);
  }

  @Test
  @DisplayName("Get all Rides by RideStatus (e.g. opened) and User id")
  void getRidesByStatus() {
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

    Criteria fieldsCriteria = new Criteria()
        .orOperator(Criteria.where("driver.id").is(userOne.getId()),
            Criteria.where("passenger.id").is(userOne.getId()));

    doReturn(Flux.just(rideAsDriver, rideAsPassenger)).when(template).find(Query
        .query(Criteria.where("status").is(RideStatus.OPENED)
            .andOperator(fieldsCriteria)), Ride.class);

    Flux<Ride> gotRides = rideRepository
        .getRidesForUserByStatus(userOne.getId(), RideStatus.OPENED);

    StepVerifier.create(gotRides)
        .expectNext(rideAsDriver)
        .expectNext(rideAsPassenger)
        .verifyComplete();

    verify(template, times(1)).find(any(Query.class), eq(Ride.class));
    verify(template, times(0)).findAll(eq(Ride.class));
    verifyNoMoreInteractions(template);
  }

  @Test
  @DisplayName("Get all Rides by null RideStatus and given User id")
  void getAllRidesByNullStatus() {
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

    Criteria fieldsCriteria = new Criteria()
        .orOperator(Criteria.where("driver.id").is(userOne.getId()),
            Criteria.where("passenger.id").is(userOne.getId()));

    doReturn(Flux.just(rideAsDriver, rideAsPassenger)).when(template).findAll(Ride.class);

    Flux<Ride> gotRides = rideRepository
        .getRidesForUserByStatus(userOne.getId(), null);

    StepVerifier.create(gotRides)
        .expectNext(rideAsDriver)
        .expectNext(rideAsPassenger)
        .verifyComplete();

    verify(template, times(0)).find(any(Query.class), eq(Ride.class));
    verify(template, times(1)).findAll(eq(Ride.class));
    verifyNoMoreInteractions(template);
  }
}
