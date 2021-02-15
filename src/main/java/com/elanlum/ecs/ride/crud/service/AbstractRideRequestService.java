package com.elanlum.ecs.ride.crud.service;

import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.repository.AbstractRideRequestRepo;
import com.elanlum.ecs.ride.exceptions.InvalidRequestParameterException;
import com.elanlum.ecs.ride.exceptions.InvalidRideRequestUpdatingException;
import com.elanlum.ecs.ride.exceptions.UserFromRideRequestDoesNotExist;
import com.elanlum.ecs.ride.model.common.AbstractRideRequest;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AbstractRideRequestService<T extends AbstractRideRequest,
    N extends AbstractRideRequestRepo<T>> {

  protected final N abstractRideRequestRepo;
  protected final ValidationService<T> validationService;
  protected final UserService userService;

  /**
   * Save rideRequest in repo. Firstly, check that rideRequest is valid(id = null). Secondly, check
   * that {@link User} with userId in rideRequest contains in repo.
   *
   * @param ride - {@link PassengerRideRequest} or {@link DriverRideRequest} that we want to save in
   *     repo.
   * @return {@link Mono}(saved in repo) that contains rideRequest or error.
   */
  public Mono<T> save(T ride) {
    return Flux.combineLatest(
        validationService.entityValidate(ride, ValidationForSave.class),
        userService.findById(ride.getUserId())
            .switchIfEmpty(Mono.error(
                new UserFromRideRequestDoesNotExist("userId:" + ride.getUserId()))),
        (source1, source2) -> source2)
        .flatMap(user -> abstractRideRequestRepo.save(ride))
        .doOnNext(ride1 -> log.debug("Ride request was created with id {}", ride1.getId()))
        .single();
  }

  /**
   * Update field "status" for concrete id.
   *
   * @param rideRequestId - where we want update status
   * @param fromStatus - what status is expected to be in the entity
   * @param toStatus - set this status for field {@link RideRequestStatus} in entity
   * @return {@link Mono} with updated {@link PassengerRideRequest} or {@link Mono} with error
   */
  public Mono<T> updateStatus(String rideRequestId, RideRequestStatus fromStatus,
      RideRequestStatus toStatus) {
    if (rideRequestId == null) {
      return Mono.error(new InvalidRideRequestUpdatingException("Ride request can't be null"));
    }
    if (fromStatus == null || toStatus == null) {
      return Mono.error(new InvalidRideRequestUpdatingException("Status can't be null"));
    }

    return abstractRideRequestRepo.updateStatus(rideRequestId, fromStatus, toStatus)
        .switchIfEmpty(Mono.fromSupplier(() -> {
          log.debug("Ride request was not found or ride participant status was not \"AVAILABLE\"");
          return null;
        }))
        .doOnNext(rideRequest ->
            log.debug("The status of given ride request with id {} was updated to {}",
                rideRequest.getId(), toStatus));
  }

  /**
   * This method cancels the ride request.
   *
   * @param rideRequestId is id of the request we want to cancel.
   * @return canceled ride request.
   */
  public Mono<T> cancelRequest(String rideRequestId) {
    if (rideRequestId == null) {
      return Mono.error(new InvalidRideRequestUpdatingException("Ride request can't be null"));
    }
    return updateStatus(rideRequestId, RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED);
  }

  public Mono<T> findById(String id) {
    return abstractRideRequestRepo.findById(id);
  }

  public Flux<T> findByUserId(String userId) {
    return abstractRideRequestRepo.findByUserId(userId);
  }

  /**
   * Returns all requests with available status by passenger id.
   *
   * @param userId of the certain passenger
   * @return Flux of PassengerRideRequest objects
   */
  public Flux<T> findAvailableRequestsByUserId(String userId, RideRequestCriteria criteria) {
    if (userId == null) {
      throw new InvalidRequestParameterException("Invalid request parameter: userId");
    }
    return RideRequestStatus.getRideRequestStatusByString(criteria.getStatus())
        .flatMapMany(status -> abstractRideRequestRepo.findUserRequestByStatus(userId, status));
  }

  public Flux<T> getAvailableRequests() {
    return abstractRideRequestRepo.getAvailableRequests();
  }
}
