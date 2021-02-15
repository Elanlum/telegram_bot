package com.elanlum.ecs.ride.crud.repository;

import com.elanlum.ecs.ride.model.common.AbstractRideRequest;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
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
public abstract class AbstractRideRequestRepo<T extends AbstractRideRequest> {

  protected final ReactiveMongoTemplate reactiveMongoTemplate;
  protected final Class<T> genericClass;

  public Mono<T> save(T ride) {
    return reactiveMongoTemplate.save(ride);
  }

  /**
   * Update field "status" for concrete id if {@link AbstractRideRequest} was AVAILABLE. {@link
   * FindAndModifyOptions} allows returning updated entity. Without this parameter method returns
   * entity before updating.
   *
   * @param rideRequestId - where we want update status
   * @param fromStatus - what status is expected to be in the entity
   * @param toStatus - set this status for field {@link RideRequestStatus} in entity
   * @return updated Mono
   */
  public Mono<T> updateStatus(@Nonnull String rideRequestId,
      @Nonnull RideRequestStatus fromStatus, @Nonnull RideRequestStatus toStatus) {
    return reactiveMongoTemplate.findAndModify(
        Query.query(Criteria.where("_id").is(rideRequestId))
            .addCriteria(Criteria.where("status").is(fromStatus)),
        Update.update("status", toStatus),
        new FindAndModifyOptions().returnNew(true), genericClass);
  }

  public Mono<T> findById(String id) {
    return reactiveMongoTemplate.findById(id, genericClass);
  }

  public Flux<T> findByUserId(String userId) {
    return reactiveMongoTemplate
        .find(Query.query(Criteria.where("userId").is(userId)), genericClass);
  }

  public Flux<T> getAvailableRequests() {
    return reactiveMongoTemplate.find(Query.query(Criteria.where("status").is(
        RideRequestStatus.AVAILABLE)), genericClass);
  }

  /**
   * Returns all requests with available status by passenger id.
   *
   * @param userId of the certain passenger
   * @return Flux of PassengerRideRequest objects
   */
  public Flux<T> findUserRequestByStatus(String userId, RideRequestStatus status) {
    return reactiveMongoTemplate.find(Query.query(Criteria.where("userId")
        .is(userId))
        .addCriteria(Criteria.where("status").is(status)), genericClass);
  }
}
