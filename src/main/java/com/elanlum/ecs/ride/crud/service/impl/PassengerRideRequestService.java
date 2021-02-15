package com.elanlum.ecs.ride.crud.service.impl;

import com.elanlum.ecs.ride.crud.repository.impl.PassengerRideRequestRepo;
import com.elanlum.ecs.ride.crud.service.AbstractRideRequestService;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.validation.ValidationService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class PassengerRideRequestService extends
    AbstractRideRequestService<PassengerRideRequest, PassengerRideRequestRepo> {

  @Autowired
  public PassengerRideRequestService(PassengerRideRequestRepo passengerRideRequestRepo,
      ValidationService<PassengerRideRequest> validationService,
      UserService userService) {
    super(passengerRideRequestRepo, validationService, userService);
  }

  public Flux<PassengerRideRequest> getAvailablePassengerRequestsInTime(LocalDateTime driverStart,
      LocalDateTime driverEnd, String driverId) {
    return abstractRideRequestRepo
        .getAvailablePassengerRequestsInTime(driverStart, driverEnd, driverId);
  }
}
