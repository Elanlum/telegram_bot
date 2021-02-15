package com.elanlum.ecs.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.IntegrationTestsConfig;
import com.elanlum.ecs.user.model.User;

import java.util.stream.Stream;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.INTEGRATION)
@DirtiesContext
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = IntegrationTestsConfig.class)
public class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  private User user = new User(
      null, "100", "100", "100", null);

  private static Stream<Arguments> createInvalidUser() {
    return Stream.of(
        Arguments.of(new User("1", "", "name", "1", null)),
        Arguments.of(new User(null, "l", "name", "1", null)),
        Arguments.of(new User(
            null, "thisIsInvalidLoginThisIsInvalidLogin", "name", "1", null)),
        Arguments.of(new User(null, "login", "", "1", null)),
        Arguments.of(new User(null, "login", "n", "1", null)),
        Arguments.of(new User(
            null, "login", "thisIsInvalidNameThisIsInvalidName", "1", null)),
        Arguments.of(new User("1", "log", "name", "1", null)),
        Arguments.of(new User("", "login", "name", "1", null))
    );
  }

  private static Stream<Arguments> createValidUser() {
    return Stream.of(
        Arguments.of(new User(null, "login", "name", "1", null)),
        Arguments.of(new User(null, "login", "name", null, null))
    );
  }

  @ParameterizedTest
  @MethodSource("createInvalidUser")
  void checkInvalidUserArguments(User user) {
    StepVerifier.create(userService.save(user))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @ParameterizedTest
  @MethodSource("createValidUser")
  void checkValidUserArguments(User user) {
    StepVerifier.create(userService.save(user))
        .expectNextMatches(user1 -> {
          assertEquals(user.getName(), user1.getName());
          assertEquals(user.getLogin(), user1.getLogin());
          return true;
        })
        .expectComplete()
        .verify();
  }

  @Test
  void findById() {
    Mono<User> save = userService.save(user);
    User savedUser = save.block();
    assertNotNull(savedUser);

    Mono<User> foundMonoUser = userService.findById(savedUser.getId());
    User foundUser = foundMonoUser.block();
    assertEquals(user.getTelegramId(), foundUser.getTelegramId());
    assertEquals(user.getLogin(), foundUser.getLogin());
    assertEquals(user.getName(), foundUser.getName());
  }

  @Test
  void findByTelegramId_returnsUserMono() {
    Mono<User> saved = userService.save(user);
    User savedUser = saved.block();
    assertNotNull(savedUser);

    Mono<User> foundMonoUser = userService.findByTelegramId(savedUser.getTelegramId());
    User foundUser = foundMonoUser.block();
    assertEquals(user.getTelegramId(), foundUser.getTelegramId());
    assertEquals(user.getLogin(), foundUser.getLogin());
    assertEquals(user.getName(), foundUser.getName());
  }

  @Test
  void findByTelegramId_returnsEmptyMono() {
    Mono<User> foundMonoUser = userService.findByTelegramId("3");
    StepVerifier.create(foundMonoUser.hasElement())
        .assertNext(hasElement -> assertFalse(hasElement))
        .verifyComplete();
  }

  @Test
  void save() {
    Mono<User> save = userService.save(user);
    User savedUser = save.block();
    assertNotNull(savedUser);
  }
}
