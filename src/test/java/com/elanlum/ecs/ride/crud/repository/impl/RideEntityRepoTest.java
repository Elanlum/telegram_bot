package com.elanlum.ecs.ride.crud.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.RideStatus;
import com.elanlum.ecs.user.model.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideEntityRepoTest {

  @Mock
  private ReactiveMongoTemplate template;
  @InjectMocks
  private RideRepository entityRepo;


  @Test
  void whenSaveThenReturnMonoRideEntity() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride rideEntity = new Ride(driver, passenger, driverRideRequest,
        passengerRideRequest);

    doReturn(Mono.just(rideEntity)).when(template).save(rideEntity);
    Mono<Ride> monoEntity = entityRepo.save(rideEntity);
    assertEquals(monoEntity.block().getDriver().getName(), "Driver");
    assertEquals(monoEntity.block().getPassenger().getName(), "Passenger");
    verify(template, times(1)).save(rideEntity);
  }

  @Test
  void whenFindByIdThenReturnMonoRideEntity() {
    User driver = new User(null, "login1", "Driver", "1", 111L);
    User passenger = new User(null, "login2", "Passenger", "2", 222L);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1req", driver.getId(), null, null,
        null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("2req", passenger.getId(),
        null, null, null, RideRequestStatus.AVAILABLE);
    Ride rideEntity = new Ride("id", driver, passenger, driverRideRequest,
        passengerRideRequest, RideStatus.OPENED, null, null);

    doReturn(Mono.just(rideEntity)).when(template).findById("id", Ride.class);
    Mono<Ride> monoEntity = entityRepo.findById("id");
    assertEquals(monoEntity.block().getDriver().getName(), "Driver");
    assertEquals(monoEntity.block().getPassenger().getName(), "Passenger");
    verify(template, times(1)).findById("id", Ride.class);

  }
}