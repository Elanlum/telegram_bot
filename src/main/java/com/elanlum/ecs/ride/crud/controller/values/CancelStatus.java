package com.elanlum.ecs.ride.crud.controller.values;

import java.beans.ConstructorProperties;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor(onConstructor = @__({@ConstructorProperties({"status"})}))
@EqualsAndHashCode
public class CancelStatus {

  @Pattern(regexp = "canceled")
  private String status;
}

