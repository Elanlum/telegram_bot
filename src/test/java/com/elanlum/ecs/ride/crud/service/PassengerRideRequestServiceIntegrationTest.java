package com.elanlum.ecs.ride.crud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.repository.impl.PassengerRideRequestRepo;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.exceptions.InvalidRideRequestUpdatingException;
import com.elanlum.ecs.ride.exceptions.UserFromRideRequestDoesNotExist;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.Role;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = IntegrationTestsConfig.class)
class PassengerRideRequestServiceIntegrationTest {

  @Autowired
  ReactiveMongoTemplate reactiveMongoTemplate;
  @Autowired
  private PassengerRideRequestService passengerRideRequestService;
  @Autowired
  private DriverRideRequestService driverRideRequestService;
  @Autowired
  private UserService userService;
  @Autowired
  private PassengerRideRequestRepo passengerRideRequestRepo;

  @Test
  @DisplayName("Check invalid passenger ride request information.")
  void checkInvalidPassengerRideRequestArguments() {
    String userId = userService.save(new User(
        null, "testUser", "Dummy", "2", null))
        .block()
        .getId();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest("1", userId,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  @DisplayName("Check valid passenger ride request information.")
  void checkValidPassengerRideRequestArguments() {
    String userId = userService.save(new User(
        null, "testUser", "Dummy", "2", null))
        .block()
        .getId();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null,
        userId,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectNextMatches(passenger -> {
          assertEquals(userId, passenger.getUserId());
          return true;
        })
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Trying to invoke updateStatus(...) method with null id returns exception")
  void updateRideRequestWithNullId() {
    StepVerifier.create(passengerRideRequestService
        .updateStatus(null, RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
    StepVerifier.create(driverRideRequestService
        .updateStatus(null, RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Invoking updateStatus(...) with null status argument returns exception")
  void updatePassengerRequestWithNullStatus() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    User savedUser = userService.save(user).block();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);
    PassengerRideRequest savedRequest =
        passengerRideRequestService.save(passengerRideRequest).block();

    StepVerifier.create(passengerRideRequestService
        .updateStatus(savedRequest.getId(), null, null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Trying to update status of non-existing ride request returns empty mono")
  void updateNonExistentPassengerRequestStatus() {
    StepVerifier.create(passengerRideRequestService.updateStatus(
        "No such ride", RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .verifyComplete();
  }

  @Test
  @DisplayName("Updating status of saved passenger ride request that has an \"AVAILABLE\" status")
  void updatePassengerRequestWithExistingId() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);
    PassengerRideRequest savedRequest =
        passengerRideRequestService.save(passengerRideRequest).block();

    Long countOfRequestsBeforeUpdate = passengerRideRequestRepo.getAll().count().block();

    StepVerifier.create(passengerRideRequestService.updateStatus(
        savedRequest.getId(), RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .expectNextMatches(updatedRequest -> {
          assertEquals(savedRequest.getId(), updatedRequest.getId());
          assertEquals(RideRequestStatus.MATCHED, updatedRequest.getStatus());
          assertEquals(savedRequest.getUserId(), updatedRequest.getUserId());
          return true;
        })
        .expectComplete()
        .verify();

    assertEquals(countOfRequestsBeforeUpdate, passengerRideRequestRepo.getAll().count().block());
  }

  @Test
  @DisplayName("Trying to set \"MATCHED\" status for ride request which is already matched")
  void updatePassengerRequestWithUnexpectedStatus() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.MATCHED);
    PassengerRideRequest savedRequest =
        passengerRideRequestService.save(passengerRideRequest).block();

    Long countOfRequestsBeforeUpdate = passengerRideRequestRepo.getAll().count().block();

    StepVerifier.create(passengerRideRequestService.updateStatus(
        savedRequest.getId(), RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED))
        .verifyComplete();

    assertEquals(countOfRequestsBeforeUpdate, passengerRideRequestRepo.getAll().count().block());
  }

  @Test
  @DisplayName("Failed saving a passenger ride request with non existent user id.")
  void saveRideRequestWithNonexistentUserId() {
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, "1",
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectError(UserFromRideRequestDoesNotExist.class)
        .verify();
  }

  @Test
  @DisplayName("Save a passenger ride request.")
  void save() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    StepVerifier.create(passengerRideRequestService.save(passengerRideRequest))
        .expectNextMatches(request -> {
          assertEquals(request.getUserId(), user.getId());
          assertEquals(request.getRideDate().getEnd(),
              passengerRideRequest.getRideDate().getEnd());
          assertEquals(request.getRole(), Role.PASSENGER);
          assertNotNull(request.getDeparturePoint());
          assertNotNull(request.getDestinationPoint());
          return true;
        })
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Find a passenger ride request by its id.")
  void findById() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    Mono<PassengerRideRequest> save = passengerRideRequestService.save(passengerRideRequest);
    PassengerRideRequest savedPassengerRequest = save.block();
    assertNotNull(savedPassengerRequest);

    Mono<PassengerRideRequest> passengerRideRequestMono = passengerRideRequestService
        .findById(savedPassengerRequest.getId());
    PassengerRideRequest testPassengerRideRequest = passengerRideRequestMono.block();
    assertEquals(savedPassengerRequest.getId(), testPassengerRideRequest.getId());
    assertEquals(savedPassengerRequest.getUserId(), testPassengerRideRequest.getUserId());
    assertEquals(savedPassengerRequest.getRideDate().getEnd().truncatedTo(ChronoUnit.MILLIS),
        testPassengerRideRequest.getRideDate().getEnd());
  }

  @Test
  @DisplayName("Find a passenger ride request by given user id.")
  void findRideRequestByUserId() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    Mono<PassengerRideRequest> savedRide = passengerRideRequestService.save(passengerRideRequest);
    PassengerRideRequest testRide = savedRide.block();
    assertNotNull(testRide);

    Flux<PassengerRideRequest> foundRide =
        passengerRideRequestService.findByUserId(savedUser.getId());
    assertEquals(foundRide.blockFirst().getId(), testRide.getId());
    assertEquals(foundRide.blockFirst().getUserId(), testRide.getUserId());
    assertEquals(foundRide.blockFirst().getRideDate().getEnd(),
        testRide.getRideDate().getEnd().truncatedTo(ChronoUnit.MILLIS));
  }

  @Test
  @DisplayName("Get all available passReqs")
  public void findAllReqsWithAvailableStatus() {

    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    User userSecond = new User(null, "testUserNew", "Ivan", "2", null);
    Mono<User> savedMonoUserSecond = userService.save(userSecond);
    User savedUserSecond = savedMonoUserSecond.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);
    PassengerRideRequest prr2 = new PassengerRideRequest(null, savedUserSecond.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(2, 2), new Position(5, 5), RideRequestStatus.MATCHED);
    PassengerRideRequest prr3 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(3, 3), new Position(6, 6), RideRequestStatus.AVAILABLE);

    Mono<PassengerRideRequest> savedReqOne = passengerRideRequestService.save(prr1);
    savedReqOne.block();
    Mono<PassengerRideRequest> savedReqTwo = passengerRideRequestService.save(prr2);
    savedReqTwo.block();
    Mono<PassengerRideRequest> savedReqThree = passengerRideRequestService.save(prr3);
    savedReqThree.block();

    Flux<PassengerRideRequest> availablePassengerRequests = passengerRideRequestService
        .getAvailableRequests();

    Long l = 2L;
    assertEquals(l, availablePassengerRequests.count().block());

    StepVerifier.create(availablePassengerRequests)
        .assertNext(
            passengerRideRequest -> assertEquals(RideRequestStatus.AVAILABLE,
                passengerRideRequest.getStatus()))
        .assertNext(
            passengerRideRequest -> assertEquals(RideRequestStatus.AVAILABLE,
                passengerRideRequest.getStatus()))
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Get all available passReqs with coincided time")
  public void findAllReqsWithAvailableStatusAndUseTime() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    User userSecond = new User(null, "testUserNew", "Ivan", "2", null);
    Mono<User> savedMonoUserSecond = userService.save(userSecond);
    User savedUserSecond = savedMonoUserSecond.block();

    User userThird = new User(null, "testUserNew2", "Mike", "3", null);
    Mono<User> savedMonoUserThird = userService.save(userThird);
    User savedUserThird = savedMonoUserThird.block();

    User userFourth = new User(null, "testUserNewNew", "Clark", "4", null);
    Mono<User> savedMonoUserFourth = userService.save(userFourth);
    User savedUserFourth = savedMonoUserFourth.block();

    User userFifth = new User(null, "testUserNewSuperNew", "Tony", "5", null);
    Mono<User> savedMonoUserFifth = userService.save(userFifth);
    User savedUserFifth = savedMonoUserFifth.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);
    PassengerRideRequest prr2 = new PassengerRideRequest(null, savedUserSecond.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(35)),
        new Position(2, 2), new Position(5, 5), RideRequestStatus.MATCHED);
    PassengerRideRequest prr3 = new PassengerRideRequest(null, savedUserThird.getId(),
        new Interval(LocalDateTime.now().plusMinutes(30), LocalDateTime.now().plusMinutes(45)),
        new Position(2, 2), new Position(5, 5), RideRequestStatus.AVAILABLE);
    PassengerRideRequest prr4 = new PassengerRideRequest(null, savedUserFourth.getId(),
        new Interval(LocalDateTime.now().plusMinutes(1), LocalDateTime.now().plusMinutes(3)),
        new Position(2, 2), new Position(5, 5), RideRequestStatus.AVAILABLE);
    PassengerRideRequest prr5 = new PassengerRideRequest(null, savedUserFifth.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(25)),
        new Position(2, 2), new Position(5, 5), RideRequestStatus.AVAILABLE);

    passengerRideRequestService.save(prr1).block();
    passengerRideRequestService.save(prr2).block();
    passengerRideRequestService.save(prr3).block();
    passengerRideRequestService.save(prr4).block();
    passengerRideRequestService.save(prr5).block();

    String userDriverId = userService.save(new User(
        null, "testUser", "Dummy", "2", null))
        .block()
        .getId();

    DriverRideRequest driverRideRequest = new DriverRideRequest("1", userDriverId,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    Flux<PassengerRideRequest> availablePassengerRequestsInTime = passengerRideRequestService
        .getAvailablePassengerRequestsInTime(driverRideRequest.getRideDate().getStart(),
            driverRideRequest.getRideDate().getEnd(), driverRideRequest.getUserId());

    StepVerifier.create(availablePassengerRequestsInTime)
        .assertNext(passengerRideRequest -> assertEquals(prr1.getUserId(),
            passengerRideRequest.getUserId()))
        .assertNext(passengerRideRequest -> assertEquals(prr5.getUserId(),
            passengerRideRequest.getUserId()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Find all rideRequests by passengerId")
  void findAvailablePassengerRideRequest() {
    Mono<User> savedPassenger = userService
        .save(new User(null, "login", "name", "telegramId", null));
    String id = savedPassenger.block().getId();
    PassengerRideRequest requestOne = new PassengerRideRequest(null, id,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);
    PassengerRideRequest requestTwo = new PassengerRideRequest(null, id,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);
    PassengerRideRequest requestThree = new PassengerRideRequest(null, id,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.MATCHED);

    PassengerRideRequest savedRequest = passengerRideRequestService.save(requestOne).block();
    passengerRideRequestService.save(requestTwo).block();
    passengerRideRequestService.save(requestThree).block();
    assertEquals(savedRequest.getUserId(), id);

    StepVerifier.create(passengerRideRequestService.findAvailableRequestsByUserId(id,
        new RideRequestCriteria("available")))
        .assertNext(request -> assertEquals(request.getRideDate().getEnd().getMinute(),
            requestOne.getRideDate().getEnd().getMinute()))
        .assertNext(request -> assertEquals(request.getRideDate().getEnd().getMinute(),
            requestTwo.getRideDate().getEnd().getMinute()))
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Update available passenger ride request status with valid request id and status.")
  void updateAvailablePassengerRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);

    PassengerRideRequest savedRequest = passengerRideRequestService.save(prr1).block();

    StepVerifier.create(passengerRideRequestService
        .updateStatus(savedRequest.getId(), RideRequestStatus.AVAILABLE,
            RideRequestStatus.CANCELED))
        .assertNext(passengerRideRequest -> assertEquals(RideRequestStatus.CANCELED,
            passengerRideRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Update matched passenger ride request status with valid request id and status.")
  void updateMatchedPassengerRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.MATCHED);

    PassengerRideRequest savedRequest = passengerRideRequestService.save(prr1).block();

    StepVerifier.create(passengerRideRequestService
        .updateStatus(savedRequest.getId(), RideRequestStatus.MATCHED, RideRequestStatus.CANCELED))
        .assertNext(passengerRideRequest -> assertEquals(RideRequestStatus.CANCELED,
            passengerRideRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Update passenger ride request status with valid status and null request id.")
  void updatePassengerRideRequestStatusWithNullId() {
    StepVerifier
        .create(passengerRideRequestService
            .updateStatus(null, RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Update passenger ride request status with valid status and nonexistent request id.")
  void updatePassengerRideRequestStatusWithNonexistentId() {
    StepVerifier
        .create(passengerRideRequestService
            .updateStatus("pip", RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .verifyComplete();
  }

  @Test
  @DisplayName("Update passenger ride request status with valid request id and null status.")
  void updatePassengerRideRequestStatusWithNullStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);

    PassengerRideRequest savedRequest = passengerRideRequestService.save(prr1).block();

    StepVerifier.create(passengerRideRequestService.updateStatus(savedRequest.getId(), null, null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Cancel available passenger ride request with valid request id.")
  void cancelAvailablePassengerRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);

    PassengerRideRequest savedRequest = passengerRideRequestService.save(prr1).block();

    StepVerifier.create(passengerRideRequestService
        .cancelRequest(savedRequest.getId()))
        .assertNext(passengerRideRequest -> assertEquals(RideRequestStatus.CANCELED,
            passengerRideRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Cancel matched passenger ride request with valid request id.")
  void cancelMatchedPassengerRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    PassengerRideRequest prr1 = new PassengerRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.MATCHED);

    PassengerRideRequest savedRequest = passengerRideRequestService.save(prr1).block();

    StepVerifier.create(passengerRideRequestService
        .cancelRequest(savedRequest.getId()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Cancel passenger ride request with null request id.")
  void cancelAvailablePassengerRideRequestStatusWithNullId() {
    StepVerifier.create(passengerRideRequestService
        .cancelRequest(null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Cancel passenger ride request with invalid request id.")
  void cancelAvailablePassengerRideRequestStatusWithInvalidId() {
    StepVerifier.create(passengerRideRequestService
        .cancelRequest("pip"))
        .verifyComplete();
  }


}