package com.elanlum.ecs.bot.context.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContextType {
  CREATE_PASSENGER_REQUEST("passenger", "Create passenger request"),
  CREATE_DRIVER_REQUEST("driver", "Create driver request");

  private final String role;
  private final String commandName;
}
