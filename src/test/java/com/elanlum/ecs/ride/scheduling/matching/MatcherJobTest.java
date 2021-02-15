package com.elanlum.ecs.ride.scheduling.matching;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.elanlum.ecs.ride.crud.service.impl.DriverRideRequestService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.ride.matcher.DriverPassengerMatchingOneBuddyService;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import reactor.core.publisher.Flux;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class MatcherJobTest {

  @Mock
  DriverPassengerMatchingOneBuddyService oneBuddyService;
  @Mock
  DriverRideRequestService driverRideRequestService;
  @Mock
  JobExecutionContext context;

  @Test
  @DisplayName("When job executes it invokes driver ride request service")
  void execute() throws JobExecutionException {
    DriverRideRequest request1 = mock(DriverRideRequest.class);
    DriverRideRequest request2 = mock(DriverRideRequest.class);
    DriverRideRequest request3 = mock(DriverRideRequest.class);
    Flux<DriverRideRequest> driverRequestFlux = Flux.just(request1, request2, request3);

    doReturn(driverRequestFlux).when(driverRideRequestService).getAvailableRequests();
    doNothing().when(oneBuddyService).matchAndNotify(request1);
    doNothing().when(oneBuddyService).matchAndNotify(request2);
    doNothing().when(oneBuddyService).matchAndNotify(request3);
    MatcherJob matcherJob = new MatcherJob(oneBuddyService, driverRideRequestService);

    matcherJob.execute(context);

    verify(driverRideRequestService, times(1)).getAvailableRequests();
    verify(oneBuddyService, times(3)).matchAndNotify(any(DriverRideRequest.class));
  }
}