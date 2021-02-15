package com.elanlum.ecs.ride.model.common;

import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.Role;
import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.ride.model.values.Interval;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.Null;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AbstractRideRequest implements Serializable {

  @Null(groups = ValidationForSave.class, message = "Ride request id should be null. ")
  @Id
  private String id;
  @Field("userId")
  private String userId;
  @Field("role")
  private Role role;
  @Field("date")
  @Valid
  private Interval rideDate;
  @Field("departure")
  private Position departurePoint;
  @Field("destination")
  private Position destinationPoint;
  @Field("status")
  private RideRequestStatus status;
}
