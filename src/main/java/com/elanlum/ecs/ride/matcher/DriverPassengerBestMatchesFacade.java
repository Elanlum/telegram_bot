package com.elanlum.ecs.ride.matcher;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DriverPassengerBestMatchesFacade {

  private final DriverRideRequestService driverRideRequestService;
  private final DriverPassengerMatchingService matchingService;

  /**
   * Method that returns Flux object of passenger's requests.
   *
   * @param driverRideRequestId - used to determine nearest passengers to the specific driver.
   */
  public Flux<PassengerRideRequest> getBestPassengers(String driverRideRequestId) {
    Mono<DriverRideRequest> driverRideRequestMono = driverRideRequestService
        .findById(driverRideRequestId);
    return matchingService.getNearPassengers(driverRideRequestMono)
        .map(scoringContainer -> scoringContainer.getPassengerRequest());
  }

}
