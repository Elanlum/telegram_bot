package com.elanlum.ecs.ride.crud.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.crud.controller.values.CancelStatus;
import com.elanlum.ecs.ride.crud.controller.values.RideRequestCriteria;
import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Tag(TestCategory.UNIT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {DriverRideRequestController.class,
    ValidationAutoConfiguration.class})
class DriverRideRequestControllerTest {

  @MockBean
  private DriverRideRequestService driverRideRequestService;

  @Autowired
  private DriverRideRequestController driverRideRequestController;

  @Autowired
  private Validator validator;

  private WebTestClient webTestClient;

  private DriverRideRequest driverRideRequest = new DriverRideRequest(
      "1", "1",
      new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
      new Position(0, 0),
      new Position(0, 0),
      RideRequestStatus.AVAILABLE);

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient
        .bindToController(driverRideRequestController)
        .validator(validator)
        .configureClient()
        .baseUrl("/driverRide")
        .build();
  }

  @Test
  @DisplayName("Find a driver ride request by its id.")
  void findById() {
    when(driverRideRequestService.findById(eq("1")))
        .thenReturn(Mono.just(driverRideRequest));

    webTestClient.get()
        .uri("/{id}", "1")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo("1");
  }

  @Test
  @DisplayName("Save a driver ride request.")
  void save() {
    doReturn(Mono.just(driverRideRequest))
        .when(driverRideRequestService).save(any(DriverRideRequest.class));

    webTestClient.post()
        .uri("/save")
        .body(BodyInserters.fromPublisher(Mono.just(driverRideRequest), DriverRideRequest.class))
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("id")
        .isEqualTo("1");
  }

  @Test
  @DisplayName("Find a driver ride request by user id")
  void findRideRequestByUserId() {
    when(driverRideRequestService.findByUserId("1")).thenReturn(Flux.just(driverRideRequest));

    webTestClient.get()
        .uri("/userId/1")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].userId").isEqualTo("1");
  }

  @Test
  @DisplayName("Get request returns only available DriverRideRequests by userId")
  void findDriverAvailableRideRequest() {
    User user = new User("1", "login", "name", null, null);
    Flux<DriverRideRequest> flux = Flux.just(
        new DriverRideRequest("id1", user.getId(), null, null, null, RideRequestStatus.AVAILABLE),
        new DriverRideRequest("id2", user.getId(), null, null, null, RideRequestStatus.AVAILABLE));
    RideRequestCriteria criteria = new RideRequestCriteria("available");

    when(driverRideRequestService
        .findAvailableRequestsByUserId("1", new RideRequestCriteria("available")))
        .thenReturn(flux);

    webTestClient.get()
        .uri("/users/{userId}/driver-requests?status=available", "1")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo("id1")
        .jsonPath("$[1].id")
        .isEqualTo("id2");

    StepVerifier.create(new DriverRideRequestController(driverRideRequestService)
        .findActiveRequestsByUserId("1", criteria))
        .expectNextCount(2)
        .expectComplete()
        .verify();

    verify(driverRideRequestService, times(2))
        .findAvailableRequestsByUserId(anyString(), any(RideRequestCriteria.class));
  }

  @Test
  @DisplayName("Cancel available driver ride request with valid id and status.")
  void cancelAvailableDriverRideRequest() {
    DriverRideRequest driverRideRequestCanceled = new DriverRideRequest(
        "1", "u1",
        new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.CANCELED);
    when(driverRideRequestService
        .cancelRequest(anyString()))
        .thenReturn(Mono.just(driverRideRequestCanceled));

    CancelStatus status = new CancelStatus("canceled");

    webTestClient.patch()
        .uri("/users/{userId}/driver-requests/{id}", "u1", "1")
        .body(BodyInserters
            .fromPublisher(Mono.just(status), CancelStatus.class))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.userId").isEqualTo("u1")
        .jsonPath("$.id").isEqualTo("1");

    verify(driverRideRequestService, times(1))
        .cancelRequest(anyString());
  }

  @Test
  @DisplayName("Failed cancellation of non existent driver ride request.")
  void cancelNonExistentDriverRideRequestFailed() {
    when(driverRideRequestService.cancelRequest(anyString()))
        .thenReturn(Mono.empty());
    CancelStatus status = new CancelStatus("canceled");

    when(driverRideRequestService.cancelRequest(anyString())).thenReturn(Mono.empty());

    webTestClient.patch()
        .uri("/users/{userId}/driver-requests/{id}", "u1", "1000")
        .body(BodyInserters
            .fromPublisher(Mono.just(status), CancelStatus.class))
        .exchange()
        .expectStatus().isNotFound()
        .expectBody();

    verify(driverRideRequestService, times(1)).cancelRequest(anyString());
  }

  @Test
  @DisplayName("Failed cancellation of a driver ride request with an invalid status.")
  void cancelMatchedDriverRideRequestFailed() {
    CancelStatus status = new CancelStatus("available");

    this.webTestClient.patch()
        .uri("/users/{userId}/driver-requests/{id}", "u1", "1")
        .body(BodyInserters
            .fromPublisher(Mono.just(status), CancelStatus.class))
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody();
  }
}
