package com.elanlum.ecs.ride.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainer;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
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
class DriverPassengerBestMatchesFacadeTest {

  @Mock
  DriverRideRequestService driverRideRequestService;
  @Mock
  DriverPassengerMatchingService matchingService;
  @InjectMocks
  DriverPassengerBestMatchesFacade facade;
  DriverRideRequest driverRideRequest = new DriverRideRequest("dReqId", null,
      null, null, null, RideRequestStatus.AVAILABLE);
  PassengerRideRequest passengerRideRequest = new PassengerRideRequest("pasReqId", null,
      null, null, null, RideRequestStatus.AVAILABLE);
  private ScoringContainer scoringContainer = mock(ScoringContainer.class);

  @Test
  void getBestPassengers() {

    doReturn(Flux.just(scoringContainer)).when(matchingService).getNearPassengers(any(Mono.class));
    doReturn(Mono.just(driverRideRequest)).when(driverRideRequestService).findById("dReqId");
    doReturn(passengerRideRequest).when(scoringContainer).getPassengerRequest();

    StepVerifier.create(facade.getBestPassengers("dReqId"))
        .assertNext(request ->
            assertEquals(request.getId(), passengerRideRequest.getId()))
        .expectComplete()
        .verify();
  }
}