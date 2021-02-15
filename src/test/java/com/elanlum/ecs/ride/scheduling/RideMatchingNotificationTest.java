package com.elanlum.ecs.ride.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.values.NotificationRecipient;
import com.elanlum.ecs.notification.values.RideMatchingNotification;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestCategory.UNIT)
class RideMatchingNotificationTest {

  @Test
  void getMessage() {
    User driver = new User(null, "user1", "Ivan", "1", 1L);
    User passenger = new User(null, "user2", "Companion", "2", 2L);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(null, "1",
        new Interval(LocalDateTime.of(2018, 11, 16, 5, 0),
            LocalDateTime.of(2018, 11, 16, 5, 0).plusMinutes(120)),
        null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest = new DriverRideRequest(null, "2",
        new Interval(LocalDateTime.of(2018, 11, 16, 5, 0),
            LocalDateTime.of(2018, 11, 16, 5, 0).plusMinutes(120)),
        null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(driver, passenger, driverRideRequest,
        passengerRideRequest);

    RideMatchingNotification notification =
        new RideMatchingNotification(driver, "Ride is ready", ride,
            NotificationRecipient.PASSENGER);

    assertEquals("Ride is ready\n"
        + "Driver: Ivan\n"
        + "Passenger: Companion\n"
        + "Date of the ride:\n" + "16 November 2018\n"
        + "Time: " + "05:00"
        + "\n[Chat with your companion](tg://user?id=1)", notification.getMessage());
  }

  @Test
  void getCompanion() {
    User driver = new User(null, "user1", "Ivan", "1", 1L);
    User passenger = new User(null, "user2", "Companion", "2", 2L);
    PassengerRideRequest passengerRideRequest = new PassengerRideRequest(null, "1",
        new Interval(LocalDateTime.of(2018, 11, 16, 5, 0),
            LocalDateTime.of(2018, 11, 16, 5, 0).plusMinutes(120)),
        null, null, RideRequestStatus.AVAILABLE);
    DriverRideRequest driverRideRequest = new DriverRideRequest(null, "2",
        new Interval(LocalDateTime.of(2018, 11, 16, 5, 0),
            LocalDateTime.of(2018, 11, 16, 5, 0).plusMinutes(120)),
        null, null, RideRequestStatus.AVAILABLE);
    Ride ride = new Ride(driver, passenger, driverRideRequest,
        passengerRideRequest);

    RideMatchingNotification notification2Driver =
        new RideMatchingNotification(driver, "Ride is ready", ride,
            NotificationRecipient.DRIVER);
    RideMatchingNotification notification2Passenger =
        new RideMatchingNotification(driver, "Ride is ready", ride,
            NotificationRecipient.PASSENGER);

    assertEquals("2", notification2Driver.getCompanion().getTelegramId());
    assertEquals("1", notification2Passenger.getCompanion().getTelegramId());
  }
}
