package com.elanlum.ecs.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@Tag(TestCategory.UNIT)
class UserControllerTest {

  UserService userService = mock(UserService.class);

  private WebTestClient webTestClient;

  @BeforeEach
  public void setUp() {
    this.webTestClient = WebTestClient.bindToController(new UserController(userService)).build();
  }

  @Test
  void save() {
    doReturn(Mono.just(new User("111", "", "", "", null)))
        .when(userService).save(any(User.class));

    this.webTestClient.post().uri("/user/save")
        .body(BodyInserters.fromPublisher(Mono.just(
            new User("111", "", "", "", null)), User.class))
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("id")
        .isEqualTo("111");
  }

  @Test
  void findById() {
    this.webTestClient.get().uri("/user/1")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void findByTelegramId_returnsUser() {
    this.webTestClient.get().uri("/user/telegramId/1")
        .exchange()
        .expectStatus().isOk();
  }
}
