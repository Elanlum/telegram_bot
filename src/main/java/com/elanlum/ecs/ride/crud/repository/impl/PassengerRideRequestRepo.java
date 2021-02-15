package com.elanlum.ecs.ride.crud.repository.impl;

import com.elanlum.ecs.ride.crud.repository.AbstractRideRequestRepo;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PassengerRideRequestRepo extends AbstractRideRequestRepo<PassengerRideRequest> {

  @Autowired
  public PassengerRideRequestRepo(ReactiveMongoTemplate reactiveMongoTemplate) {
    super(reactiveMongoTemplate, PassengerRideRequest.class);
  }

  public Flux<PassengerRideRequest> getAll() {
    return reactiveMongoTemplate.findAll(PassengerRideRequest.class);
  }

  /**
   * This method is intended to give you passengers which have coincided time slots for a Ride.
   *
   * @param driverStart from diver request.
   * @param driverEnd from driver request.
   * @return all fitted passengers requests.
   */
  public Flux<PassengerRideRequest> getAvailablePassengerRequestsInTime(LocalDateTime driverStart,
      LocalDateTime driverEnd, String driverId) {

    Query query = Query.query(Criteria.where("status").is(RideRequestStatus.AVAILABLE))
        .addCriteria(Criteria.where("userId").ne(driverId))
        .addCriteria(Criteria.where("rideDate.end").gte(driverStart)
            .andOperator(Criteria.where("rideDate.start").lte(driverEnd)));

    return reactiveMongoTemplate.find(query, PassengerRideRequest.class);
  }
}
