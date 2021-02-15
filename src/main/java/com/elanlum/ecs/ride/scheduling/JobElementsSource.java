package com.elanlum.ecs.ride.scheduling;

import static org.quartz.CronScheduleBuilder.cronSchedule;

import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.ride.scheduling.matching.MatcherJob;
import com.elanlum.ecs.ride.scheduling.notifying.NotifierJob;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobElementsSource {

  /**
   * Method for getting {@link JobDetail} for quartz job constructing for notifying.
   *
   * @param notification - {@link Notification} to send.
   * @return {@link JobDetail} for the notification job for quartz.
   */
  public JobDetail notifierJobDetail(Notification notification) {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("notification", notification);
    return JobBuilder.newJob().ofType(NotifierJob.class)
        .withDescription("Job that sends notification to user")
        .usingJobData(jobDataMap)
        .build();
  }

  /**
   * Method for getting {@link JobDetail} for quartz job constructing for matching.
   *
   * @return {@link JobDetail} for matcher job for quartz.
   */
  public JobDetail driverMatcherJobDetail() {
    return JobBuilder.newJob().ofType(MatcherJob.class)
        .withDescription("Job that is used for driver and passenger matching")
        .build();
  }

  /**
   * Method for getting {@link Trigger} for quartz job constructing. This one is for triggering any
   * jobs only once.
   *
   * @param jobDetail - which job to trigger.
   * @param localDateTime - when to trigger it.
   * @return associated trigger.
   */
  public Trigger simpleTrigger(JobDetail jobDetail, LocalDateTime localDateTime) {
    return TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withDescription("One-time trigger for custom jobs")
        .startAt(Date.from(
            localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()))
        .build();
  }

  /**
   * Method for getting {@link Trigger} for quartz job constructing. This one is for triggering any
   * jobs only once.
   *
   * @param jobDetail - which job to trigger.
   * @return associated {@link Trigger}
   */
  public Trigger uniqueMatchingTrigger(JobDetail jobDetail, String minutes) {
    return TriggerBuilder.newTrigger()
        .withIdentity(TriggerKey.triggerKey("matching_trigger"))
        .forJob(jobDetail)
        .withDescription("Trigger that fires the given job periodically")
        .withSchedule(cronSchedule("0 0/" + minutes + " * * * ?"))
        .build();
  }
}
