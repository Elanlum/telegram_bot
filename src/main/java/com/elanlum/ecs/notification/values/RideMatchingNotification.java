package com.elanlum.ecs.notification.values;

import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.user.model.User;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import lombok.Getter;

@Getter
public class RideMatchingNotification extends Notification {

  private String message;
  private Ride ride;
  private NotificationRecipient notificationTo;

  /**
   * This is constructor for ride notification, destined to notify appropriate user.
   *
   * @param user recipient.
   * @param message what we want to say firstable.
   * @param ride ride entity.
   * @param notificationTo to whom we will send notification.
   */
  public RideMatchingNotification(User user, String message, Ride ride,
      NotificationRecipient notificationTo) {
    super(user);
    this.message = message;
    this.ride = ride;
    this.notificationTo = notificationTo;
  }

  @Override
  public String getMessage() {
    String telegramId = getCompanion().getTelegramId();
    StringBuilder sb = new StringBuilder();
    sb.append(message).append("\n")
        .append("Driver: ").append(ride.getDriver().getName())
        .append("\n")
        .append("Passenger: ").append(ride.getPassenger().getName())
        .append("\n")
        .append("Date of the ride:\n").append(ride.getRideDateTime().format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.UK)))
        .append("\nTime: ")
        .append(
            ride.getRideDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.UK)))
        .append("\n[Chat with your companion](tg://user?id="
            + telegramId + ")");
    return sb.toString();
  }

  /**
   * Method fot getting opposite user to recipient.
   *
   * @return User.
   */
  public User getCompanion() {
    if (isSentToDriver()) {
      return ride.getPassenger();
    }
    return ride.getDriver();
  }

  private boolean isSentToDriver() {
    return notificationTo.equals(NotificationRecipient.DRIVER);

  }
}
