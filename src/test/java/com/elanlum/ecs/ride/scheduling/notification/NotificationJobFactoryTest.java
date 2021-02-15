package com.elanlum.ecs.ride.scheduling.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.ride.scheduling.notifying.NotificationJobFactory;
import com.elanlum.ecs.ride.scheduling.notifying.NotifierJob;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class NotificationJobFactoryTest {

  @Mock
  TriggerFiredBundle triggerFiredBundle;
  @Mock
  Scheduler scheduler;
  @Mock
  NotificationMessageQueue notificationMessageQueue;
  @InjectMocks
  NotificationJobFactory notificationJobFactory;

  @Test
  void newJob_returnsNewNotifierJob() throws SchedulerException {
    Job job = notificationJobFactory.newJob(triggerFiredBundle, scheduler);

    assertEquals(NotifierJob.class, job.getClass());
    verifyNoMoreInteractions(triggerFiredBundle);
    verifyNoMoreInteractions(scheduler);
  }
}