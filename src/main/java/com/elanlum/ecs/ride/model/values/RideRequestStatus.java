package com.elanlum.ecs.ride.model.values;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

public enum RideRequestStatus {
  AVAILABLE,
  MATCHED,
  CANCELED;

  private static Map<String, RideRequestStatus> map;

  static {
    map = Arrays.stream(RideRequestStatus.values())
        .collect(Collectors.toMap(status -> status.name().toUpperCase(), status -> status));
  }

  /**
   * Method compares input string with enum values and returns Mono of result.
   *
   * @param criteria input string
   * @return Mono object that contains Status or empty Mono
   */
  public static Mono<RideRequestStatus> getRideRequestStatusByString(String criteria) {
    return Mono.justOrEmpty(map.get(criteria.toUpperCase()));
  }
}
