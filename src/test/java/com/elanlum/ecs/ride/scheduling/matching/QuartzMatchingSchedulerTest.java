package com.elanlum.ecs.ride.scheduling.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.quartz.CronScheduleBuilder.cronSchedule;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.scheduling.JobElementsSource;
import com.elanlum.ecs.ride.scheduling.config.MatchingConfiguration;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class QuartzMatchingSchedulerTest {

  @Mock
  JobElementsSource jobElementsSource;
  @Mock
  MatchingConfiguration configuration;
  @Mock
  MatcherJobFactory matcherJobFactory;
  @Mock
  StdSchedulerFactory schedulerFactory;
  @Mock
  Scheduler scheduler;

  @ParameterizedTest
  @DisplayName("Matching scheduler's constructor invokes internal methods and "
      + "schedules or reschedules the job depending on triggers found")
  @ValueSource(strings = {"scheduling", "rescheduling"})
  void quartzMatchingSchedulerSchedules(String action) throws SchedulerException {
    JobDetail testJobDetail = JobBuilder.newJob().ofType(MatcherJob.class)
        .withDescription("Job that is used for driver and passenger matching")
        .build();
    Trigger testTrigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("matching_trigger"))
        .forJob(testJobDetail)
        .withDescription("Trigger that fires the given job periodically")
        .withSchedule(cronSchedule("0 0/1 * * * ?"))
        .build();

    doNothing().when(schedulerFactory).initialize(anyString());
    doReturn(scheduler).when(schedulerFactory).getScheduler();
    doNothing().when(scheduler).setJobFactory(matcherJobFactory);
    doNothing().when(scheduler).start();
    doReturn("1").when(configuration).getMatchFrequencyMinutes();
    if (action.equals("scheduling")) {
      doReturn(null).when(scheduler).getTrigger(TriggerKey.triggerKey("matching_trigger"));
      doReturn(Date.from(Instant.now())).when(scheduler).scheduleJob(testJobDetail, testTrigger);
    } else if (action.equals("rescheduling")) {
      doReturn(testTrigger).when(scheduler).getTrigger(TriggerKey.triggerKey("matching_trigger"));
    }
    doReturn(testJobDetail).when(jobElementsSource).driverMatcherJobDetail();
    doReturn(testTrigger).when(jobElementsSource).uniqueMatchingTrigger(testJobDetail, "1");

    QuartzMatchingScheduler quartzNotificationScheduler = new QuartzMatchingScheduler(
        matcherJobFactory, jobElementsSource, configuration, schedulerFactory, "anyString");

    verify(schedulerFactory, times(1)).initialize("anyString");
    verify(schedulerFactory, times(1)).getScheduler();
    verify(scheduler, times(1)).setJobFactory(matcherJobFactory);
    verify(scheduler, times(1)).start();
    if (action.equals("scheduling")) {
      verify(scheduler, times(1)).scheduleJob(testJobDetail, testTrigger);
    } else if (action.equals("rescheduling")) {
      verify(scheduler, times(1))
          .rescheduleJob(TriggerKey.triggerKey("matching_trigger"), testTrigger);
    }
  }

  @Test
  @DisplayName("Exception is thrown if initializing scheduler factory in Matching Scheduler fails")
  void exceptionIsCaughtDuringQuartzMatchingSchedulerInitialization()
      throws SchedulerException {
    doThrow(new SchedulerException("ExceptionMessage")).when(schedulerFactory)
        .initialize(anyString());

    assertThrows(RuntimeException.class,
        () -> new QuartzMatchingScheduler(matcherJobFactory, jobElementsSource, configuration,
            schedulerFactory,
            "anyString"));
  }

  @Test
  @DisplayName("schedule(...) throws exception if internal scheduler fails to schedule the job")
  void scheduleJobThrowsException() throws SchedulerException {
    JobDetail testJobDetail = JobBuilder.newJob().ofType(MatcherJob.class).build();
    Trigger testTrigger = TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("matching_trigger")).forJob(testJobDetail).build();

    doNothing().when(schedulerFactory).initialize(anyString());
    doReturn(scheduler).when(schedulerFactory).getScheduler();
    doReturn(null).when(scheduler).getTrigger(TriggerKey.triggerKey("matching_trigger"));
    doReturn("1").when(configuration).getMatchFrequencyMinutes();
    doNothing().when(scheduler).setJobFactory(matcherJobFactory);
    doNothing().when(scheduler).start();
    doReturn(testJobDetail).when(jobElementsSource).driverMatcherJobDetail();
    doReturn(testTrigger).when(jobElementsSource).uniqueMatchingTrigger(testJobDetail, "1");

    doThrow(new SchedulerException("ExceptionMessage")).when(scheduler)
        .scheduleJob(testJobDetail, testTrigger);

    try {
      new QuartzMatchingScheduler(
          matcherJobFactory, jobElementsSource, configuration, schedulerFactory, "anyString");
    } catch (RuntimeException ex) {
      assertEquals("org.quartz.SchedulerException: ExceptionMessage", ex.getMessage());
    }
  }
}