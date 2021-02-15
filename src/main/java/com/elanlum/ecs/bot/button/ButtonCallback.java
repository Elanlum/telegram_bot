package com.elanlum.ecs.bot.button;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ButtonCallback {

  private String command;
  private String rideId;
  private String rideUserId;
}
