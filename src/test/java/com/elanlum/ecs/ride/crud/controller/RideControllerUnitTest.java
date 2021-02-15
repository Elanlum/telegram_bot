package com.elanlum.ecs.ride.crud.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.controller.values.RideCriteria;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.RideStatus;
import com.elanlum.ecs.user.model.User;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@Tag(TestCategory.UNIT)
class RideControllerUnitTest {

  private RideService rideService = mock(RideService.class);
  private RideController controller = new RideController(rideService);

  private WebTestClient webTestClient = WebTestClient.bindToController(controller)
      .configureClient()
      .baseUrl("/users")
      .build();

  @Test
  @DisplayName("Get all Rides by status (e.g. opened) and  by User id")
  void getAllOpenedRides() {
    User userOne = new User("id", "testlogin", "Vasya", "1", 111L);
    User userTwo = new User("id2", "newtestlogin", "Ivan", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("drvReq", userOne.getId(),
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(2)),
        null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("passReq", userTwo.getId(),
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(2)),
        null, null, RideRequestStatus.AVAILABLE);
    Ride rideAsDriver = new Ride("idRide", userOne, userTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride rideAsPassenger = new Ride("idRide", userTwo, userOne, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    when(rideService.getRidesForUserByStatus(userOne.getId(), new RideCriteria("opened")))
        .thenReturn(Flux.just(rideAsDriver, rideAsPassenger));

    webTestClient.get()
        .uri("/id/rides?status=opened")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].driver.id").isEqualTo("id")
        .jsonPath("$[1].passenger.id").isEqualTo("id");
  }

  @Test
  @DisplayName("Get all Rides without status by User id")
  void getAllRides() {
    User userOne = new User("id", "testlogin", "Vasya", "1", 111L);
    User userTwo = new User("id2", "newtestlogin", "Ivan", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("drvReq", userOne.getId(),
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(2)),
        null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("passReq", userTwo.getId(),
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(2)),
        null, null, RideRequestStatus.AVAILABLE);
    Ride rideAsDriver = new Ride("idRide", userOne, userTwo, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);
    Ride rideAsPassenger = new Ride("idRide", userTwo, userOne, driverRideRequest,
        passengerRideRequest, RideStatus.CLOSED, null, null);

    when(rideService.getRidesForUserByStatus(userOne.getId(), new RideCriteria(null)))
        .thenReturn(Flux.just(rideAsDriver, rideAsPassenger));

    webTestClient.get()
        .uri("/id/rides")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].driver.id").isEqualTo("id")
        .jsonPath("$[1].passenger.id").isEqualTo("id");
  }
}
