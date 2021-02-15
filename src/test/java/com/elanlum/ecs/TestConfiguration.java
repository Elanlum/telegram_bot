package com.elanlum.ecs;

import com.elanlum.ecs.ride.scheduling.matching.QuartzMatchingScheduler;
import com.elanlum.ecs.ride.scheduling.notifying.QuartzNotificationScheduler;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {

  @MockBean
  private QuartzNotificationScheduler quartzNotificationScheduler;
  @MockBean
  private QuartzMatchingScheduler quartzMatchingScheduler;
}
