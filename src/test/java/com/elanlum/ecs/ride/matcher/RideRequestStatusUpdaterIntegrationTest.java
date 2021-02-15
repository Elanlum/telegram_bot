package com.elanlum.ecs.ride.matcher;

import static com.elanlum.ecs.ride.model.values.RideRequestStatus.AVAILABLE;
import static com.elanlum.ecs.ride.model.values.RideRequestStatus.MATCHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IntegrationTestsConfig.class)
class RideRequestStatusUpdaterIntegrationTest {

  @Autowired
  private RideRequestStatusUpdater statusUpdater;

  @Autowired
  private UserService userService;

  @Autowired
  private DriverRideRequestService driverRideRequestService;

  @Autowired
  private PassengerRideRequestService passengerRideRequestService;

  private Random random = new Random(System.currentTimeMillis());

  @MethodSource("updateStatusesData")
  @ParameterizedTest(name = "{0}")
  void updateRideRequestsStatuses(
      String testName,
      RideRequestStatus initialDriverStatus,
      RideRequestStatus initialPassengerStatus,
      boolean expectedResult,
      RideRequestStatus finalDriverStatus,
      RideRequestStatus finalPassengerStatus) {
    User passenger = userService.save(randomUser()).block();
    User driver = userService.save(randomUser()).block();

    var passengerRequest = passengerRideRequestService.save(new PassengerRideRequest(
        null,
        passenger.getId(),
        interval(),
        position(),
        position(),
        initialPassengerStatus
    )).block();

    var driverRequest = driverRideRequestService.save(new DriverRideRequest(
        null,
        driver.getId(),
        interval(),
        position(),
        position(),
        initialDriverStatus
    )).block();

    Mono<Boolean> updateMono
        = statusUpdater.updateStatusesToMatched(driverRequest.getId(), passengerRequest.getId());

    StepVerifier.create(updateMono)
        .expectNext(expectedResult)
        .verifyComplete();

    StepVerifier.create(passengerRideRequestService.findById(passengerRequest.getId()))
        .assertNext(request -> assertThat(request.getStatus()).isEqualTo(finalPassengerStatus))
        .verifyComplete();

    StepVerifier.create(driverRideRequestService.findById(driverRequest.getId()))
        .assertNext(request -> assertThat(request.getStatus()).isEqualTo(finalDriverStatus))
        .verifyComplete();
  }

  private static Stream<Arguments> updateStatusesData() {
    return Stream.of(
        arguments("both updated successfully", AVAILABLE, AVAILABLE, true, MATCHED, MATCHED),
        arguments("driver updated, rolled back", AVAILABLE, MATCHED, false, AVAILABLE, MATCHED),
        arguments("passenger updated, rolled back", MATCHED, AVAILABLE, false, MATCHED, AVAILABLE),
        arguments("both were not updated", MATCHED, MATCHED, false, MATCHED, MATCHED)
    );
  }

  private Position position() {
    return new Position(59.833562f, 30.347907f);
  }

  private Interval interval() {
    return new Interval(LocalDateTime.now().plusMinutes(30), LocalDateTime.now().plusHours(1));
  }

  private User randomUser() {
    return new User(
        null,
        randomString(),
        randomString(),
        null,
        null
    );
  }

  private String randomString() {
    return String.valueOf(random.nextInt(10000));
  }
}
