package com.elanlum.ecs.ride.crud.service.impl;

import com.elanlum.ecs.ride.crud.repository.impl.DriverRideRequestRepo;
import com.elanlum.ecs.ride.crud.service.AbstractRideRequestService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DriverRideRequestService extends
    AbstractRideRequestService<DriverRideRequest, DriverRideRequestRepo> {

  @Autowired
  public DriverRideRequestService(DriverRideRequestRepo driverRideRequestRepo,
      ValidationService<DriverRideRequest> validationService,
      UserService userService) {
    super(driverRideRequestRepo, validationService, userService);
  }
}
