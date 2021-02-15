package com.elanlum.ecs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class EcsApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(EcsApplication.class);
    application.setWebApplicationType(WebApplicationType.REACTIVE);
    application.run(args);
  }
}
