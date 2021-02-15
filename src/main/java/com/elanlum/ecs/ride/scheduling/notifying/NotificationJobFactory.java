package com.elanlum.ecs.ride.scheduling.notifying;

import com.elanlum.ecs.notification.NotificationMessageQueue;
import lombok.AllArgsConstructor;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NotificationJobFactory implements JobFactory {

  private NotificationMessageQueue notificationMessageQueue;

  @Override
  public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
    return new NotifierJob(notificationMessageQueue);
  }
}
