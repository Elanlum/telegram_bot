package com.elanlum.ecs.ride.model.values;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
class RideStatusTest {

  @Test
  @DisplayName("Get opened Ride status by string")
  void getOpenedRide() {
    StepVerifier.create(RideStatus.getRideStatusByString("opened"))
        .assertNext(rideStatus -> assertEquals(RideStatus.OPENED, rideStatus))
        .verifyComplete();

    StepVerifier.create(RideStatus.getRideStatusByString("OPENED"))
        .assertNext(rideStatus -> assertEquals(RideStatus.OPENED, rideStatus))
        .verifyComplete();
  }

  @Test
  @DisplayName("Get closed Ride status by string")
  void getClosedRide() {
    StepVerifier.create(RideStatus.getRideStatusByString("closed"))
        .assertNext(rideStatus -> assertEquals(RideStatus.CLOSED, rideStatus))
        .verifyComplete();

    StepVerifier.create(RideStatus.getRideStatusByString("CLOSED"))
        .assertNext(rideStatus -> assertEquals(RideStatus.CLOSED, rideStatus))
        .verifyComplete();
  }

  @Test
  @DisplayName("Get null in case no match of input string with statuses")
  void getRideStatusFailed() {
    StepVerifier.create(RideStatus.getRideStatusByString("unknown"))
        .expectComplete()
        .verify();
  }
}