package com.elanlum.ecs.ride.model.common;

import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.ride.model.values.Role;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "driverRide")
@TypeAlias("driverRide")
public class DriverRideRequest extends AbstractRideRequest {

  public DriverRideRequest(String id, String userId, Interval rideDate, Position departurePoint,
                           Position destinationPoint, RideRequestStatus status) {
    super(id, userId, Role.DRIVER, rideDate, departurePoint, destinationPoint, status);
  }
}
