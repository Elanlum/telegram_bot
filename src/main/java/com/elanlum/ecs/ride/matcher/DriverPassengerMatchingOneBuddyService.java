package com.elanlum.ecs.ride.matcher;

import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.BeforeRideNotification;
import com.elanlum.ecs.notification.values.NotificationRecipient;
import com.elanlum.ecs.notification.values.RideMatchingNotification;
import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.ride.exceptions.RideRequestStatusUpdatingException;
import com.elanlum.ecs.ride.scheduling.config.NotificationConfiguration;
import com.elanlum.ecs.ride.scheduling.notifying.NotificationFacade;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainer;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverPassengerMatchingOneBuddyService {

  private final DriverPassengerMatchingService driverPassengerMatchingService;
  private final RideRequestStatusUpdater rideRequestStatusUpdater;
  private final UserService userService;
  private final RideService rideService;
  private final NotificationFacade notificationFacade;
  private final NotificationConfiguration notificationConfiguration;

  /**
   * This method combine matching service and notification. As result, it put notification to
   * passenger and driver with minimal useful info in {@link NotificationMessageQueue}.
   *
   * @param driverRideRequest - find near passenger for this request
   */
  public void matchAndNotify(DriverRideRequest driverRideRequest) {

    getPassenger(Mono.just(driverRideRequest))
        .flatMap(passengerRideRequest -> Mono.zip(
            userService.findById(driverRideRequest.getUserId()),
            userService.findById(passengerRideRequest.getUserId()),
            Mono.just(passengerRideRequest)))
        .flatMap(driverPassengerAndPassengersRequest -> createRide(driverRideRequest,
            driverPassengerAndPassengersRequest))
        .map(ride -> {
          notificationFacade.sendNow(
              new RideMatchingNotification(ride.getDriver(),
                  "We organized a ride for you.", ride, NotificationRecipient.DRIVER));
          notificationFacade.sendNow(
              new RideMatchingNotification(ride.getPassenger(),
                  "We organized a ride for you.", ride, NotificationRecipient.PASSENGER));
          log.debug("A driver {} and a passenger {} were matched and notified.",
              ride.getDriver(), ride.getPassenger());
          return ride;
        })
        .subscribe(this::notifyAboutTheRideStart,
            throwable -> log.warn("Matching failed: ", throwable));
  }

  /**
   * Method gives one first passenger from three best matches.
   *
   * @param driverRideRequestMono driver request from anywhere
   * @return Mono of PassengerRideRequest
   */
  private Mono<PassengerRideRequest> getPassenger(Mono<DriverRideRequest> driverRideRequestMono) {

    Flux<ScoringContainer> nearPassengers = driverPassengerMatchingService
        .getNearPassengers(driverRideRequestMono);

    Flux<PassengerRideRequest> passengerRequestFlux = nearPassengers.take(1)
        .flatMap(scoringContainer -> Mono.zip(Mono.just(scoringContainer), rideRequestStatusUpdater
            .updateStatusesToMatched(scoringContainer.getDriverRequestId(),
                scoringContainer.getPassengerRequestId())))
        .map(scoringContainerAndStatusesUpdated -> {
          Boolean bothStatusesUpdated = scoringContainerAndStatusesUpdated.getT2();
          ScoringContainer container = scoringContainerAndStatusesUpdated.getT1();
          if (!bothStatusesUpdated) {
            throw new RideRequestStatusUpdatingException(
                "Matching driver " + container.getDriverRequestId() + " with passenger "
                    + container.getPassengerRequestId() + " failed because the found passenger "
                    + "request's status was not \"AVAILABLE\" anymore");
          }
          return container.getPassengerRequest();
        });

    return Mono.from(passengerRequestFlux);
  }

  private Mono<Ride> createRide(DriverRideRequest driverRideRequest,
      Tuple3<User, User, PassengerRideRequest> tuple) {
    User driver = tuple.getT1();
    User passenger = tuple.getT2();
    PassengerRideRequest passengerRideRequest = tuple.getT3();
    Ride createdRide = new Ride(driver, passenger, driverRideRequest, passengerRideRequest);

    return rideService.save(createdRide);
  }

  private void notifyAboutTheRideStart(Ride ride) {
    int minutesBeforeTheRideStart = notificationConfiguration.getMinutesBeforeTheRideStart();
    User driver = ride.getDriver();
    User passenger = ride.getPassenger();
    LocalDateTime notifyBeforeTheRide = ride.getRideDateTime()
        .minus(minutesBeforeTheRideStart, ChronoUnit.MINUTES);

    notificationFacade.sendScheduled(new BeforeRideNotification(driver,
        "Your ride starts in " + minutesBeforeTheRideStart + " minutes", ride,
        NotificationRecipient.DRIVER), notifyBeforeTheRide);
    notificationFacade.sendScheduled(new BeforeRideNotification(passenger,
        "Your ride starts in " + minutesBeforeTheRideStart + " minutes", ride,
        NotificationRecipient.PASSENGER), notifyBeforeTheRide);
  }
}
