package com.elanlum.ecs.ride.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.notification.values.SimpleNotification;
import com.elanlum.ecs.ride.scheduling.config.MatchingConfiguration;
import com.elanlum.ecs.ride.scheduling.matching.MatcherJob;
import com.elanlum.ecs.ride.scheduling.notifying.NotifierJob;
import com.elanlum.ecs.user.model.User;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class JobElementsSourceTest {

  @Mock
  private MatchingConfiguration matchingConfiguration;
  @InjectMocks
  private JobElementsSource jobElementsSource;
  private User user = new User("1", "login", "name", "telegramId", 1L);
  private Notification notification = new SimpleNotification(user, "testMessage");
  private LocalDateTime testLocalDateTime = LocalDateTime.now();

  @Test
  @DisplayName("Invoking notifierJobDetail returns JobDetail instance for NotificationJob "
      + "with notification inside it's map")
  void notifierJobDetail() {
    JobDetail testJobDetail = jobElementsSource.notifierJobDetail(notification);

    Notification testNotification = (Notification) testJobDetail.getJobDataMap()
        .get("notification");
    assertEquals("1", testNotification.getUser().getId());
    assertEquals("testMessage", testNotification.getMessage());
    assertEquals(NotifierJob.class, testJobDetail.getJobClass());
  }

  @Test
  @DisplayName("Invoking simpleTrigger returns Trigger instance for the given jobDetail "
      + "with given date settings")
  void simpleTrigger() {
    JobDetail testJobDetail = jobElementsSource.notifierJobDetail(notification);
    JobKey testJobDetailKey = testJobDetail.getKey();
    Trigger trigger = jobElementsSource.simpleTrigger(testJobDetail, testLocalDateTime);

    assertEquals(testJobDetailKey, trigger.getJobKey());
    assertEquals("One-time trigger for custom jobs", trigger.getDescription());
    assertEquals(Date.from(testLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()),
        trigger.getStartTime());
  }

  @Test
  @DisplayName("Invoking driverMatcherJobDetail returns JobDetail instance for MatcherJob")
  void driverMatcherJobDetail() {
    assertEquals(MatcherJob.class, jobElementsSource.driverMatcherJobDetail().getJobClass());
  }

  @Test
  @DisplayName("Invoking uniqueMatchingTrigger returns specific trigger for matching job")
  void uniqueMatchingTrigger() {
    Trigger testTrigger = jobElementsSource
        .uniqueMatchingTrigger(jobElementsSource.driverMatcherJobDetail(), "1");

    assertEquals("Trigger that fires the given job periodically", testTrigger.getDescription());
    assertEquals(TriggerKey.triggerKey("matching_trigger"), testTrigger.getKey());
  }
}