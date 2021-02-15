package com.elanlum.ecs.ride.crud.controller;

import com.elanlum.ecs.ride.crud.controller.values.CancelStatus;
import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/passengerRide")
public class PassengerRideRequestController {

  private final PassengerRideRequestService passengerRideRequestService;

  @PostMapping("/save")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<PassengerRideRequest> save(@RequestBody PassengerRideRequest ride) {
    return passengerRideRequestService.save(ride);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<PassengerRideRequest> findById(@PathVariable String id) {
    return passengerRideRequestService.findById(id);
  }

  @GetMapping("/userId/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public Flux<PassengerRideRequest> findByUserId(@PathVariable String userId) {
    return passengerRideRequestService.findByUserId(userId);
  }


  /**
   * Method cancels the ride request's status.
   *
   * @param id is id of existent ride request
   * @param status is a "canceled" status for this request
   * @return canceled ride request
   */
  @PatchMapping("/users/{userId}/passenger-requests/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<PassengerRideRequest> cancelRequest(@PathVariable String userId,
      @PathVariable String id, @RequestBody @Valid CancelStatus status) {

    return passengerRideRequestService.cancelRequest(id)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
  }

  @GetMapping("/users/{userId}/passenger-requests")
  @ResponseStatus(HttpStatus.OK)
  public Flux<PassengerRideRequest> findActiveRequestsByUserId(@PathVariable String userId,
      RideRequestCriteria criteria) {
    return passengerRideRequestService.findAvailableRequestsByUserId(userId, criteria);
  }
}
