package com.elanlum.ecs.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.utils.TestCategory;
import java.time.Clock;
import java.time.LocalDateTime;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class RideRequestTimeParametersValidatorTest {

  RideRequestTimeParametersValidator validator = new RideRequestTimeParametersValidator();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    when(context.getClockProvider().getClock()).thenReturn(Clock.systemDefaultZone());
  }

  @Test
  @DisplayName("Testing valid Interval instance")
  void givenValidInterval_isValid_returnsTrue() {
    Interval validInterval = new Interval(LocalDateTime.now().plusMinutes(5),
        LocalDateTime.now().plusMinutes(15));
    assertTrue(validator.isValid(validInterval, context));
  }

  @Test
  @DisplayName("Checking that an Interval instance with start time in the past is invalid")
  void givenStartIsInThePast_isValid_returnsFalse() {
    Interval invalidInterval = new Interval(LocalDateTime.now().minusMinutes(10),
        LocalDateTime.now());
    assertFalse(validator.isValid(invalidInterval, context));
  }

  @Test
  @DisplayName("Checking that an Interval instance with end time before start time is invalid")
  void givenEndTimeLaterThanStartTime_isValid_returnsFalse() {
    Interval invalidInterval = new Interval(LocalDateTime.now().plusMinutes(10),
        LocalDateTime.now().plusMinutes(5));
    assertFalse(validator.isValid(invalidInterval, context));
  }
}
