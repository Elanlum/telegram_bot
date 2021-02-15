package com.elanlum.ecs.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.utils.TestCategory;
import java.util.Collections;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
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
class UserValidationServiceTest {

  private User user = new User("11", "log", "name", "1", null);
  @Mock
  private Validator validator;
  @InjectMocks
  private ValidationService<User> validationService;

  @Test
  void userValidate() {
    when(validator.validate(any(User.class), any()))
        .thenReturn(Collections.singleton(mock(ConstraintViolation.class)));
    StepVerifier.create(validationService.entityValidate(user, ValidationForSave.class))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  void userValidateEmpty() {
    when(validator.validate(any(User.class), any())).thenReturn(Collections.emptySet());
    Mono<User> userMono = validationService.entityValidate(user, ValidationForSave.class);
    User savedUser = userMono.block();
    assertEquals(savedUser.getId(), user.getId());
    assertEquals(savedUser.getTelegramId(), user.getTelegramId());
    verify(validator, times(1)).validate(any(User.class), any());
  }
}
