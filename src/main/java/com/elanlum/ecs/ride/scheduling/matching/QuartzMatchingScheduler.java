package com.elanlum.ecs.ride.scheduling.matching;

import com.elanlum.ecs.ride.scheduling.JobElementsSource;
import com.elanlum.ecs.ride.scheduling.config.MatchingConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuartzMatchingScheduler {

  private Scheduler instance;

  /**
   * Constructor for matching scheduler.
   *
   * @param matcherJobFactory - {@link org.quartz.spi.JobFactory} that constructs {@link
   *     MatcherJob}.
   * @param jobElementsSource - {@link JobElementsSource} instance used to get job components.
   * @param matchingConfiguration - {@link MatchingConfiguration} file with matching settings.
   * @param factory - {@link StdSchedulerFactory} instance to get {@link Scheduler} from.
   * @param propertiesFile - name of a properties file for factory initialization.
   */
  @Autowired
  public QuartzMatchingScheduler(MatcherJobFactory matcherJobFactory,
      JobElementsSource jobElementsSource, MatchingConfiguration matchingConfiguration,
      @Qualifier("matchingSchedulerFactory") StdSchedulerFactory factory,
      @Value("${quartz.properties.matching-scheduling}") String propertiesFile) {
    try {
      factory.initialize(propertiesFile);
      log.debug(
          "Matching scheduler factory was initialized using properties file: " + propertiesFile);
      instance = factory.getScheduler();
      instance.setJobFactory(matcherJobFactory);
      instance.start();
      log.debug("Quartz matching job scheduler started running");
      schedule(jobElementsSource, matchingConfiguration);
    } catch (SchedulerException ex) {
      log.error("There was a problem with scheduler or job factory", ex);
      throw new RuntimeException(ex);
    }
  }

  private void schedule(JobElementsSource jobElementsSource,
      MatchingConfiguration matchingConfiguration) {
    try {
      String matchingFrequencyMinutes = matchingConfiguration.getMatchFrequencyMinutes();
      JobDetail jobDetail = jobElementsSource.driverMatcherJobDetail();
      Trigger trigger = jobElementsSource
          .uniqueMatchingTrigger(jobDetail, matchingFrequencyMinutes);
      Trigger oldTrigger = instance.getTrigger(TriggerKey.triggerKey("matching_trigger"));
      if (oldTrigger == null) {
        instance.scheduleJob(jobDetail, trigger);
        log.debug("Matching job scheduled");
      } else {
        instance.rescheduleJob(oldTrigger.getKey(), trigger);
        log.debug("Matching job rescheduled");
      }
    } catch (SchedulerException ex) {
      log.error("Could not schedule the job", ex);
      throw new RuntimeException(ex);
    }
  }
}
