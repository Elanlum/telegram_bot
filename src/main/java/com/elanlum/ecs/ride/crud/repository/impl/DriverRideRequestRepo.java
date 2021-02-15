package com.elanlum.ecs.ride.crud.repository.impl;

import com.elanlum.ecs.ride.crud.repository.AbstractRideRequestRepo;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DriverRideRequestRepo extends AbstractRideRequestRepo<DriverRideRequest> {

  @Autowired
  public DriverRideRequestRepo(ReactiveMongoTemplate reactiveMongoTemplate) {
    super(reactiveMongoTemplate, DriverRideRequest.class);
  }
}
