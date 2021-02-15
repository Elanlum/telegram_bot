package com.elanlum.ecs.ride.scheduling.notification;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.notification.values.SimpleNotification;
import com.elanlum.ecs.ride.scheduling.notifying.NotifierJob;
import com.elanlum.ecs.user.model.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class NotifierJobTest {

  @Mock
  NotificationMessageQueue notificationMessageQueue;
  @InjectMocks
  NotifierJob notifierJob;
  @Mock
  JobExecutionContext jobExecutionContext;
  @Mock
  JobDataMap jobDataMap;

  @Test
  void execute_putsNotificationToQueue() throws JobExecutionException {
    User user = new User("1", "login", "name", "telegramId", 1L);
    Object testNotificationObj = new SimpleNotification(user, "testMessage");

    doReturn(jobDataMap).when(jobExecutionContext).getMergedJobDataMap();
    doReturn(testNotificationObj).when(jobDataMap).get("notification");
    Notification testNotification = (Notification) testNotificationObj;
    doNothing().when(notificationMessageQueue).sendNotification(testNotification);

    notifierJob.execute(jobExecutionContext);

    verify(jobExecutionContext, times(1)).getMergedJobDataMap();
    verify(jobDataMap, times(1)).get("notification");
    verify(notificationMessageQueue, times(1)).sendNotification(testNotification);
  }
}