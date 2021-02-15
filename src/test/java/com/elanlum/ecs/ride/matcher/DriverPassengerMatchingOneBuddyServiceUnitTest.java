package com.elanlum.ecs.ride.matcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.service.impl.RideService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainer;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.scheduling.config.NotificationConfiguration;
import com.elanlum.ecs.ride.scheduling.notifying.NotificationFacade;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class DriverPassengerMatchingOneBuddyServiceUnitTest {

  @Mock
  NotificationConfiguration configuration;
  @Mock
  DriverPassengerMatchingService driverPassengerMatchingService;
  @Mock
  RideRequestStatusUpdater statusUpdater;
  @Mock
  UserService userService;
  @Mock
  NotificationFacade notificationFacade;
  @Mock
  RideService rideService;
  @InjectMocks
  DriverPassengerMatchingOneBuddyService onePassengerService;

  LocalDateTime testLocalDateTime = LocalDateTime.now();
  Position passengerPosition = new Position(59.835168f, 30.345053f);
  Position driverPosition = new Position(59.833562f, 30.347907f);

  @Test
  @DisplayName("Method matchAndNotify works correctly and invokes all the right internal objects")
  void matchAndNotifyIsComplete() {
    User passenger = new User("2", "passenger", "passenger", "1", 1L);
    User driver = new User("4", "driver", "driver", "2", 2L);

    PassengerRideRequest updatedPassengerRideRequest = new PassengerRideRequest("1", "2",
        new Interval(testLocalDateTime, testLocalDateTime.plusMinutes(15)), passengerPosition,
        null, RideRequestStatus.MATCHED);
    DriverRideRequest updatedDriverRideRequest = new DriverRideRequest("2", "4",
        new Interval(testLocalDateTime, testLocalDateTime.plusMinutes(15)), driverPosition,
        null, RideRequestStatus.MATCHED);

    ScoringContainer scoringContainerOne = mock(ScoringContainer.class);
    ScoringContainer scoringContainerTwo = mock(ScoringContainer.class);
    ScoringContainer scoringContainerThree = mock(ScoringContainer.class);
    Flux<ScoringContainer> scoringContainerFlux = Flux
        .just(scoringContainerOne, scoringContainerTwo, scoringContainerThree);

    doReturn(scoringContainerFlux).when(driverPassengerMatchingService).getNearPassengers(any());
    doReturn("1").when(scoringContainerOne).getPassengerRequestId();
    doReturn("2").when(scoringContainerOne).getDriverRequestId();
    doReturn(updatedPassengerRideRequest).when(scoringContainerOne).getPassengerRequest();
    doReturn(Mono.just(true)).when(statusUpdater).updateStatusesToMatched("2", "1");

    Semaphore semaphore = new Semaphore(-1);
    when(userService.findById(anyString()))
        .thenAnswer(invocation -> {
          semaphore.release();
          return Mono.just(passenger);
        });

    doReturn(5).when(configuration).getMinutesBeforeTheRideStart();
    doNothing().when(notificationFacade)
        .sendScheduled(any(Notification.class), any(LocalDateTime.class));

    Ride createdRide = new Ride(driver, passenger, updatedDriverRideRequest,
        updatedPassengerRideRequest);

    when(rideService.save(any())).thenReturn(Mono.just(createdRide));

    onePassengerService.matchAndNotify(updatedDriverRideRequest);

    org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2),
        (Executable) semaphore::acquire);
    verify(driverPassengerMatchingService, times(1)).getNearPassengers(any());
    verifyNoMoreInteractions(driverPassengerMatchingService);
    verify(statusUpdater, times(1)).updateStatusesToMatched("2", "1");
    verifyNoMoreInteractions(statusUpdater);
    verify(scoringContainerOne, times(1)).getPassengerRequestId();
    verify(scoringContainerOne, times(1)).getPassengerRequest();
    verifyNoMoreInteractions(scoringContainerOne);
    verify(userService, times(2)).findById(anyString());
    verify(notificationFacade, times(2)).sendNow(any(Notification.class));
    verify(notificationFacade, times(2))
        .sendScheduled(any(Notification.class), any(LocalDateTime.class));
    verify(rideService, times(1)).save(any());
  }

  @Test
  @DisplayName("updateStatusesToMatched returns false causing exception")
  void matchAndNotifyThrowsStatusUpdatingException() {
    ScoringContainer scoringContainerOne = mock(ScoringContainer.class);
    ScoringContainer scoringContainerTwo = mock(ScoringContainer.class);
    ScoringContainer scoringContainerThree = mock(ScoringContainer.class);
    Flux<ScoringContainer> scoringContainerFlux =
        Flux.just(scoringContainerOne, scoringContainerTwo, scoringContainerThree);

    DriverRideRequest testDriverRideRequest = new DriverRideRequest("2", "4",
        new Interval(testLocalDateTime, testLocalDateTime.plusMinutes(15)), driverPosition,
        null, RideRequestStatus.MATCHED);
    Mono<DriverRideRequest> driverRequestMono = Mono.just(testDriverRideRequest);

    doReturn(scoringContainerFlux).when(driverPassengerMatchingService)
        .getNearPassengers(any());
    doReturn("2").when(scoringContainerOne).getDriverRequestId();
    doReturn("1").when(scoringContainerOne).getPassengerRequestId();
    doReturn(Mono.just(false)).when(statusUpdater).updateStatusesToMatched("2", "1");

    onePassengerService.matchAndNotify(testDriverRideRequest);
    verify(statusUpdater, times(1)).updateStatusesToMatched("2", "1");
    verifyZeroInteractions(userService, notificationFacade);
  }
}
