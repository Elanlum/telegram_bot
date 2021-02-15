package com.elanlum.ecs.notification.values;

import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.user.model.User;

public class BeforeRideNotification extends RideMatchingNotification {

  /**
   * This is constructor for ride notification, destined to notify appropriate user.
   *
   * @param user recipient.
   * @param message what we want to say firstable.
   * @param ride ride entity.
   * @param notificationTo to whom we will send notification.
   */
  public BeforeRideNotification(User user, String message,
      Ride ride,
      NotificationRecipient notificationTo) {
    super(user, message, ride, notificationTo);
  }
}
