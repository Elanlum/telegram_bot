package com.elanlum.ecs.ride.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.ride.crud.repository.impl.PassengerRideRequestRepo;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainer;
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
@SpringBootTest(classes = IntegrationTestsConfig.class)
class DriverPassengerMatchingServiceIntegrationTest {

  @Autowired
  DriverPassengerMatchingService driverPassengerMatchingService;
  @Autowired
  PassengerRideRequestRepo passengerRideRequestRepo;

  @Test
  void getDriverPassengerDistances() {
    Position driverPosition = new Position(59.906842f, 30.298719f);
    Position firstPassengerPosition = new Position(59.888854f, 30.322629f);
    Position secondPassengerPosition = new Position(59.891565f, 30.318767f);
    Position thirdPassengerPosition = new Position(59.864789f, 30.318935f);

    PassengerRideRequest prr1 = new PassengerRideRequest("1", "1", null,
        firstPassengerPosition, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest prr2 = new PassengerRideRequest("2", "2", null,
        secondPassengerPosition, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest prr3 = new PassengerRideRequest("3", "3", null,
        thirdPassengerPosition, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest drr = new DriverRideRequest("4", "4", null,
        driverPosition, null, RideRequestStatus.AVAILABLE);

    Flux<ScoringContainer> containers = driverPassengerMatchingService.getDriverPassengerPairs(
        Mono.just(drr),
        Flux.just(prr1, prr2, prr3)
    );

    StepVerifier.create(containers)
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "1"))
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "2"))
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "3"))
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("GetNearPassengers test")
  public void getNearPassengersTest() {
    Position driverPosition = new Position(59.833562f, 30.347907f);  //zvyozdnaya metro
    Position passengerPositionOne = new Position(59.835168f,
        30.345053f); //lensoveta  ~250m car by yandex rank-1
    Position passengerPositionTwo = new Position(59.833040f,
        30.367463f);  //dunaysky ~1900m car by yandex rank-4
    Position passengerPositionThree = new Position(59.839002f,
        30.338645f); //gagarina ~830m  car by yandex rank-2
    Position passengerPositionFour = new Position(59.840675f,
        30.334679f); //zvyozdnaya ul ~1100m car by yandex  rank-3

    PassengerRideRequest passRideRequestOne = new PassengerRideRequest("1", "2", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        passengerPositionOne, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passRideRequestTwo = new PassengerRideRequest("2", "3", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        passengerPositionTwo, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passRideRequestThree = new PassengerRideRequest("3", "4", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        passengerPositionThree, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passRideRequestFour = new PassengerRideRequest("4", "5", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        passengerPositionFour, null, RideRequestStatus.AVAILABLE);

    final DriverRideRequest driverRideRequest = new DriverRideRequest("1", "1", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)),
        driverPosition, null, RideRequestStatus.AVAILABLE);

    Mono<PassengerRideRequest> save = passengerRideRequestRepo.save(passRideRequestOne);
    save.block().getId();
    Mono<PassengerRideRequest> save1 = passengerRideRequestRepo.save(passRideRequestTwo);
    save1.block().getId();
    Mono<PassengerRideRequest> save2 = passengerRideRequestRepo.save(passRideRequestThree);
    save2.block().getId();
    Mono<PassengerRideRequest> save3 = passengerRideRequestRepo.save(passRideRequestFour);
    save3.block().getId();

    Mono<DriverRideRequest> driverRideRequestMono = Mono.just(driverRideRequest);

    Flux<ScoringContainer> nearPassengers = driverPassengerMatchingService
        .getNearPassengers(driverRideRequestMono);

    StepVerifier.create(nearPassengers)
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "1"))
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "3"))
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "4"))
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("User can not match himself test")
  public void givenDriverAndPassengerHaveTheSameId_theyDontMatch() {
    Position driverPassengerDeparture = new Position(59.833562f, 30.347907f);  //zvyozdnaya metro

    PassengerRideRequest passRideRequest = new PassengerRideRequest("1", "1", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        driverPassengerDeparture, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest = new DriverRideRequest("1", "1", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(10)),
        driverPassengerDeparture, null, RideRequestStatus.AVAILABLE);

    Mono<PassengerRideRequest> saved = passengerRideRequestRepo.save(passRideRequest);
    saved.block();
    Mono<DriverRideRequest> driverRideRequestMono = Mono.just(driverRideRequest);

    Flux<ScoringContainer> nearPassengers = driverPassengerMatchingService
        .getNearPassengers(driverRideRequestMono);

    StepVerifier.create(nearPassengers)
        .expectComplete()
        .verify();
  }

  @Test
  @DisplayName("GetNearPassengers test < N")
  public void getNearPassengersTest_whenFewReqs() {
    Position driverPosition = new Position(59.833562f, 30.347907f);  //zvyozdnaya metro
    Position passengerPositionOne = new Position(59.835168f,
        30.345053f); //lensoveta  ~250m car yandex rank-1
    Position passengerPositionTwo = new Position(59.833040f,
        30.367463f);  //dunaysky ~1900m car yadex rank-2

    PassengerRideRequest passRideRequestOne = new PassengerRideRequest("1", "2", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        passengerPositionOne, null, RideRequestStatus.AVAILABLE);
    PassengerRideRequest passRideRequestTwo = new PassengerRideRequest("2", "3", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        passengerPositionTwo, null, RideRequestStatus.AVAILABLE);

    final DriverRideRequest driverRideRequest = new DriverRideRequest("1", "1", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        driverPosition, null, RideRequestStatus.AVAILABLE);

    Mono<PassengerRideRequest> save = passengerRideRequestRepo.save(passRideRequestOne);
    save.block().getId();
    Mono<PassengerRideRequest> save1 = passengerRideRequestRepo.save(passRideRequestTwo);
    save1.block().getId();

    Mono<DriverRideRequest> driverRideRequestMono = Mono.just(driverRideRequest);

    Flux<ScoringContainer> nearPassengers = driverPassengerMatchingService
        .getNearPassengers(driverRideRequestMono);

    StepVerifier.create(nearPassengers)
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "1"))
        .assertNext(
            scoringContainer -> assertEquals(scoringContainer.getPassengerRequestId(), "2"))
        .expectComplete()
        .verify();
  }
}
