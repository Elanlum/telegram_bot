package com.elanlum.ecs.ride.scheduling.config;

import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

  @Bean(name = "notificationSchedulerFactory")
  public StdSchedulerFactory notificationSchedulerFactory() {
    return new StdSchedulerFactory();
  }

  @Bean(name = "matchingSchedulerFactory")
  public StdSchedulerFactory matchingSchedulerFactory() {
    return new StdSchedulerFactory();
  }
}
