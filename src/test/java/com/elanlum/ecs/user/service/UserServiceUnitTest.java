package com.elanlum.ecs.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.user.exceptions.InvalidTelegramChatIdUpdatingException;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.repository.UserRepository;
import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.validation.ValidationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

  @Mock
  private UserRepository userRepo;
  @Mock
  private ValidationService<User> validationService;
  @InjectMocks
  private UserService userService;


  @Test
  void whenFindByIdThenReturnMonoUserId() {
    User user = new User("11", "log", "name", "1", null);
    when(userRepo.findById("11")).thenReturn(Mono.just(user));
    Mono<User> result = userService.findById("11");
    assertEquals(result.block().getId(), "11");
    verify(userRepo, times(1)).findById("11");
  }

  @Test
  void whenFindByTelegramId_thenReturnMonoUserId() {
    User user = new User("11", "log", "name", "1", null);
    when(userRepo.findByTelegramId("1")).thenReturn(Mono.just(user));
    Mono<User> result = userService.findByTelegramId("1");
    assertEquals(result.block().getId(), "11");
    verify(userRepo, times(1)).findByTelegramId("1");
  }

  @Test
  void whenSaveUserThenReturnMonoUser() {
    User user = new User("11", "log", "name", "1", null);
    when(userRepo.save(user)).thenReturn(Mono.just(user));
    when(validationService.entityValidate(user, ValidationForSave.class))
        .thenReturn(Mono.just(user));
    Mono<User> result = userService.save(user);
    assertEquals(result.block().getId(), "11");
    verify(userRepo, times(1)).save(user);
    verify(validationService, times(1))
        .entityValidate(user, ValidationForSave.class);
  }

  @Test
  void updateTelegramChatIdWithNullTelegramIdTest() {
    StepVerifier.create(userService
        .updateTelegramChatId(null, 2L))
        .expectError(InvalidTelegramChatIdUpdatingException.class)
        .verify();

    verifyZeroInteractions(userRepo);
  }

  @Test
  void updateTelegramChatIdWithNullTelegramChatIdTest() {
    StepVerifier.create(userService
        .updateTelegramChatId("1", null))
        .expectError(InvalidTelegramChatIdUpdatingException.class)
        .verify();

    verifyZeroInteractions(userRepo);
  }

  @Test
  void updateTelegramChatIdWithNonExistingTelegramId() {
    when(userRepo.updateTelegramChatId("0", 1L))
        .thenReturn(Mono.empty());

    StepVerifier.create(userService.updateTelegramChatId("0", 1L))
        .verifyComplete();

    verify(userRepo, times(1))
        .updateTelegramChatId("0", 1L);
  }

  @Test
  void updateTelegramChatIdtWithExistingTelegramId() {
    User user = new User("11", "log", "name", "1", 2L);
    when(userRepo.updateTelegramChatId("1", 2L))
        .thenReturn(Mono.just(user));
    StepVerifier.create(userService.updateTelegramChatId("1", 2L))
        .assertNext(user1 -> assertEquals(user1.getTelegramChatId(), Long.valueOf(2L)))
        .verifyComplete();
    verify(userRepo, times(1))
        .updateTelegramChatId("1", 2L);
    verifyNoMoreInteractions(userRepo);
  }
}
