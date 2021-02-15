package com.elanlum.ecs.ride.matcher.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.map.service.MapService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class ScoringContainerFactoryTest {

  @Mock
  private MapService mapService;

  @Mock
  private PassengerRideRequest passengerRideRequest;

  @Mock
  private DriverRideRequest driverRideRequest;

  @InjectMocks
  private ScoringContainerFactory containerFactory;

  @Test
  void createPriorityContainer() {
    String driverRequestId = "driverRequestId";
    when(driverRideRequest.getId()).thenReturn(driverRequestId);
    String passengerRequestId = "passengerRequestId";
    when(passengerRideRequest.getId()).thenReturn(passengerRequestId);

    ScoringContainer scoringContainer
        = containerFactory.create(driverRideRequest, passengerRideRequest);

    assertThat(scoringContainer.getPassengerRequestId()).isEqualTo(passengerRequestId);
    assertThat(scoringContainer.getDriverRequestId()).isEqualTo(driverRequestId);
  }
}