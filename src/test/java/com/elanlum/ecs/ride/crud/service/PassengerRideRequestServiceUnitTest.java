package com.elanlum.ecs.ride.crud.service;

import static com.elanlum.ecs.ride.model.values.RideRequestStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.repository.impl.PassengerRideRequestRepo;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.validation.ValidationService;
import com.elanlum.ecs.ride.exceptions.InvalidRequestParameterException;
import com.elanlum.ecs.ride.exceptions.InvalidRideRequestUpdatingException;
import com.elanlum.ecs.ride.exceptions.UserFromRideRequestDoesNotExist;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
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
class PassengerRideRequestServiceUnitTest {

  @Mock
  PassengerRideRequestRepo passengerRideRequestRepo;
  @Mock
  ValidationService<PassengerRideRequest> validationService;
  @Mock
  UserService userService;
  @InjectMocks
  PassengerRideRequestService passengerRideRequestService;

  private PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
      "1", "1",
      new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
      new Position(0, 0),
      new Position(0, 0), RideRequestStatus.AVAILABLE);

  private User user = new User(
      "1", "testUser", "Dummy", "2", null);

  @Test
  @DisplayName("Saving a passenger ride request through service returns available ride request")
  void save() {
    when(validationService.entityValidate(passengerRideRequest, ValidationForSave.class))
        .thenReturn(Mono.just(passengerRideRequest));
    when(userService.findById("1")).thenReturn(Mono.just(user));
    when(passengerRideRequestRepo.save(passengerRideRequest))
        .thenReturn(Mono.just(passengerRideRequest));

    Mono<PassengerRideRequest> mono = passengerRideRequestService.save(passengerRideRequest);
    PassengerRideRequest testPassengerRideRequest = mono.block();
    assertNotNull(testPassengerRideRequest);
    verify(passengerRideRequestRepo, times(1)).save(passengerRideRequest);
    verifyNoMoreInteractions(passengerRideRequestRepo);
    verify(validationService, times(1))
        .entityValidate(passengerRideRequest, ValidationForSave.class);
    verify(userService, times(1)).findById("1");
  }

  @Test
  @DisplayName("Failed saving a passenger ride request with non existent user id.")
  void saveRideWithNonexistentUserId() {
    when(validationService.entityValidate(passengerRideRequest, ValidationForSave.class))
        .thenReturn(Mono.just(passengerRideRequest));
    when(userService.findById("1")).thenReturn(Mono.empty());

    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectError(UserFromRideRequestDoesNotExist.class)
        .verify();
  }

  @Test
  @DisplayName("Find a passenger ride request by its id.")
  void findById() {
    when(passengerRideRequestRepo.findById("1")).thenReturn(Mono.just(passengerRideRequest));
    Mono<PassengerRideRequest> mono = passengerRideRequestService.findById("1");
    PassengerRideRequest testPassengerRideRequest = mono.block();
    assertEquals(passengerRideRequest.getId(), testPassengerRideRequest.getId());
    verify(passengerRideRequestRepo, times(1)).findById("1");
    verifyNoMoreInteractions(passengerRideRequestRepo);
  }

  @Test
  @DisplayName("Find a passenger ride request by user id.")
  public void findPassengerRideRequestByUserId() {
    when(passengerRideRequestRepo.findByUserId("1")).thenReturn(Flux.just(passengerRideRequest));
    PassengerRideRequest testPassengerRideRequest = passengerRideRequestService.findByUserId("1")
        .blockFirst();

    assertEquals(passengerRideRequest.getId(), testPassengerRideRequest.getId());
    assertEquals(passengerRideRequest.getUserId(), testPassengerRideRequest.getUserId());
    verify(passengerRideRequestRepo, times(1)).findByUserId(anyString());
    verifyNoMoreInteractions(passengerRideRequestRepo);
  }

  @Test
  @DisplayName("Get available passRequests")
  public void findAllReqsWithAvailableStatus() {
    when(passengerRideRequestRepo.getAvailableRequests())
        .thenReturn(Flux.just(passengerRideRequest));
    PassengerRideRequest passengerRideRequestFromFlux = passengerRideRequestService
        .getAvailableRequests().blockFirst();

    assertEquals(RideRequestStatus.AVAILABLE, passengerRideRequestFromFlux.getStatus());
    verify(passengerRideRequestRepo, times(1)).getAvailableRequests();
    verifyNoMoreInteractions(passengerRideRequestRepo);
  }

  @Test
  @DisplayName("Get available passRequests with coincided time")
  public void findAllReqsWithAvailableStatusInTime() {
    DriverRideRequest driver = new DriverRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0),
        RideRequestStatus.AVAILABLE);

    PassengerRideRequest passengerRideRequestTimed = new PassengerRideRequest(
        "1", "2",
        new Interval(LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusMinutes(100L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.AVAILABLE);

    when(passengerRideRequestRepo
        .getAvailablePassengerRequestsInTime(driver.getRideDate().getStart(),
            driver.getRideDate().getEnd(), driver.getUserId()))
        .thenReturn(Flux.just(passengerRideRequestTimed));
    Flux<PassengerRideRequest> allRequests = passengerRideRequestService
        .getAvailablePassengerRequestsInTime(driver.getRideDate().getStart(),
            driver.getRideDate().getEnd(), driver.getUserId());

    StepVerifier.create(allRequests)
        .assertNext(passengerRideRequestFromDb -> {
          assertEquals(RideRequestStatus.AVAILABLE, passengerRideRequestFromDb.getStatus());
          assertEquals(AVAILABLE, passengerRideRequestFromDb.getStatus());
          assertEquals("2", passengerRideRequestFromDb.getUserId());
        })
        .verifyComplete();

    verify(passengerRideRequestRepo, times(1))
        .getAvailablePassengerRequestsInTime(any(), any(), any());
    verifyNoMoreInteractions(passengerRideRequestRepo);
  }

  @Test
  @DisplayName("Trying to invoke updateStatus(...) method with null id returns exception")
  void updatePassengerRequestWithNullId() {
    StepVerifier.create(passengerRideRequestService
        .updateStatus(null, RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();

    verifyZeroInteractions(passengerRideRequestRepo);
  }

  @Test
  @DisplayName("Invoking updateStatus(...) with null status argument returns exception")
  void updatePassengerRequestWithNullStatus() {
    StepVerifier.create(passengerRideRequestService
        .updateStatus("1", null, null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();

    verifyZeroInteractions(passengerRideRequestRepo);
  }

  @Test
  @DisplayName("Trying to update status of non-existing ride request returns exception")
  void updateNonExistentPassengerRequestStatus() {
    when(passengerRideRequestRepo
        .updateStatus("No such ride", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .thenReturn(Mono.empty());

    StepVerifier.create(passengerRideRequestService
        .updateStatus("No such ride", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .verifyComplete();

    verify(passengerRideRequestRepo, times(1))
        .updateStatus("No such ride", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
  }


  @Test
  @DisplayName("Verifying that updateStatus(...) service method invokes the one from repo")
  void updatePassengerRequestWithExistingId() {
    PassengerRideRequest expectedRequest = new PassengerRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.MATCHED);
    when(passengerRideRequestRepo
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .thenReturn(Mono.just(expectedRequest));
    PassengerRideRequest updatedRequest = passengerRideRequestService
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED).block();

    assertEquals(expectedRequest.getId(), updatedRequest.getId());
    assertEquals(expectedRequest.getUserId(), updatedRequest.getUserId());
    assertEquals(expectedRequest.getStatus(), updatedRequest.getStatus());

    verify(passengerRideRequestRepo, times(1))
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED);
  }

  @Test
  @DisplayName("Find all rideRequests by passengerId")
  void findAvailablePassengerRideRequest() {
    PassengerRideRequest requestOne = new PassengerRideRequest("1", "sameUser",
        null, null, null, null);
    PassengerRideRequest requestTwo = new PassengerRideRequest("2", "sameUser",
        null, null, null, null);
    PassengerRideRequest requestThree = new PassengerRideRequest("3", "sameUser",
        null, null, null, null);

    Flux<PassengerRideRequest> flux = Flux.just(requestOne, requestTwo, requestThree);

    when(passengerRideRequestRepo.findUserRequestByStatus(any(), any())).thenReturn(flux);

    StepVerifier.create(passengerRideRequestService
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
  @DisplayName("Receive Exception if given passengerId is null")
  void failedPassengerRideRequest() {
    assertThrows(InvalidRequestParameterException.class,
        () -> passengerRideRequestService
            .findAvailableRequestsByUserId(null, new RideRequestCriteria("available")));
  }

  @Test
  @DisplayName("Successful cancellation of the ride request")
  void cancelRequestSuccessfully() {
    PassengerRideRequest beforeCancelRequest = new PassengerRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.AVAILABLE);

    PassengerRideRequest afterCancelRequest = new PassengerRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.CANCELED);

    when(passengerRideRequestRepo
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .thenReturn(Mono.just(afterCancelRequest));

    StepVerifier.create(passengerRideRequestService.cancelRequest(beforeCancelRequest.getId()))
        .assertNext(
            updatedRequest -> assertEquals(RideRequestStatus.CANCELED, updatedRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Failed cancellation of the ride request with non matching status")
  void cancelRequestFailure() {
    PassengerRideRequest beforeCancelRequest = new PassengerRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.MATCHED);

    when(passengerRideRequestRepo
        .updateStatus("1", RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .thenReturn(Mono.empty());

    StepVerifier.create(passengerRideRequestService.cancelRequest(beforeCancelRequest.getId()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Failed cancellation of the ride request with null id")
  void cancelRequestFailed() {
    StepVerifier.create(passengerRideRequestService.cancelRequest(null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }
}
