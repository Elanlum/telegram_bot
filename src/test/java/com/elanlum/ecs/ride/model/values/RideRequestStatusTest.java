package com.elanlum.ecs.ride.model.values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestCategory.UNIT)
class RideRequestStatusTest {

  @Test
  @DisplayName("Get available RideRequest status by string")
  void getAvailableRideRequest() {
    assertEquals(RideRequestStatus.AVAILABLE,
        RideRequestStatus.getRideRequestStatusByString("available").block());
  }

  @Test
  @DisplayName("Get matched RideRequest status by string")
  void getMatchedRideRequest() {
    assertEquals(RideRequestStatus.MATCHED,
        RideRequestStatus.getRideRequestStatusByString("matched").block());
  }

  @Test
  @DisplayName("Get canceled RideRequest status by string")
  void getCancelledRideRequest() {
    assertEquals(RideRequestStatus.CANCELED,
        RideRequestStatus.getRideRequestStatusByString("canceled").block());
  }

  @Test
  @DisplayName("Get null in case no match of input string with statuses")
  void getRideRequestStatusFailed() {
    assertNull(RideRequestStatus.getRideRequestStatusByString("unknown").block());
  }
}