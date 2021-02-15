package com.elanlum.ecs.ride.scheduling.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.matcher.DriverPassengerMatchingOneBuddyService;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
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
class MatcherJobFactoryTest {

  @Mock
  TriggerFiredBundle triggerFiredBundle;
  @Mock
  DriverPassengerMatchingOneBuddyService oneBuddyService;
  @Mock
  DriverRideRequestService driverRideRequestService;
  @Mock
  Scheduler scheduler;
  @InjectMocks
  MatcherJobFactory matcherJobFactory;

  @Test
  @DisplayName("Check creation of a Mathcer job by MatcherJobFactory")
  void getNewJobFromFactory() throws SchedulerException {
    Job job = matcherJobFactory.newJob(triggerFiredBundle, scheduler);
    Objects.requireNonNull(job);
    assertEquals(job.getClass(), MatcherJob.class);
    verifyNoMoreInteractions(triggerFiredBundle);
    verifyNoMoreInteractions(scheduler);
  }
}