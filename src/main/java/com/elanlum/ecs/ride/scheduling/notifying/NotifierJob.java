package com.elanlum.ecs.ride.scheduling.notifying;

import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class NotifierJob implements Job {

  private NotificationMessageQueue notificationMessageQueue;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
    Notification notification = (Notification) mergedJobDataMap.get("notification");
    notificationMessageQueue.sendNotification(notification);
    log.debug("Notification with message \"{}\" was sent to user {}", notification.getMessage(),
        notification.getUser().getId());
  }
}
