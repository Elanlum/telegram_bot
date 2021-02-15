package com.elanlum.ecs.bot.context.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FieldName {
  TELEGRAM_ID("/setTelegramId", 0),
  ROLE("/setRole", 0),
  DEPARTURE_POSITION("/setDeparture", 1),
  DESTINATION_POSITION("/setDestination", 2),
  RIDE_DATE("/setDate", 3),
  RIDE_TIME("/setTime", 4),
  EXPECTATION_PERIOD("/setExpectationPeriod", 5);

  private final String command;
  private final int state;

  /**
   * Method takes in an int in range from 1 to 5, which represents a state in {@link UserContext}.
   * This state shows which parameter is required to proceed with filling in ride request
   * parameters.
   *
   * @param state - state of {@link UserContext}.
   * @return String of information for user about what parameter to send next.
   */
  public static String getInfo(int state) {
    switch (state) {
      case 1:
        return getDepartureInfo();
      case 2:
        return getDestinationInfo();
      case 3:
        return getDateInfo();
      case 4:
        return getTimeInfo();
      case 5:
        return getExpectationInfo();
      default:
        throw new UnsupportedOperationException();
    }
  }

  private static String getDepartureInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("Please enter your departure point.\n");
    sb.append("This is the position, from which you start your ride.\n");
    sb.append("Position should be described as two coordinates separated by space:\n");
    sb.append("[Latitude] [Longitude]\n");
    sb.append("Dot should be used as decimal separator.\n");
    sb.append("Or you could just send location using telegram API.");
    return sb.toString();
  }

  private static String getDestinationInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("Please enter your destination point.\n");
    sb.append("This is the position, where you are going.\n");
    sb.append("Position should be described as two coordinates separated by space:\n");
    sb.append("[Latitude] [Longitude]\n");
    sb.append("Dot should be used as decimal separator.\n");
    sb.append("Or you could just send location using telegram API.");
    return sb.toString();
  }

  private static String getDateInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("Please enter your ride date.\n");
    sb.append("This is the date of desired ride:\n");
    sb.append("Use following date formats:\n");
    sb.append("DD-MM-YYYY\n");
    sb.append("YYYY-MM-DD\n");
    return sb.toString();
  }

  private static String getTimeInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("Please enter your ride time.\n");
    sb.append("What time are you going?\n");
    sb.append("Use 24h time format as follows:\n");
    sb.append("15:00\n");
    sb.append("3:00\n");
    return sb.toString();
  }

  private static String getExpectationInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("Please enter an expectation period.\n");
    sb.append("How long would you not mind to wait for your companion?\n");
    sb.append("Just enter a number of minutes.\n");
    return sb.toString();
  }
}
