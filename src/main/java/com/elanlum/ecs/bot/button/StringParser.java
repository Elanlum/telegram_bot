package com.elanlum.ecs.bot.button;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StringParser {

  /**
   * Parse of string message that contains 3 parts divided by delimiter.
   *
   * @param message input string
   * @return Mono object that contains ButtonCallback entity
   */
  public Mono<ButtonCallback> parse(String message) {
    String[] callback = message.split(":");

    if (callback.length != 3) {
      return Mono.empty();
    }

    String command = callback[0];
    String rideId = callback[1];
    String rideUserId = callback[2];

    ButtonCallback buttonCallback = new ButtonCallback(command, rideId,
        rideUserId);

    return Mono.just(buttonCallback);
  }

  String combine(ButtonCallback buttonCallback) {
    return buttonCallback.getCommand() + ":"
        + buttonCallback.getRideId() + ":"
        + buttonCallback.getRideUserId();
  }
}
