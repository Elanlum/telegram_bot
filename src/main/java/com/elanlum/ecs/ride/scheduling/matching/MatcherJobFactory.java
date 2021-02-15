package com.elanlum.ecs.ride.scheduling.matching;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.matcher.DriverPassengerMatchingOneBuddyService;
import lombok.AllArgsConstructor;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MatcherJobFactory implements JobFactory {

  private DriverPassengerMatchingOneBuddyService matchingOneService;
  private DriverRideRequestService driverRideRequestService;

  @Override
  public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
    return new MatcherJob(matchingOneService, driverRideRequestService);
  }
}
