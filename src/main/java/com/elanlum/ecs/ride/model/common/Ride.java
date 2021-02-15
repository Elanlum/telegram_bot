package com.elanlum.ecs.ride.model.common;

import com.elanlum.ecs.ride.model.values.Feedback;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideStatus;
import com.elanlum.ecs.user.model.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.groups.Default;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "rideEntity")
public class Ride implements Serializable {

  @Null(groups = Default.class)
  @Id
  String id;
  @Valid
  @NotNull(groups = Default.class)
  User driver;
  @Valid
  @NotNull(groups = Default.class)
  User passenger;
  @NotNull(groups = Default.class)
  DriverRideRequest driverRideRequest;
  @NotNull(groups = Default.class)
  PassengerRideRequest passengerRideRequest;
  RideStatus status;
  Feedback driverFeedback;
  Feedback passengerFeedback;

  /**
   * Ride constructor.
   *
   * @param driver - {@link User} instance of the driver.
   * @param passenger - {@link User} instance of the passenger.
   * @param driverRideRequest - {@link DriverRideRequest}.
   * @param passengerRideRequest - {@link PassengerRideRequest}.
   */
  public Ride(User driver, User passenger, DriverRideRequest driverRideRequest,
      PassengerRideRequest passengerRideRequest) {
    this.driver = driver;
    this.passenger = passenger;
    this.driverRideRequest = driverRideRequest;
    this.passengerRideRequest = passengerRideRequest;
    status = RideStatus.OPENED;
  }

  /**
   * Method returns approximate starting position of the ride.
   *
   * @return {@link Position} instance of the starting position
   */
  public Position getStartingPosition() {
    return passengerRideRequest.getDeparturePoint();
  }

  /**
   * Method returns approximate date and time of the ride.
   *
   * @return {@link LocalDateTime} which indicates date and time of the ride
   */
  public LocalDateTime getRideDateTime() {
    if (driverRideRequest.getRideDate().getStart()
        .compareTo(passengerRideRequest.getRideDate().getStart()) > 0) {
      return driverRideRequest.getRideDate().getStart();
    }
    return passengerRideRequest.getRideDate().getStart();
  }
}
