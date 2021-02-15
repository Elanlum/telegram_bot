package com.elanlum.ecs.ride.matcher;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideRequestStatusUpdaterTest {

  @Mock
  DriverRideRequestService driverRideRequestService;
  @Mock
  PassengerRideRequestService passengerRideRequestService;
  @InjectMocks
  RideRequestStatusUpdater statusUpdater;

  DriverRideRequest updatedDriverRideRequest = new DriverRideRequest(
      "1", "Zhoka", interval(), position(), position(), RideRequestStatus.MATCHED);
  PassengerRideRequest updatedPassengerRideRequest = new PassengerRideRequest(
      "2", "Boka", interval(), position(), position(), RideRequestStatus.MATCHED);
  DriverRideRequest rolledBackDriverRideRequest = new DriverRideRequest(
      "1", "Zhoka", interval(), position(), position(), RideRequestStatus.AVAILABLE);
  PassengerRideRequest rolledBackPassengerRideRequest = new PassengerRideRequest(
      "2", "Boka", interval(), position(), position(), RideRequestStatus.AVAILABLE);

  @Test
  @DisplayName("Updater successfully updates both statuses and returns true")
  void updateStatusesToMatched_returnsTrue() {

    doReturn(Mono.just(updatedDriverRideRequest)).when(driverRideRequestService)
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    doReturn(Mono.just(updatedPassengerRideRequest)).when(passengerRideRequestService)
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);

    StepVerifier.create(statusUpdater.updateStatusesToMatched(updatedDriverRideRequest.getId(),
        updatedPassengerRideRequest.getId()))
        .assertNext(Assertions::assertTrue)
        .verifyComplete();
  }

  @Test
  @DisplayName("Driver's request could not be updated so passenger's request is rolled back")
  void givenUpdateStatusesToMatchFails_passengerRequestsStatusRolledBack() {

    doReturn(Mono.empty()).when(driverRideRequestService)
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    doReturn(Mono.just(updatedPassengerRideRequest)).when(passengerRideRequestService)
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    doReturn(Mono.just(rolledBackPassengerRideRequest)).when(passengerRideRequestService)
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.MATCHED, RideRequestStatus.AVAILABLE);

    StepVerifier.create(statusUpdater.updateStatusesToMatched(updatedDriverRideRequest.getId(),
        updatedPassengerRideRequest.getId()))
        .assertNext(Assertions::assertFalse)
        .verifyComplete();

    verify(driverRideRequestService, times(1))
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    verify(passengerRideRequestService, times(1))
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    verify(passengerRideRequestService, times(1))
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.MATCHED, RideRequestStatus.AVAILABLE);
    verifyNoMoreInteractions(driverRideRequestService, passengerRideRequestService);
  }

  @Test
  @DisplayName("Passenger's request could not be updated so driver's request is rolled back")
  void givenUpdateStatusesToMatchFails_driverRequestsStatusRolledBack() {

    doReturn(Mono.just(updatedDriverRideRequest)).when(driverRideRequestService)
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    doReturn(Mono.empty()).when(passengerRideRequestService)
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    doReturn(Mono.just(rolledBackDriverRideRequest)).when(driverRideRequestService)
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.MATCHED, RideRequestStatus.AVAILABLE);

    StepVerifier.create(statusUpdater.updateStatusesToMatched(updatedDriverRideRequest.getId(),
        updatedPassengerRideRequest.getId()))
        .assertNext(Assertions::assertFalse)
        .verifyComplete();

    verify(passengerRideRequestService, times(1))
        .updateStatus(updatedPassengerRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    verify(driverRideRequestService, times(1))
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
    verify(driverRideRequestService, times(1))
        .updateStatus(updatedDriverRideRequest.getId(),
            RideRequestStatus.MATCHED, RideRequestStatus.AVAILABLE);
    verifyNoMoreInteractions(driverRideRequestService, passengerRideRequestService);
  }

  private Interval interval() {
    return new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15));
  }

  private Position position() {
    return new Position(0f, 0f);
  }
}