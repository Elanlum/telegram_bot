package com.elanlum.ecs.ride.crud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.repository.impl.DriverRideRequestRepo;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.validation.ValidationService;
import com.elanlum.ecs.ride.exceptions.InvalidRequestParameterException;
import com.elanlum.ecs.ride.exceptions.InvalidRideRequestUpdatingException;
import com.elanlum.ecs.ride.exceptions.UserFromRideRequestDoesNotExist;
import com.elanlum.ecs.ride.matcher.DriverPassengerMatchingOneBuddyService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class DriverRideRequestServiceUnitTest {

  @Mock
  DriverRideRequestRepo driverRideRequestRepo;
  @Mock
  ValidationService<DriverRideRequest> validationService;
  @Mock
  UserService userService;
  @Mock
  DriverPassengerMatchingOneBuddyService oneBuddyService;
  @InjectMocks
  DriverRideRequestService driverRideRequestService;

  DriverRideRequest driverRideRequest = new DriverRideRequest(
      "1", "1",
      new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
      new Position(0, 0),
      new Position(0, 0),
      RideRequestStatus.AVAILABLE);

  User user = new User(
      "1", "testUser", "Dummy", "2", null);

  @Test
  @DisplayName("Saving a driver ride request through service returns available ride request")
  void save() {
    when(validationService.entityValidate(driverRideRequest, ValidationForSave.class))
        .thenReturn(Mono.just(driverRideRequest));
    when(userService.findById("1")).thenReturn(Mono.just(user));
    when(driverRideRequestRepo.save(driverRideRequest)).thenReturn(Mono.just(driverRideRequest));

    DriverRideRequest testDriverRideRequest = driverRideRequestService.save(driverRideRequest)
        .block();
    assertNotNull(testDriverRideRequest);
    assertEquals(RideRequestStatus.AVAILABLE, testDriverRideRequest.getStatus());
    verify(driverRideRequestRepo, times(1)).save(driverRideRequest);
    verifyNoMoreInteractions(driverRideRequestRepo);
    verify(validationService, times(1)).entityValidate(driverRideRequest, ValidationForSave.class);
    verify(userService, times(1)).findById("1");
  }

  @Test
  @DisplayName("Failed saving a driver ride request with non existent user id.")
  void saveRideWithNonexistentUserId() {
    when(validationService.entityValidate(driverRideRequest, ValidationForSave.class))
        .thenReturn(Mono.just(driverRideRequest));
    when(userService.findById("1")).thenReturn(Mono.empty());

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectError(UserFromRideRequestDoesNotExist.class)
        .verify();
  }

  @Test
  @DisplayName("Find a driver ride request by its id.")
  void findById() {
    when(driverRideRequestRepo.findById("1")).thenReturn(Mono.just(driverRideRequest));
    Mono<DriverRideRequest> mono = driverRideRequestService.findById("1");
    DriverRideRequest testDriverRideRequest = mono.block();

    assertEquals(driverRideRequest.getId(), testDriverRideRequest.getId());
    assertEquals(driverRideRequest.getUserId(), testDriverRideRequest.getUserId());
    assertEquals(driverRideRequest.getRole(), testDriverRideRequest.getRole());
    verify(driverRideRequestRepo, times(1)).findById("1");
    verifyNoMoreInteractions(driverRideRequestRepo);
  }

  @Test
  @DisplayName("Find a driver ride request by user id.")
  public void findDriverRideRequestByUserId() {
    when(driverRideRequestRepo.findByUserId("1")).thenReturn(Flux.just(driverRideRequest));
    DriverRideRequest testDriverRideRequest = driverRideRequestService.findByUserId("1")
        .blockFirst();

    assertEquals(driverRideRequest.getId(), testDriverRideRequest.getId());
    assertEquals(driverRideRequest.getUserId(), testDriverRideRequest.getUserId());
    verify(driverRideRequestRepo, times(1)).findByUserId(anyString());
    verifyNoMoreInteractions(driverRideRequestRepo);
  }

  @Test
  @DisplayName("Get available driver requests")
  public void findAllReqsWithAvailableStatus() {
    when(driverRideRequestRepo.getAvailableRequests())
        .thenReturn(Flux.just(driverRideRequest));
    StepVerifier.create(driverRideRequestService.getAvailableRequests())
        .expectNext(driverRideRequest);
    verify(driverRideRequestRepo, times(1)).getAvailableRequests();
    verifyNoMoreInteractions(driverRideRequestRepo);
  }

  @Test
  @DisplayName("Find all rideRequests by driverId")
  void findAvailableDriverRideRequest() {
    DriverRideRequest requestOne = new DriverRideRequest("1", "sameUser",
        null, null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest requestTwo = new DriverRideRequest("2", "sameUser",
        null, null, null, RideRequestStatus.MATCHED);
    DriverRideRequest requestThree = new DriverRideRequest("3", "sameUser",
        null, null, null, RideRequestStatus.AVAILABLE);

    Flux<DriverRideRequest> flux = Flux.just(requestOne, requestTwo, requestThree);

    when(driverRideRequestRepo.findUserRequestByStatus(any(), any())).thenReturn(flux);

    StepVerifier.create(driverRideRequestService
        .findAvailableRequestsByUserId("sameUser", new RideRequestCriteria("available")))
        .assertNext(response -> {
          assertEquals(response.getId(), "1");
          assertEquals(response.getUserId(), "sameUser");
        })
        .assertNext(response -> assertEquals(response.getId(), "2"))
        .assertNext(response -> assertEquals(response.getId(), "3"))
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Receive Exception if given driverId is null")
  void failedDriverRideRequest() {
    assertThrows(InvalidRequestParameterException.class,
        () -> driverRideRequestService.findAvailableRequestsByUserId(null,
            new RideRequestCriteria("available")));
  }

  @Test
  @DisplayName("Successful cancellation of the driver ride request")
  void cancelRequestSuccessfully() {
    DriverRideRequest beforeCancelRequest = new DriverRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.AVAILABLE);

    DriverRideRequest afterCancelRequest = new DriverRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.CANCELED);

    when(driverRideRequestRepo
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .thenReturn(Mono.just(afterCancelRequest));

    StepVerifier.create(driverRideRequestService.cancelRequest(beforeCancelRequest.getId()))
        .assertNext(
            updatedRequest -> assertEquals(RideRequestStatus.CANCELED, updatedRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Failed cancellation of the driver ride request with non matching status")
  void cancelRequestFailure() {
    DriverRideRequest beforeCancelRequest = new DriverRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.MATCHED);

    when(driverRideRequestRepo
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .thenReturn(Mono.empty());

    StepVerifier.create(driverRideRequestService.cancelRequest(beforeCancelRequest.getId()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Failed cancellation of the driver ride request with null id")
  void cancelRequestFailed() {
    StepVerifier.create(driverRideRequestService.cancelRequest(null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }
}
