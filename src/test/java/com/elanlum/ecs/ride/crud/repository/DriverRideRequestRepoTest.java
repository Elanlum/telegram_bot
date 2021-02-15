package com.elanlum.ecs.ride.crud.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.repository.impl.DriverRideRequestRepo;
import com.elanlum.ecs.utils.TestCategory;
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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class DriverRideRequestRepoTest {

  @Mock
  private ReactiveMongoTemplate reactiveMongoTemplate;
  @InjectMocks
  private DriverRideRequestRepo driverRideRequestRepo;

  private DriverRideRequest driverRideRequest = new DriverRideRequest(
      "1", "1",
      new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
      new Position(0, 0),
      new Position(0, 0),
      RideRequestStatus.AVAILABLE);

  @Test
  void save() {
    when(reactiveMongoTemplate.save(driverRideRequest))
        .thenReturn(Mono.just(driverRideRequest));
    Mono<DriverRideRequest> mono = driverRideRequestRepo.save(driverRideRequest);
    DriverRideRequest testDriverRideRequest = mono.block();

    assertEquals(driverRideRequest.getId(), testDriverRideRequest.getId());
    verify(reactiveMongoTemplate, times(1)).save(driverRideRequest);
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  void findById() {
    when(reactiveMongoTemplate.findById("1", DriverRideRequest.class))
        .thenReturn(Mono.just(driverRideRequest));
    Mono<DriverRideRequest> mono = driverRideRequestRepo.findById("1");
    DriverRideRequest testDriverRideRequest = mono.block();

    assertEquals("1", testDriverRideRequest.getId());
    verify(reactiveMongoTemplate, times(1)).findById("1", DriverRideRequest.class);
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  void givenUserId_findRideEntity() {
    when(reactiveMongoTemplate
        .find(Query.query(Criteria.where("userId").is("1")), DriverRideRequest.class)).thenReturn(
        Flux.just(driverRideRequest));
    Flux<DriverRideRequest> flux = driverRideRequestRepo.findByUserId("1");
    DriverRideRequest testDriverRideRequest = flux.blockFirst();

    assertEquals("1", testDriverRideRequest.getUserId());
    verify(reactiveMongoTemplate, times(1)).find(any(Query.class), eq(DriverRideRequest.class));
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  @DisplayName("Get available driver requests")
  void findAllReqsWithAvailableStatus() {
    when(reactiveMongoTemplate.find(Query.query(Criteria.where("status").is(
        RideRequestStatus.AVAILABLE)), DriverRideRequest.class))
        .thenReturn(Flux.just(driverRideRequest));
    StepVerifier.create(driverRideRequestRepo.getAvailableRequests())
        .expectNext(driverRideRequest).verifyComplete();
    verify(reactiveMongoTemplate, times(1)).find(any(), any());
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  @DisplayName("Get multiple driverRideRequests from Repo")
  void findAvailableDriverRideRequest() {
    DriverRideRequest requestOne = new DriverRideRequest("1", "sameUser",
        null, null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest requestTwo = new DriverRideRequest("2", "sameUser",
        null, null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest requestThree = new DriverRideRequest("3", "sameUser",
        null, null, null, RideRequestStatus.AVAILABLE);

    Flux<DriverRideRequest> flux = Flux.just(requestOne, requestTwo, requestThree);

    Query query = Query.query(Criteria.where("userId").is("sameUser"))
        .addCriteria(Criteria.where("status").is(RideRequestStatus.AVAILABLE));

    when(reactiveMongoTemplate.find(query, DriverRideRequest.class)).thenReturn(flux);

    StepVerifier.create(
        driverRideRequestRepo.findUserRequestByStatus("sameUser", RideRequestStatus.AVAILABLE))
        .assertNext(response -> {
          assertEquals(response.getId(), "1");
          assertEquals(response.getUserId(), "sameUser");
        })
        .assertNext(response -> {
          assertEquals(response.getId(), "2");
          assertEquals(response.getUserId(), "sameUser");
        })
        .assertNext(response -> {
          assertEquals(response.getId(), "3");
          assertEquals(response.getUserId(), "sameUser");
        })
        .expectComplete()
        .verify();

    verify(reactiveMongoTemplate, times(1)).find(any(), any());
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }
}
