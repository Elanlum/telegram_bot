package com.elanlum.ecs.ride.crud.repository.impl;

import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.RideStatus;
import java.util.Objects;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RideRepository {

  protected final ReactiveMongoTemplate reactiveMongoTemplate;

  /**
   * Method saves Ride into DB.
   *
   * @return Mono from successfully saved Ride
   */
  public Mono<Ride> save(Ride ride) {
    return reactiveMongoTemplate.save(ride);
  }

  /**
   * Returns Ride by its Id.
   *
   * @param id of desired Ride
   * @return Mono from Ride
   */
  public Mono<Ride> findById(String id) {
    return reactiveMongoTemplate.findById(id, Ride.class);
  }

  /**
   * Sets a boolean field of Feedback for Driver.
   *
   * @param rideId Id of a certain ride that field needs to be set
   * @param feedback Entity that contains boolean value to set
   * @return Mono object that contains updated Ride entity
   */
  public Mono<Ride> updateDriverFeedback(String rideId, Feedback feedback) {
    return reactiveMongoTemplate.findAndModify(
        Query.query(Criteria.where("_id").is(rideId).and("driverFeedback").exists(false)),
        Update.update("driverFeedback", feedback),
        new FindAndModifyOptions().returnNew(true), Ride.class);
  }

  /**
   * Sets a boolean field of Passenger for Driver.
   *
   * @param rideId Id of a certain ride that field needs to be set
   * @param feedback Entity that contains boolean value to set
   * @return Mono object that contains updated Ride entity
   */
  public Mono<Ride> updatePassengerFeedback(@Nonnull String rideId, Feedback feedback) {
    return reactiveMongoTemplate.findAndModify(
        Query.query(Criteria.where("_id").is(rideId).and("passengerFeedback").exists(false)),
        Update.update("passengerFeedback", feedback),
        new FindAndModifyOptions().returnNew(true), Ride.class);
  }

  /**
   * Returns all opened Rides entities.
   *
   * @param userId user userId.
   * @param rideStatus Status for the Ride entity.
   * @return Flux of Rides from DB
   */
  public Flux<Ride> getRidesForUserByStatus(String userId, RideStatus rideStatus) {
    Criteria fieldsCriteria = new Criteria()
        .orOperator(Criteria.where("driver.id").is(userId),
            Criteria.where("passenger.id").is(userId));

    if (Objects.nonNull(rideStatus)) {
      return reactiveMongoTemplate.find(Query.query(
          Criteria.where("status").is(rideStatus)
              .andOperator(fieldsCriteria)), Ride.class);
    }
    return reactiveMongoTemplate.findAll(Ride.class);
  }
}
