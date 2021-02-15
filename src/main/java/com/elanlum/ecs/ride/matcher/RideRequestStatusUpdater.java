package com.elanlum.ecs.ride.matcher;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideRequestStatusUpdater {

  private final DriverRideRequestService driverRideRequestService;
  private final PassengerRideRequestService passengerRideRequestService;

  Mono<Boolean> updateStatusesToMatched(String driverRequestId,
      String passengerRequestId) {
    return Mono
        .zip(driverRideRequestService.updateStatus(driverRequestId,
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED)
                .map(driverRideRequest -> true)
                .defaultIfEmpty(false),
            passengerRideRequestService.updateStatus(passengerRequestId,
                RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED)
                .map(passengerRideRequest -> true)
                .defaultIfEmpty(false))
        .flatMap(driverAndPassengerRequestsUpdated -> {
          Boolean driverRequestUpdated = driverAndPassengerRequestsUpdated.getT1();
          Boolean passengerRequestUpdated = driverAndPassengerRequestsUpdated.getT2();
          if (driverRequestUpdated && passengerRequestUpdated) {
            log.info(
                "Driver request {} and passenger request {} statuses successfully matched",
                driverRequestId, passengerRequestId);
            return Mono.just(true);
          } else if (!driverRequestUpdated && passengerRequestUpdated) {
            log.debug(
                "Driver request {} status was not updated "
                    + "and passenger request {} is being rolled back to available",
                driverRequestId, passengerRequestId);
            return passengerRideRequestService
                .updateStatus(passengerRequestId,
                    RideRequestStatus.MATCHED, RideRequestStatus.AVAILABLE)
                .map(passengerRideRequest -> false);
          } else if (!passengerRequestUpdated && driverRequestUpdated) {
            log.debug(
                "Passenger request {} status was not updated "
                    + "and driver request {} is being rolled back to available",
                passengerRequestId, driverRequestId);
            return driverRideRequestService
                .updateStatus(driverRequestId,
                    RideRequestStatus.MATCHED, RideRequestStatus.AVAILABLE)
                .map(driverRideRequest -> false);
          } else {
            return Mono.just(false);
          }
        });
  }
}
