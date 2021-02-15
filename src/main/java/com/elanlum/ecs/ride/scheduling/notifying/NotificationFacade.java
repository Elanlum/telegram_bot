package com.elanlum.ecs.ride.scheduling.notifying;

import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.ride.scheduling.JobElementsSource;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationFacade {

  private final QuartzNotificationScheduler quartzNotificationScheduler;
  private final NotificationMessageQueue notificationMessageQueue;
  private final JobElementsSource jobElementsSource;

  /**
   * Method takes {@link Notification} and sends it immediately.
   *
   * @param notification - {@link Notification} to be sent.
   */
  public void sendNow(Notification notification) {
    notificationMessageQueue.sendNotification(notification);
    log.debug("Notification with message \"{}\" was sent to user {}", notification.getMessage(),
        notification.getUser().getId());
  }

  /**
   * Method that takes Notification, and schedules it for sending.
   *
   * @param notification - {@link Notification} to be sent.
   * @param localDateTime - {@link LocalDateTime} when to send notification.
   */
  public void sendScheduled(Notification notification, LocalDateTime localDateTime) {
    JobDetail jobDetail = jobElementsSource.notifierJobDetail(notification);
    quartzNotificationScheduler
        .schedule(jobDetail, jobElementsSource.simpleTrigger(jobDetail, localDateTime));
    log.debug("Notification with message \"{}\" was scheduled for sending to user {} at {} on {}",
        notification.getMessage(),
        notification.getUser().getId(),
        localDateTime.toLocalTime(),
        localDateTime.toLocalDate());
  }
}
