package com.elanlum.ecs.validation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.elanlum.ecs.validation.RideRequestTimeParametersValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;


@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {RideRequestTimeParametersValidator.class})
@Documented
public @interface ValidateTimeParameters {

  /**
   * The default key for creating error messages in case the constraint is violated.
   */
  String message() default "{package com.elanlum.ecs.validation.constraints.message}";

  /**
   * An attribute that allows the specification of validation groups, to which this constraint
   * belongs. This must be an empty array by default. of type Class<?>.
   */
  Class<?>[] groups() default {};

  /**
   * An attribute payload that can be used by clients of the Bean Validation API to assign custom
   * payload objects to a constraint. This attribute is not used by the API itself.
   */
  Class<? extends Payload>[] payload() default {};
}
