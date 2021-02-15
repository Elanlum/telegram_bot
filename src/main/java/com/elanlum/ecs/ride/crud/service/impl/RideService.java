package com.elanlum.ecs.ride.crud.service.impl;

import com.elanlum.ecs.ride.crud.controller.values.RideCriteria;
import com.elanlum.ecs.ride.crud.repository.impl.RideRepository;
import com.elanlum.ecs.ride.exceptions.FeedbackUpdateException;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.RideStatus;
import com.elanlum.ecs.validation.ValidationService;
import javax.annotation.Nonnull;
import javax.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class RideService {

  private final RideRepository rideRepository;
  private final ValidationService<Ride> validationService;

  /**
   * Uses repo save method to pass valid Ride to DB.
   *
   * @return Mono from valid Ride
   */
  public Mono<Ride> save(Ride ride) {
    return validationService.entityValidate(ride, Default.class)
        .flatMap(rideRepository::save)
        .doOnNext(ride1 -> log.debug("Ride with id {} was saved", ride1.getId()));
  }

  /**
   * Returns Ride by Its ID.
   *
   * @param id of Ride
   * @return Mono from desired Ride
   */
  public Mono<Ride> findById(String id) {
    return rideRepository.findById(id);
  }

  /**
   * Calls Driver update method of Repo in case it feedback has a null value.
   *
   * @param rideId Ride id to be passed to repo method
   * @param feedback Boolean value to be passed to repo method
   * @return Mono that contains updated Ride
   */
  public Mono<Ride> updateDriverFeedback(String rideId, Feedback feedback) {
    if (rideId == null) {
      return Mono.error(new FeedbackUpdateException("Ride Id was null"));
    }
    return rideRepository.updateDriverFeedback(rideId, feedback);
  }

  /**
   * Calls Passenger update method of Repo in case it feedback has a null value.
   *
   * @param rideId Ride id to be passed to repo method
   * @param feedback Boolean value to be passed to repo method
   * @return Mono that contains updated Ride
   */
  public Mono<Ride> updatePassengerFeedback(String rideId, Feedback feedback) {
    if (rideId == null) {
      return Mono.error(new FeedbackUpdateException("Ride Id was null"));
    }
    return rideRepository.updatePassengerFeedback(rideId, feedback);
  }

  /**
   * Returns all opened Rides entities.
   *
   * @param userId user userId. Must be not null.
   * @param criteria criteria from path. Object must be not null, but value in object can be.
   * @return Flux of Rides from DB
   */
  public Flux<Ride> getRidesForUserByStatus(@Nonnull String userId,
      @Nonnull RideCriteria criteria) {
    if (criteria.getStatus() == null) {
      return rideRepository.getRidesForUserByStatus(userId, null);
    }

    return RideStatus.getRideStatusByString(criteria.getStatus())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Wrong criteria parameter"
            + " was passed to the method")))
        .flatMapMany(status -> rideRepository.getRidesForUserByStatus(userId, status));
  }
}
