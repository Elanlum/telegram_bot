package com.elanlum.ecs.ride.model.values;

import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.validation.constraints.ValidateTimeParameters;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ValidateTimeParameters(groups = ValidationForSave.class,
    message = "Invalid ride request time parameters. ")
@NotNull(groups = ValidationForSave.class, message = "Interval can not be null.")
public class Interval implements Serializable {

  @NotNull(groups = ValidationForSave.class, message = "Start time can not be null.")
  private LocalDateTime start;
  @NotNull(groups = ValidationForSave.class, message = "End time can not be null.")
  private LocalDateTime end;
}
