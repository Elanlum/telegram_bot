package com.elanlum.ecs.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.utils.TestCategory;
import java.time.LocalDateTime;
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
class DriverRideRequestValidationServiceTest {

  DriverRideRequest driverRideRequest = new DriverRideRequest("1", "1",
      new Interval(LocalDateTime.now(), LocalDateTime.now().plusMinutes(120L)),
      new Position(0, 0),
      new Position(1, 1),
      RideRequestStatus.AVAILABLE);
  @Mock
  private Validator validator;
  @InjectMocks
  private ValidationService<DriverRideRequest> validationService;

  @Test
  void driverRideRequestValidate() {
    when(validator.validate(any(DriverRideRequest.class), any()))
        .thenReturn(Collections.singleton(mock(ConstraintViolation.class)));
    StepVerifier.create(validationService
        .entityValidate(driverRideRequest, ValidationForSave.class))
        .expectError(ConstraintViolationException.class)
        .verify();
  }

  @Test
  void driverRideRequestValidateEmpty() {
    when(validator.validate(any(DriverRideRequest.class),
        any())).thenReturn(Collections.emptySet());
    Mono<DriverRideRequest> requestMono = validationService.entityValidate(driverRideRequest,
        ValidationForSave.class);
    DriverRideRequest savedRequest = requestMono.block();
    assertEquals(savedRequest.getId(), driverRideRequest.getId());
    assertEquals(savedRequest.getUserId(), driverRideRequest.getUserId());
    verify(validator, times(1))
        .validate(any(DriverRideRequest.class), any());
  }
}
