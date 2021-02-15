package com.elanlum.ecs.ride.matcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@Tag(TestCategory.UNIT)
class DriverPassengerMatchingControllerTest {

  DriverPassengerBestMatchesFacade implementation = mock(
      DriverPassengerBestMatchesFacade.class);
  private DriverPassengerMatchingController controller = new DriverPassengerMatchingController(
      implementation);

  private WebTestClient webTestClient = WebTestClient.bindToController(controller)
      .configureClient()
      .baseUrl("/pairs")
      .build();

  @Test
  void getNearPassengers() {
    doReturn(Flux.just(new PassengerRideRequest("test",
        null, null, null, null, RideRequestStatus.AVAILABLE)))
        .when(implementation).getBestPassengers(any(String.class));

    webTestClient.get()
        .uri("/driver/{id}", "test")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id").isEqualTo("test");
  }
}