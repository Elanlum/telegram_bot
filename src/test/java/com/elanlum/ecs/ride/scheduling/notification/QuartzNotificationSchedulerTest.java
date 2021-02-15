package com.elanlum.ecs.ride.scheduling.notification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.scheduling.notifying.NotificationJobFactory;
import com.elanlum.ecs.ride.scheduling.notifying.NotifierJob;
import com.elanlum.ecs.ride.scheduling.notifying.QuartzNotificationScheduler;

import java.sql.Date;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class QuartzNotificationSchedulerTest {

  @Mock
  NotificationJobFactory notificationJobFactory;
  @Mock
  StdSchedulerFactory schedulerFactory;
  @Mock
  Scheduler scheduler;

  @Test
  @DisplayName("Notification scheduler's constructor invokes internal methods and "
      + "schedule(...) method invokes internal scheduler with given arguments")
  void givenJobDetailAndTrigger_schedule_schedulesTheJob() throws SchedulerException {
    JobDetail testJobDetail = JobBuilder.newJob().ofType(NotifierJob.class).build();
    Trigger testTrigger = TriggerBuilder.newTrigger().forJob(testJobDetail).build();

    doNothing().when(schedulerFactory).initialize(anyString());
    doReturn(scheduler).when(schedulerFactory).getScheduler();
    doNothing().when(scheduler).setJobFactory(notificationJobFactory);
    doNothing().when(scheduler).start();

    doReturn(Date.from(Instant.now())).when(scheduler).scheduleJob(testJobDetail, testTrigger);

    QuartzNotificationScheduler quartzNotificationScheduler = new QuartzNotificationScheduler(
        notificationJobFactory, schedulerFactory, "anyString");

    quartzNotificationScheduler.schedule(testJobDetail, testTrigger);

    verify(schedulerFactory, times(1)).initialize("anyString");
    verify(schedulerFactory, times(1)).getScheduler();
    verify(scheduler, times(1)).setJobFactory(notificationJobFactory);
    verify(scheduler, times(1)).start();
    verify(scheduler, times(1)).scheduleJob(testJobDetail, testTrigger);
  }

  @Test
  @DisplayName("Failed scheduler factory initializing in Notification Scheduler throws exception")
  void exceptionIsCaughtDuringQuartzNotificationSchedulerInitialization()
      throws SchedulerException {
    doThrow(new SchedulerException("ExceptionMessage")).when(schedulerFactory)
        .initialize(anyString());

    assertThrows(RuntimeException.class,
        () -> new QuartzNotificationScheduler(notificationJobFactory, schedulerFactory,
            "anyString"));
  }

  @Test
  @DisplayName("schedule(...) throws exception if internal scheduler fails to schedule the job")
  void scheduleJob_throwsException() throws SchedulerException {
    JobDetail testJobDetail = JobBuilder.newJob().ofType(NotifierJob.class).build();
    Trigger testTrigger = TriggerBuilder.newTrigger().forJob(testJobDetail).build();

    doNothing().when(schedulerFactory).initialize(anyString());
    doReturn(scheduler).when(schedulerFactory).getScheduler();
    doNothing().when(scheduler).setJobFactory(notificationJobFactory);
    doNothing().when(scheduler).start();

    doThrow(new SchedulerException("ExceptionMessage")).when(scheduler)
        .scheduleJob(testJobDetail, testTrigger);

    QuartzNotificationScheduler quartzNotificationScheduler = new QuartzNotificationScheduler(
        notificationJobFactory, schedulerFactory, "anyString");

    assertThrows(RuntimeException.class,
        () -> quartzNotificationScheduler.schedule(testJobDetail, testTrigger));
  }
}
