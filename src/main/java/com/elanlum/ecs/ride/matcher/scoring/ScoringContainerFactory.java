package com.elanlum.ecs.ride.matcher.scoring;

import com.elanlum.ecs.map.service.MapService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoringContainerFactory {

  private final MapService mapService;

  public ScoringContainer create(
      DriverRideRequest driverRequest,
      PassengerRideRequest passengerRequest) {
    return new ScoringContainer(driverRequest, passengerRequest, mapService);
  }
}
