package com.elanlum.ecs.ride.scheduling.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "matching-settings")
@Getter
@Setter
public class MatchingConfiguration {

  private String matchFrequencyMinutes;
}
