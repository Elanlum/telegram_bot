package com.elanlum.ecs.ride.exceptions;

public class UserFromRideRequestDoesNotExist extends RuntimeException {

  public UserFromRideRequestDoesNotExist(String message) {
    super(message);
  }
}
