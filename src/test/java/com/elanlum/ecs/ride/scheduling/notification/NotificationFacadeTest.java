package com.elanlum.ecs.ride.scheduling.notification;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.notification.values.SimpleNotification;
import com.elanlum.ecs.ride.scheduling.JobElementsSource;
import com.elanlum.ecs.ride.scheduling.notifying.NotificationFacade;
import com.elanlum.ecs.ride.scheduling.notifying.NotifierJob;
import com.elanlum.ecs.ride.scheduling.notifying.QuartzNotificationScheduler;
import com.elanlum.ecs.user.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class NotificationFacadeTest {

  @Mock
  QuartzNotificationScheduler quartzNotificationScheduler;
  @Mock
  NotificationMessageQueue notificationMessageQueue;
  @Mock
  JobElementsSource jobElementsSource;
  @InjectMocks
  NotificationFacade notificationFacade;

  User user = new User("1", "login", "name", "telegramId", 1L);

  @Test
  void givenNotificationInstance_sendNow_putsItInQueue() {
    Notification notification = new SimpleNotification(user, "testMessage");
    doNothing().when(notificationMessageQueue).sendNotification(notification);

    notificationFacade.sendNow(notification);
    verify(notificationMessageQueue, times(1)).sendNotification(notification);
  }

  @Test
  void givenNotificationInstanceAndLocalDateTime_sendSchedule_schedulesTheJob() {
    Notification notification = new SimpleNotification(user, "testMessage");
    LocalDateTime testLdt = LocalDateTime.of(LocalDate.of(2077, 6, 6), LocalTime.of(4, 20, 0));
    JobDetail testJobDetail = JobBuilder.newJob().ofType(NotifierJob.class).build();
    Trigger testTrigger = TriggerBuilder.newTrigger().forJob(testJobDetail).build();

    doReturn(testJobDetail).when(jobElementsSource).notifierJobDetail(notification);
    doReturn(testTrigger).when(jobElementsSource).simpleTrigger(testJobDetail, testLdt);
    doNothing().when(quartzNotificationScheduler).schedule(testJobDetail, testTrigger);

    notificationFacade.sendScheduled(notification, testLdt);

    verify(jobElementsSource, times(1)).notifierJobDetail(notification);
    verify(jobElementsSource, times(1)).simpleTrigger(testJobDetail, testLdt);
    verify(quartzNotificationScheduler, times(1)).schedule(testJobDetail, testTrigger);
  }
}