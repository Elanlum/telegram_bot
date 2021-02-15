package com.elanlum.ecs.validation;

import com.elanlum.ecs.validation.constraints.ValidateTimeParameters;
import com.elanlum.ecs.ride.model.values.Interval;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RideRequestTimeParametersValidator
    implements ConstraintValidator<ValidateTimeParameters, Interval> {

  @Override
  public void initialize(ValidateTimeParameters constraintAnnotation) {

  }

  @Override
  public boolean isValid(Interval value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    LocalDateTime now = LocalDateTime.now(context.getClockProvider().getClock());
    if (value.getStart().isAfter(now)) {
      if (value.getEnd().isAfter(value.getStart())) {
        return true;
      }
    }
    return false;
  }
}
