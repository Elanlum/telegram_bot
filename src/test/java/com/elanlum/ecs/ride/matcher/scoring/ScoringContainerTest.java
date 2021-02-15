package com.elanlum.ecs.ride.matcher.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.map.service.MapService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.graphhopper.PathWrapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class ScoringContainerTest {

  @Mock
  private MapService mapService;

  @Mock
  private PathWrapper pathWrapper;

  @Test
  void scoring() {
    Position driverPosition = new Position(1.0f, 1.0f);
    Position passengerPosition = new Position(2.0f, 2.0f);
    PassengerRideRequest passengerRideRequest
        = new PassengerRideRequest("1", "1", null, passengerPosition, null,
        RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest
        = new DriverRideRequest("4", "4", null, driverPosition, null, RideRequestStatus.AVAILABLE);
    when(mapService.getDistance(driverPosition, passengerPosition))
        .thenReturn(pathWrapper);
    when(pathWrapper.getDistance()).thenReturn(10.0);

    ScoringContainer scoringContainer = new ScoringContainer(driverRideRequest,
        passengerRideRequest, mapService);

    assertThat(scoringContainer.getScore()).isCloseTo(0.1, offset(Math.pow(10, -6)));
    assertThat(scoringContainer.getPassengerRequestId()).isEqualTo(passengerRideRequest.getId());
    assertThat(scoringContainer.getDriverRequestId()).isEqualTo(driverRideRequest.getId());
    verify(mapService).getDistance(driverPosition, passengerPosition);
    verifyNoMoreInteractions(mapService);
  }

  @Test
  void cachingScore() {
    Position driverPosition = new Position(1.0f, 1.0f);
    Position passengerPosition = new Position(2.0f, 2.0f);
    PassengerRideRequest passengerRideRequest
        = new PassengerRideRequest("1", "1", null, passengerPosition, null,
        RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest
        = new DriverRideRequest("4", "4", null, driverPosition, null, RideRequestStatus.AVAILABLE);
    when(mapService.getDistance(driverPosition, passengerPosition))
        .thenReturn(pathWrapper);
    when(pathWrapper.getDistance()).thenReturn(10.0);

    ScoringContainer scoringContainer = new ScoringContainer(driverRideRequest,
        passengerRideRequest, mapService);
    double score1 = scoringContainer.getScore();
    double score2 = scoringContainer.getScore();

    assertThat(score1).isCloseTo(score2, offset(Math.pow(10, -6)));
    verify(mapService).getDistance(any(), any());
    verifyNoMoreInteractions(mapService);
  }
}