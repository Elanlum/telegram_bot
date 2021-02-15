package com.elanlum.ecs.ride.scheduling.notifying;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuartzNotificationScheduler {

  private Scheduler instance;

  /**
   * Constructor for notification scheduler.
   *
   * @param notificationJobFactory - {@link org.quartz.spi.JobFactory} that constructs {@link
   *     NotifierJob}.
   * @param factory - {@link StdSchedulerFactory} instance to get {@link Scheduler} from.
   * @param propertiesFile - name of a properties file for factory initialization.
   */
  @Autowired
  public QuartzNotificationScheduler(NotificationJobFactory notificationJobFactory,
      @Qualifier("notificationSchedulerFactory") StdSchedulerFactory factory,
      @Value("${quartz.properties.notification-scheduling}") String propertiesFile) {
    try {
      factory.initialize(propertiesFile);
      log.debug("Notification scheduler factory was initialized using properties file: "
          + propertiesFile);
      instance = factory.getScheduler();
      instance.setJobFactory(notificationJobFactory);
      instance.start();
      log.debug("Quartz notification job scheduler started running");
    } catch (SchedulerException ex) {
      log.error("There was a problem with scheduler or job factory", ex);
      throw new RuntimeException(ex);
    }
  }

  /**
   * Method schedules a job based on input parameters.
   *
   * @param jobDetail - {@link JobDetail} instance used to instantiate the job.
   * @param trigger - {@link Trigger} instance used to instantiate the job.
   */
  public void schedule(JobDetail jobDetail, Trigger trigger) {
    try {
      instance.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException ex) {
      log.error("Could not schedule the job: ", ex);
      throw new RuntimeException(ex);
    }
  }
}
