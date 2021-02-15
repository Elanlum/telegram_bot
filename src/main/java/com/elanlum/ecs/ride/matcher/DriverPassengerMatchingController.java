package com.elanlum.ecs.ride.matcher;

import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
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
@RequestMapping("/pairs")
public class DriverPassengerMatchingController {

  private final DriverPassengerBestMatchesFacade matchingService;

  @GetMapping(value = "/driver/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Flux<PassengerRideRequest> getNearPassengers(@PathVariable String id) {
    return matchingService.getBestPassengers(id);
  }
}

