package com.elanlum.ecs.ride.model.values;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Setter(AccessLevel.PROTECTED)
@Getter
public class Feedback implements Serializable {

  private boolean rideHappened;
}
