package com.elanlum.ecs.ride.crud.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.exceptions.InvalidRideRequestUpdatingException;
import com.elanlum.ecs.ride.exceptions.UserFromRideRequestDoesNotExist;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.Role;

import java.time.LocalDateTime;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class DriverRideRequestServiceIntegrationTest {

  @Autowired
  private DriverRideRequestService driverRideRequestService;
  @Autowired
  private UserService userService;

  @Test
  @DisplayName("Check invalid driver ride request information.")
  void checkInvalidDriverRideRequestArguments() {
    String userId = userService.save(new User(
        null, "testUser", "Dummy", "2", null))
        .block()
        .getId();

    DriverRideRequest driverRideRequest = new DriverRideRequest("1", userId,
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  @DisplayName("Check valid driver ride request information.")
  void checkValidDriverRideRequestArguments() {
    String userId = userService.save(new User(
        null, "testUser", "Dummy", "2", null))
        .block()
        .getId();

    DriverRideRequest driverRideRequest = new DriverRideRequest(null, userId,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectNextMatches((request) -> {
          assertEquals(userId, request.getUserId());
          return true;
        })
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Failed saving a driver ride request with non existent user id.")
  void saveRideRequestWithNonexistentUserId() {
    DriverRideRequest driverRideRequest = new DriverRideRequest(
        null, "1",
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectError(UserFromRideRequestDoesNotExist.class)
        .verify();
  }

  @Test
  @DisplayName("Save a driver ride request.")
  void save() {
    User user = new User(null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest driverRideRequest = new DriverRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    StepVerifier.create(driverRideRequestService.save(driverRideRequest))
        .expectNextMatches(request -> {
          assertEquals(request.getUserId(), user.getId());
          assertEquals(request.getRideDate().getEnd().getMinute(),
              driverRideRequest.getRideDate().getEnd().getMinute());
          assertEquals(request.getRole(), Role.DRIVER);
          assertNotNull(request.getDeparturePoint());
          assertNotNull(request.getDestinationPoint());
          return true;
        })
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Find a driver ride request by its id.")
  void findById() {
    User user = new User(
        null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest driverRideRequest = new DriverRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    Mono<DriverRideRequest> save = driverRideRequestService.save(driverRideRequest);
    DriverRideRequest savedDriverRequest = save.block();
    assertNotNull(savedDriverRequest);

    Mono<DriverRideRequest> driverRideRequestMono = driverRideRequestService
        .findById(savedDriverRequest.getId());
    DriverRideRequest testDriverRideRequest = driverRideRequestMono.block();
    assertEquals(savedDriverRequest.getId(), testDriverRideRequest.getId());
    assertEquals(savedDriverRequest.getUserId(), testDriverRideRequest.getUserId());
    assertEquals(savedDriverRequest.getRideDate().getEnd().getMinute(),
        testDriverRideRequest.getRideDate().getEnd().getMinute());
  }

  @Test
  @DisplayName("Find a driver ride request by given user id.")
  void findRideRequestByUserId() {
    User user = new User(
        null, "testUser", "Dummy", "2", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest driverRideRequest = new DriverRideRequest(
        null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1),
        RideRequestStatus.AVAILABLE);

    Mono<DriverRideRequest> savedRide = driverRideRequestService.save(driverRideRequest);
    DriverRideRequest testRide = savedRide.block();
    assertNotNull(testRide);

    Flux<DriverRideRequest> foundRide = driverRideRequestService.findByUserId(savedUser.getId());
    assertEquals(foundRide.blockFirst().getId(), testRide.getId());
    assertEquals(foundRide.blockFirst().getUserId(), testRide.getUserId());
    assertEquals(foundRide.blockFirst().getRideDate().getEnd().getMinute(),
        testRide.getRideDate().getEnd().getMinute());
  }

  @Test
  @DisplayName("Get all available driver requests")
  public void findAllReqsWithAvailableStatus() {

    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    User userSecond = new User(null, "testUserNew", "Ivan", "2", null);
    Mono<User> savedMonoUserSecond = userService.save(userSecond);
    User savedUserSecond = savedMonoUserSecond.block();

    DriverRideRequest dvrRideReqOne = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);
    DriverRideRequest dvrRideReqTwo = new DriverRideRequest(null, savedUserSecond.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(2, 2), new Position(5, 5), RideRequestStatus.MATCHED);
    DriverRideRequest dvrRideReqThree = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(120L)),
        new Position(3, 3), new Position(6, 6), RideRequestStatus.AVAILABLE);

    Mono<DriverRideRequest> savedReqOne = driverRideRequestService.save(dvrRideReqOne);
    savedReqOne.block();
    Mono<DriverRideRequest> savedReqTwo = driverRideRequestService.save(dvrRideReqTwo);
    savedReqTwo.block();
    Mono<DriverRideRequest> savedReqThree = driverRideRequestService.save(dvrRideReqThree);
    savedReqThree.block();

    Flux<DriverRideRequest> availableDriverRequests = driverRideRequestService
        .getAvailableRequests();

    StepVerifier.create(availableDriverRequests)
        .assertNext(
            driverRequestDb -> assertEquals(dvrRideReqOne.getStatus(), driverRequestDb.getStatus()))
        .assertNext(driverRequestDb -> assertEquals(dvrRideReqThree.getStatus(),
            driverRequestDb.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Find all rideRequests by driverId")
  void findAvailableDriverRideRequest() {
    Mono<User> savedDriver = userService
        .save(new User(null, "login", "name", "telegramId", null));
    String id = savedDriver.block().getId();
    DriverRideRequest requestOne = new DriverRideRequest(null, id,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(20)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.MATCHED);
    DriverRideRequest requestTwo = new DriverRideRequest(null, id,
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(30)),
        new Position(0, 0),
        new Position(1, 1), RideRequestStatus.AVAILABLE);

    DriverRideRequest savedRequest = driverRideRequestService.save(requestOne).block();
    driverRideRequestService.save(requestTwo).block();
    assertEquals(savedRequest.getUserId(), id);

    StepVerifier.create(driverRideRequestService
        .findAvailableRequestsByUserId(id, new RideRequestCriteria("available")))
        .assertNext(request -> assertEquals(request.getRideDate().getEnd().getMinute(),
            requestTwo.getRideDate().getEnd().getMinute()))
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("Update available driver ride request status with valid request id and status.")
  void updateAvailableDriverRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest drr1 = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);

    DriverRideRequest savedRequest = driverRideRequestService.save(drr1).block();

    StepVerifier.create(driverRideRequestService
        .updateStatus(savedRequest.getId(), RideRequestStatus.AVAILABLE,
            RideRequestStatus.CANCELED))
        .assertNext(driverRideRequest -> assertEquals(RideRequestStatus.CANCELED,
            driverRideRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Update matched driver ride request status with valid request id and status.")
  void updateMatchedDriverRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest drr1 = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.MATCHED);

    DriverRideRequest savedRequest = driverRideRequestService.save(drr1).block();

    StepVerifier.create(driverRideRequestService
        .updateStatus(savedRequest.getId(), RideRequestStatus.MATCHED, RideRequestStatus.CANCELED))
        .assertNext(driverRideRequest -> assertEquals(RideRequestStatus.CANCELED,
            driverRideRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Update driver ride request status with valid status and null request id.")
  void updateDriverRideRequestStatusWithNullId() {
    StepVerifier
        .create(driverRideRequestService
            .updateStatus(null, RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Update driver ride request status with valid status and nonexistent request id.")
  void updateDriverRideRequestStatusWithNonexistentId() {
    StepVerifier
        .create(driverRideRequestService
            .updateStatus("pip", RideRequestStatus.AVAILABLE, RideRequestStatus.CANCELED))
        .verifyComplete();
  }

  @Test
  @DisplayName("Update driver ride request status with valid request id and null status.")
  void updateDriverRideRequestStatusWithNullStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest drr1 = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);

    DriverRideRequest savedRequest = driverRideRequestService.save(drr1).block();

    StepVerifier.create(driverRideRequestService.updateStatus(savedRequest.getId(), null, null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Cancel available driver ride request with valid request id.")
  void cancelAvailableDriverRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest drr1 = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.AVAILABLE);

    DriverRideRequest savedRequest = driverRideRequestService.save(drr1).block();

    StepVerifier.create(driverRideRequestService
        .cancelRequest(savedRequest.getId()))
        .assertNext(driverRideRequest -> assertEquals(RideRequestStatus.CANCELED,
            driverRideRequest.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Cancel matched driver ride request with valid request id.")
  void cancelMatchedDriverRideRequestStatus() {
    User user = new User(null, "testUser", "John", "1", null);
    Mono<User> savedMonoUser = userService.save(user);
    User savedUser = savedMonoUser.block();

    DriverRideRequest drr1 = new DriverRideRequest(null, savedUser.getId(),
        new Interval(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusMinutes(15)),
        new Position(1, 1), new Position(4, 4), RideRequestStatus.MATCHED);

    DriverRideRequest savedRequest = driverRideRequestService.save(drr1).block();

    StepVerifier.create(driverRideRequestService
        .cancelRequest(savedRequest.getId()))
        .verifyComplete();
  }

  @Test
  @DisplayName("Cancel driver ride request with null request id.")
  void cancelAvailableDriverRideRequestStatusWithNullId() {
    StepVerifier.create(driverRideRequestService
        .cancelRequest(null))
        .expectError(InvalidRideRequestUpdatingException.class)
        .verify();
  }

  @Test
  @DisplayName("Cancel driver ride request with invalid request id.")
  void cancelAvailableDriverRideRequestStatusWithInvalidId() {
    StepVerifier.create(driverRideRequestService
        .cancelRequest("pip"))
        .verifyComplete();
  }
}
