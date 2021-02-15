package com.elanlum.ecs.ride.crud.repository;

import static com.elanlum.ecs.ride.model.values.RideRequestStatus.AVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.repository.impl.PassengerRideRequestRepo;
import com.elanlum.ecs.utils.TestCategory;
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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class PassengerRideRequestRepoTest {

  @Mock
  private ReactiveMongoTemplate reactiveMongoTemplate;
  @InjectMocks
  private PassengerRideRequestRepo passengerRideRequestRepo;
  private PassengerRideRequest passengerRideRequest = new PassengerRideRequest(
      "1", "1",
      new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
      new Position(0, 0),
      new Position(0, 0), RideRequestStatus.AVAILABLE);

  @Test
  void save() {
    when(reactiveMongoTemplate.save(passengerRideRequest))
        .thenReturn(Mono.just(passengerRideRequest));
    Mono<PassengerRideRequest> mono = passengerRideRequestRepo.save(passengerRideRequest);
    PassengerRideRequest testPassengerRideRequest = mono.block();

    assertEquals(passengerRideRequest.getId(), testPassengerRideRequest.getId());
    verify(reactiveMongoTemplate, times(1)).save(passengerRideRequest);
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  void findById() {
    when(reactiveMongoTemplate.findById("1", PassengerRideRequest.class))
        .thenReturn(Mono.just(passengerRideRequest));
    Mono<PassengerRideRequest> mono = passengerRideRequestRepo.findById("1");
    PassengerRideRequest testPassengerRideRequest = mono.block();

    assertEquals("1", testPassengerRideRequest.getId());
    verify(reactiveMongoTemplate, times(1)).findById("1", PassengerRideRequest.class);
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  void givenUserId_findRideEntity() {
    when(reactiveMongoTemplate
        .find(Query.query(Criteria.where("userId").is("1")), PassengerRideRequest.class))
        .thenReturn(
            Flux.just(passengerRideRequest));
    Flux<PassengerRideRequest> flux = passengerRideRequestRepo.findByUserId("1");
    PassengerRideRequest testPassengerRideRequest = flux.blockFirst();

    assertEquals("1", testPassengerRideRequest.getUserId());
    verify(reactiveMongoTemplate, times(1)).find(any(Query.class), eq(PassengerRideRequest.class));
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  void getAllTest() {
    when(reactiveMongoTemplate.findAll(PassengerRideRequest.class))
        .thenReturn(Flux.just(passengerRideRequest));
    Flux<PassengerRideRequest> allPass = passengerRideRequestRepo.getAll();
    PassengerRideRequest oneRequest = allPass.blockFirst();
    assertEquals("1", oneRequest.getId());
    verify(reactiveMongoTemplate, times(1)).findAll(PassengerRideRequest.class);
  }

  @Test
  void correctUpdateStatus() {
    PassengerRideRequest expectedRequest = new PassengerRideRequest(
        "1", "1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.MATCHED);

    when(reactiveMongoTemplate
        .findAndModify(eq(Query.query(Criteria.where("_id").is("1"))
                .addCriteria(Criteria.where("status").is(AVAILABLE))),
            eq(Update.update("status", RideRequestStatus.MATCHED)),
            any(FindAndModifyOptions.class),
            eq(PassengerRideRequest.class)))
        .thenReturn(Mono.just(expectedRequest));

    PassengerRideRequest updatedRequest =
        passengerRideRequestRepo.updateStatus("1",
            RideRequestStatus.AVAILABLE, RideRequestStatus.MATCHED).block();

    assertEquals(expectedRequest.getId(), updatedRequest.getId());
    assertEquals(expectedRequest.getUserId(), updatedRequest.getUserId());
    assertEquals(expectedRequest.getStatus(), updatedRequest.getStatus());
    verify(reactiveMongoTemplate, times(1))
        .findAndModify(any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(PassengerRideRequest.class));
    verifyNoMoreInteractions(reactiveMongoTemplate);
  }

  @Test
  @DisplayName("Get available PassRequests")
  void returnAllAvailableRequests() {
    when(reactiveMongoTemplate.find(Query.query(Criteria.where("status").is(
        RideRequestStatus.AVAILABLE)), PassengerRideRequest.class))
        .thenReturn(Flux.just(passengerRideRequest));
    Flux<PassengerRideRequest> allRequests = passengerRideRequestRepo
        .getAvailableRequests();
    PassengerRideRequest firstRequest = allRequests.blockFirst();
    assertEquals("1", firstRequest.getId());
    assertEquals(RideRequestStatus.AVAILABLE, passengerRideRequest.getStatus());
    assertEquals(AVAILABLE, passengerRideRequest.getStatus());
    verify(reactiveMongoTemplate, times(1)).find(any(), any());
  }

  @Test
  @DisplayName("Get available PassRequests with coincided time")
  void returnAllAvailableRequestsInTime() {
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

    Query query = Query.query(Criteria.where("status").is(RideRequestStatus.AVAILABLE))
        .addCriteria(Criteria.where("userId").ne(driver.getUserId()))
        .addCriteria(Criteria.where("rideDate.end").gte(driver.getRideDate().getStart())
            .andOperator(Criteria.where("rideDate.start").lte(driver.getRideDate().getEnd())));

    when(reactiveMongoTemplate.find(query, PassengerRideRequest.class))
        .thenReturn(Flux.just(passengerRideRequestTimed));
    Flux<PassengerRideRequest> allRequests = passengerRideRequestRepo
        .getAvailablePassengerRequestsInTime(driver.getRideDate().getStart(),
            driver.getRideDate().getEnd(), driver.getUserId());

    StepVerifier.create(allRequests)
        .assertNext(passengerRideRequestFromDb -> {
          assertEquals(RideRequestStatus.AVAILABLE, passengerRideRequestFromDb.getStatus());
          assertEquals(AVAILABLE, passengerRideRequestFromDb.getStatus());
          assertEquals("2", passengerRideRequestFromDb.getUserId());
        })
        .verifyComplete();

    verify(reactiveMongoTemplate, times(1)).find(any(), any());
  }

  @Test
  @DisplayName("Get multiple passengerRideRequests from Repo")
  void findAvailablePassengerRideRequest() {
    PassengerRideRequest requestOne = new PassengerRideRequest("1", "sameUser",
        null, null, null, AVAILABLE);
    PassengerRideRequest requestTwo = new PassengerRideRequest("2", "sameUser",
        null, null, null, AVAILABLE);
    PassengerRideRequest requestThree = new PassengerRideRequest("3", "sameUser",
        null, null, null, AVAILABLE);

    Flux<PassengerRideRequest> flux = Flux.just(requestOne, requestTwo, requestThree);

    Query query = Query.query(Criteria.where("userId").is("sameUser"))
        .addCriteria(Criteria.where("status").is(AVAILABLE));

    when(reactiveMongoTemplate.find(query, PassengerRideRequest.class)).thenReturn(flux);

    StepVerifier.create(passengerRideRequestRepo.findUserRequestByStatus("sameUser", AVAILABLE))
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
