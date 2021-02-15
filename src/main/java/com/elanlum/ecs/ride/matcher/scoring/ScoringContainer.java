package com.elanlum.ecs.ride.matcher.scoring;

import com.elanlum.ecs.map.service.MapService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ScoringContainer {

  private final DriverRideRequest driverRequest;
  @Getter
  private final PassengerRideRequest passengerRequest;
  private final MapService mapService;
  private Double cachedScore;

  /**
   * Returns id of corresponding {@link DriverRideRequest}.
   */
  public String getDriverRequestId() {
    return driverRequest.getId();
  }

  /**
   * Returns id of corresponding {@link PassengerRideRequest}.
   */
  public String getPassengerRequestId() {
    return passengerRequest.getId();
  }

  /**
   * Calculates score of driver-passenger match.
   */
  public double getScore() {
    Double cachedScore = this.cachedScore;
    if (cachedScore != null) {
      log.debug("Returning cached score for {}-{} match: {}",
          getDriverRequestId(), getPassengerRequestId(), cachedScore);
      return cachedScore;
    }
    double distance = mapService.getDistance(
        driverRequest.getDeparturePoint(),
        passengerRequest.getDeparturePoint()
    ).getDistance();
    double score = 1.0 / distance;
    this.cachedScore = score;
    log.debug("Calculated score for {}-{} match: {}",
        getDriverRequestId(), getPassengerRequestId(), score);
    return score;
  }
}
