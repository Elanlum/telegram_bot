package com.elanlum.ecs.ride.matcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.service.impl.PassengerRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainer;
import com.elanlum.ecs.ride.matcher.scoring.ScoringContainerFactory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
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
class DriverPassengerMatchingServiceUnitTest {

  @Mock
  ScoringContainerFactory containerFactory;
  @Mock
  PassengerRideRequestService passengerRideRequestService;
  @InjectMocks
  DriverPassengerMatchingService driverPassengerMatchingService;

  @Test
  void whenGetDriverPassengerDistances_thenReturnFluxWithDistances() {
    Position driverPosition = new Position(0.0f, 0.0f);
    Position firstPassengerPosition = new Position(1.0f, 1.0f);
    Position secondPassengerPosition = new Position(2.0f, 2.0f);
    Position thirdPassengerPosition = new Position(3.0f, 3.0f);

    PassengerRideRequest passengerRequest1 = passengerRequest(firstPassengerPosition, "1");
    PassengerRideRequest passengerRequest2 = passengerRequest(secondPassengerPosition, "2");
    PassengerRideRequest passengerRequest3 = passengerRequest(thirdPassengerPosition, "3");
    DriverRideRequest driverRequest = new DriverRideRequest("4", "4", null,
        driverPosition, null, RideRequestStatus.AVAILABLE);

    when(containerFactory.create(any(), any())).thenAnswer(invocation -> {
      ScoringContainer container = mock(ScoringContainer.class);
      PassengerRideRequest passengerRideRequest = invocation.getArgument(1);
      when(container.getPassengerRequestId()).thenReturn(passengerRideRequest.getId());
      return container;
    });

    Flux<PassengerRideRequest> passengerRides = Flux
        .just(passengerRequest1, passengerRequest2, passengerRequest3);
    Mono<DriverRideRequest> driverRide = Mono.just(driverRequest);
    Flux<ScoringContainer> containers = driverPassengerMatchingService
        .getDriverPassengerPairs(driverRide, passengerRides);

    StepVerifier.create(containers)
        .assertNext(scoringContainer ->
            assertThat(scoringContainer.getPassengerRequestId()).isEqualTo("1"))
        .assertNext(scoringContainer ->
            assertThat(scoringContainer.getPassengerRequestId()).isEqualTo("2"))
        .assertNext(scoringContainer ->
            assertThat(scoringContainer.getPassengerRequestId()).isEqualTo("3"))
        .expectComplete()
        .verify();

    verify(containerFactory).create(driverRequest, passengerRequest1);
    verify(containerFactory).create(driverRequest, passengerRequest2);
    verify(containerFactory).create(driverRequest, passengerRequest3);
  }

  private PassengerRideRequest passengerRequest(Position position, String id) {
    return new PassengerRideRequest(id, id, null, position, null, RideRequestStatus.AVAILABLE);
  }

  @Test
  @DisplayName("GetNearPassengers test")
  public void whenDriverRequestMono_returnFluxOfScoringContainers() {

    Double d1 = 0.5;

    Position firstPassengerPosition = new Position(1.0f, 1.0f);
    Position secondPassengerPosition = new Position(2.0f, 2.0f);
    Position thirdPassengerPosition = new Position(3.0f, 3.0f);
    Position forthPassengerPosition = new Position(4.0f, 4.0f);
    Position driverPosition = new Position(0.0f, 0.0f);

    PassengerRideRequest passengerRequest1 = passengerRequest(firstPassengerPosition, "1");
    PassengerRideRequest passengerRequest2 = passengerRequest(secondPassengerPosition, "2");
    PassengerRideRequest passengerRequest3 = passengerRequest(thirdPassengerPosition, "3");
    PassengerRideRequest passengerRequest4 = passengerRequest(forthPassengerPosition, "4");

    when(containerFactory.create(any(), any())).thenAnswer(invocation -> {
      ScoringContainer container = mock(ScoringContainer.class);
      PassengerRideRequest passengerRideRequest = invocation.getArgument(1);
      when(container.getScore())
          .thenReturn(d1 + (Double.parseDouble(passengerRideRequest.getId())) / 10);
      return container;
    });

    final DriverRideRequest driverRideRequest = new DriverRideRequest("1", "1", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        driverPosition, null, RideRequestStatus.AVAILABLE);

    Flux<PassengerRideRequest> passRideReqFlux = Flux
        .just(passengerRequest1, passengerRequest2, passengerRequest3, passengerRequest4);

    when(passengerRideRequestService
        .getAvailablePassengerRequestsInTime(eq(driverRideRequest.getRideDate().getStart()),
            eq(driverRideRequest.getRideDate().getEnd()), eq(driverRideRequest.getUserId())))
        .thenReturn(passRideReqFlux);

    Flux<ScoringContainer> fluxThree = driverPassengerMatchingService
        .getNearPassengers(Mono.just(driverRideRequest));

    StepVerifier.create(fluxThree)
        .assertNext(scoringContainer -> assertThat(scoringContainer.getScore()).isEqualTo(0.9))
        .assertNext(scoringContainer -> assertThat(scoringContainer.getScore()).isEqualTo(0.8))
        .assertNext(scoringContainer -> assertThat(scoringContainer.getScore()).isEqualTo(0.7))
        .verifyComplete();

    verify(passengerRideRequestService, times(1))
        .getAvailablePassengerRequestsInTime(any(), any(), any());
    verifyNoMoreInteractions(passengerRideRequestService);
    verify(containerFactory).create(driverRideRequest, passengerRequest1);
    verify(containerFactory).create(driverRideRequest, passengerRequest2);
    verify(containerFactory).create(driverRideRequest, passengerRequest3);
    verify(containerFactory).create(driverRideRequest, passengerRequest4);
  }

  @Test
  @DisplayName("GetNearPassengers test < N ")
  public void whenDriverRequestMonoAndFewPassReqs_returnFluxOfScoringContainers() {

    Double d1 = 0.5;

    Position firstPassengerPosition = new Position(1.0f, 1.0f);
    Position secondPassengerPosition = new Position(2.0f, 2.0f);
    Position driverPosition = new Position(0.0f, 0.0f);

    PassengerRideRequest passengerRequest1 = passengerRequest(firstPassengerPosition, "1");
    PassengerRideRequest passengerRequest2 = passengerRequest(secondPassengerPosition, "2");

    when(containerFactory.create(any(), any())).thenAnswer(invocation -> {
      ScoringContainer container = mock(ScoringContainer.class);
      PassengerRideRequest passengerRideRequest = invocation.getArgument(1);
      when(container.getScore())
          .thenReturn(d1 + (Double.parseDouble(passengerRideRequest.getId())) / 10);
      return container;
    });

    Flux<PassengerRideRequest> passRideReqFlux = Flux
        .just(passengerRequest1, passengerRequest2);

    final DriverRideRequest driverRideRequest = new DriverRideRequest("1", "1", new Interval(
        LocalDateTime.now(), LocalDateTime.now().plusMinutes(20)),
        driverPosition, null, RideRequestStatus.AVAILABLE);

    when(passengerRideRequestService
        .getAvailablePassengerRequestsInTime(eq(driverRideRequest.getRideDate().getStart()),
            eq(driverRideRequest.getRideDate().getEnd()), eq(driverRideRequest.getUserId())))
        .thenReturn(passRideReqFlux);

    Flux<ScoringContainer> fluxTwo = driverPassengerMatchingService
        .getNearPassengers(Mono.just(driverRideRequest));

    StepVerifier.create(fluxTwo)
        .assertNext(scoringContainer -> assertThat(scoringContainer.getScore()).isEqualTo(0.7))
        .assertNext(scoringContainer -> assertThat(scoringContainer.getScore()).isEqualTo(0.6))
        .verifyComplete();

    verify(passengerRideRequestService, times(1))
        .getAvailablePassengerRequestsInTime(any(), any(), any());
    verifyNoMoreInteractions(passengerRideRequestService);
    verify(containerFactory).create(driverRideRequest, passengerRequest1);
    verify(containerFactory).create(driverRideRequest, passengerRequest2);
  }

  @Test
  @DisplayName("Merging method test")
  public void whenListsApplied_ReturnBinaryOperator() {

    ScoringContainer scoringContainerOne = mock(ScoringContainer.class);
    ScoringContainer scoringContainerTwo = mock(ScoringContainer.class);
    ScoringContainer scoringContainerThree = mock(ScoringContainer.class);
    ScoringContainer scoringContainerFour = mock(ScoringContainer.class);

    Double d1 = 0.5;
    Double d2 = 0.2;
    Double d3 = 0.7;
    Double d4 = 0.6;

    doReturn(d1).when(scoringContainerOne).getScore();
    doReturn(d2).when(scoringContainerTwo).getScore();
    doReturn(d3).when(scoringContainerThree).getScore();
    doReturn(d4).when(scoringContainerFour).getScore();

    List<ScoringContainer> list = new ArrayList<>();
    List<ScoringContainer> listTwo = new ArrayList<>();

    list.add(scoringContainerOne);
    list.add(scoringContainerTwo);
    listTwo.add(scoringContainerThree);
    listTwo.add(scoringContainerFour);

    BinaryOperator<List<ScoringContainer>> listBinaryOperator = driverPassengerMatchingService
        .getListBinaryOperator();
    List<ScoringContainer> apply = listBinaryOperator.apply(list, listTwo);
    assertEquals(0.7, apply.get(0).getScore());
    assertEquals(0.2, apply.get(3).getScore());
  }
}
