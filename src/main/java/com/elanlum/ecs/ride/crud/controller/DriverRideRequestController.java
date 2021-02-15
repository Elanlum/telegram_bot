package com.elanlum.ecs.ride.crud.controller;

import com.elanlum.ecs.ride.crud.controller.values.CancelStatus;
import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
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
@RequestMapping("/driverRide")
public class DriverRideRequestController {

  private final DriverRideRequestService driverRideRequestService;

  @PostMapping("/save")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<DriverRideRequest> save(@RequestBody DriverRideRequest ride) {
    return driverRideRequestService.save(ride);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<DriverRideRequest> findById(@PathVariable String id) {
    return driverRideRequestService.findById(id);
  }

  @GetMapping("/userId/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public Flux<DriverRideRequest> findByUserId(@PathVariable String userId) {
    return driverRideRequestService.findByUserId(userId);
  }

  /**
   * Method cancels the ride request's status.
   *
   * @param id is id of existent ride request
   * @param status is a "canceled" status for this request
   * @return canceled ride request
   */
  @PatchMapping("/users/{userId}/driver-requests/{id}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<DriverRideRequest> cancelRequest(@PathVariable String userId,
      @PathVariable String id, @RequestBody @Valid CancelStatus status) {

    return driverRideRequestService.cancelRequest(id)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
  }

  @GetMapping("/users/{userId}/driver-requests")
  @ResponseStatus(HttpStatus.OK)
  public Flux<DriverRideRequest> findActiveRequestsByUserId(@PathVariable String userId,
      RideRequestCriteria criteria) {
    return driverRideRequestService.findAvailableRequestsByUserId(userId, criteria);
  }
}
