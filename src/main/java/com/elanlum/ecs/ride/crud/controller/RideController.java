package com.elanlum.ecs.ride.crud.controller;

import com.elanlum.ecs.ride.crud.controller.values.RideCriteria;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.ride.model.common.Ride;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class RideController {

  private final RideService rideService;

  @GetMapping("/{userId}/rides")
  @ResponseStatus(HttpStatus.OK)
  public Flux<Ride> getRidesForUserByStatus(@PathVariable String userId, RideCriteria criteria) {
    return rideService.getRidesForUserByStatus(userId, criteria);
  }
}
