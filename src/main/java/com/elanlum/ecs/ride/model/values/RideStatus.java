package com.elanlum.ecs.ride.model.values;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

public enum RideStatus implements Serializable {
  OPENED,
  CLOSED;

  private static Map<String, RideStatus> map;

  static {
    map = Arrays.stream(RideStatus.values())
        .collect(Collectors.toMap(status -> status.name().toUpperCase(), status -> status));
  }

  /**
   * Method converts any string to RideStatus.
   *
   * @param rideCriteria any criteria prepared for mapping to RideStatus.
   * @return Optional of RideStatus value of empty if there is no such a status.
   */
  public static Mono<RideStatus> getRideStatusByString(String rideCriteria) {
    return Mono.justOrEmpty(map.get(rideCriteria.toUpperCase()));
  }
}
