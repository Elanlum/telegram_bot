package com.elanlum.ecs.ride.scheduling.matching;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.ride.matcher.DriverPassengerMatchingOneBuddyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatcherJob implements Job {

  private final DriverPassengerMatchingOneBuddyService matchingOneService;
  private final DriverRideRequestService driverRideRequestService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    driverRideRequestService.getAvailableRequests()
        .subscribe(matchingOneService::matchAndNotify);
    log.debug("Matching process started");
  }
}
