package com.elanlum.ecs.bot.context.exceptions;

public class WrongCommandException extends RuntimeException {

  public WrongCommandException(String message) {
    super(message);
  }
}
