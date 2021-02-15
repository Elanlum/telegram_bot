package com.elanlum.ecs.bot.context.model;

import com.elanlum.ecs.bot.context.exceptions.UnparsableInputException;
import com.elanlum.ecs.bot.context.exceptions.WrongCommandException;
import com.elanlum.ecs.ride.model.values.Position;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RideRequestFieldParser {

  private static final Pattern COORDINATE_PATTERN = Pattern.compile(
      "^-?[1-9]?[0-9]{0,2}\\.[0-9][0-9]{0,5}[1,9]?$");

  private static final Pattern DATE_WEST_PATTERN = Pattern.compile(
      "^((2000|2400|2800|(19|2[0-9](0[48]|[2468][048]|[13579][26])))-02-29)$"
          + "|^(((19|2[0-9])[0-9]{2})-02-(0[1-9]|1[0-9]|2[0-8]))$"
          + "|^(((19|2[0-9])[0-9]{2})-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))$"
          + "|^(((19|2[0-9])[0-9]{2})-(0[469]|11)-(0[1-9]|[12][0-9]|30))$");


  private static final Pattern DATE_RUS_PATTERN = Pattern.compile(
      "^(29-02-(2000|2400|2800|(19|2[0-9](0[48]|[2468][048]|[13579][26]))))$"
          + "|^((0[1-9]|1[0-9]|2[0-8])-02-((19|2[0-9])[0-9]{2}))$"
          + "|^((0[1-9]|[12][0-9]|3[01])-(0[13578]|10|12)-((19|2[0-9])[0-9]{2}))$"
          + "|^((0[1-9]|[12][0-9]|30)-(0[469]|11)-((19|2[0-9])[0-9]{2}))$");

  private static final Pattern TIME_24HRS_PATTERN = Pattern
      .compile("([01]?[0-9]|2[0-3]):([0-5][0-9])");

  private static final DateTimeFormatter DATE_WEST_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd");

  private static final DateTimeFormatter DATE_RUS_FORMATTER = DateTimeFormatter
      .ofPattern("dd-MM-yyyy");

  /**
   * Parses String object that contains latitude and longitude divided by single space into Position
   * object.
   *
   * @param msg is a string received from User
   * @return gives Position object that contains double values for latitude and longitude
   */
  public Position getParsedPosition(String msg) throws UnparsableInputException {
    String str = msg.trim();
    String[] coordinates = str.split("\\s");
    return new Position(Float.parseFloat(coordinates[0]), Float.parseFloat(coordinates[1]));
  }

  /**
   * Parses String object that contains date into LocalDate object.
   *
   * @param inputMessage is a string received from User
   * @return gives LocalDate object that contains date and time objects
   */
  public LocalDate getParsedDate(String inputMessage) {

    String trimmedString = inputMessage.trim();

    if (DATE_WEST_PATTERN.matcher(trimmedString).matches()) {
      return parseDate(trimmedString, DATE_WEST_FORMATTER);
    }

    if (DATE_RUS_PATTERN.matcher(trimmedString).matches()) {
      return parseDate(trimmedString, DATE_RUS_FORMATTER);
    }

    throw new UnparsableInputException("Date may not satisfy supported date formats.");
  }

  /**
   * Parses String object that contains time into LocalTime object.
   *
   * @param inputMessage is a string received from User
   * @return gives LocalTime object that contains date and time objects
   */
  public LocalTime getParsedTime(String inputMessage) {

    String trimmedString = inputMessage.trim();
    if (trimmedString.charAt(1) == ':') {
      trimmedString = "0" + trimmedString;
    }

    final LocalTime time = LocalTime.parse(trimmedString);
    return LocalTime.of(time.getHour(), time.getMinute());
  }

  /**
   * Parses String object that contains duration as minutes into long parameter.
   *
   * @param inputMessage is a string received from User
   * @return gives long parameter that contains long value of minutes
   */
  public long getParsedExpectationTime(String inputMessage) {
    try {
      long duration = Long.parseLong(inputMessage.trim());

      if (duration > 0) {
        return duration;
      }
      throw new UnparsableInputException("Duration value must be positive");
    } catch (NumberFormatException e) {
      log.debug("Exception caught", e);
      throw new UnparsableInputException("Duration must contain no non-number characters");
    }
  }

  /**
   * Validates string to a specific DD-MM-YYYY and YYYY-MM-DD Date pattern.
   *
   * @param inputString is a string received from User
   * @return true if input string validates pattern
   */
  public boolean validateDate(String inputString) {
    String trimmedString = inputString.trim();
    if (DATE_RUS_PATTERN.matcher(trimmedString).matches()
        || DATE_WEST_PATTERN.matcher(trimmedString).matches()) {
      LocalDate rideDate = getParsedDate(trimmedString);
      LocalDate today = LocalDate.now();
      if (rideDate.compareTo(today) < 0) {
        throw new WrongCommandException("You can't create rides in the past");
      }
      return true;
    }
    throw new UnparsableInputException("Date may not satisfy supported date formats.");
  }

  /**
   * Validates string to a specific 24 hours Time pattern.
   *
   * @param inputString is a string received from User
   * @return true if input string validates pattern
   */
  public boolean validateTime(String inputString, String rideDateString) {
    String trimmedString = inputString.trim();

    LocalDate rideDate = getParsedDate(rideDateString);
    LocalDate today = LocalDate.now();

    if (!TIME_24HRS_PATTERN.matcher(trimmedString).matches()) {
      throw new UnparsableInputException("Time may not satisfy supported date formats.");
    }

    if (rideDate.equals(today)) {
      LocalTime rideTime = getParsedTime(trimmedString);
      LocalTime now = LocalTime.now();
      if (rideTime.compareTo(now) < 0) {
        throw new WrongCommandException("You can't create rides in the past");
      }
    }

    return true;
  }

  /**
   * Validates string to a specific Coordinate pattern.
   *
   * @param inputString is a string received from User
   * @return true if input string validates pattern
   */
  public boolean validateCoordinate(String inputString) {

    String str = inputString.trim();
    String[] coordinates = str.split("\\s");

    if (coordinates.length != 2) {
      throw new UnparsableInputException("Incorrect input. The message should contain "
          + "only two consecutive values of latitude and longitude");
    }
    if (COORDINATE_PATTERN.matcher(coordinates[0]).matches() && COORDINATE_PATTERN
        .matcher(coordinates[1]).matches()) {
      float latitude = Float.parseFloat(coordinates[0]);
      float longitude = Float.parseFloat(coordinates[1]);
      if (latitude < -90.00 || latitude > 90.00) {
        throw new UnparsableInputException(
            "Incorrect latitude value. Please, enter latitude between -90 and 90 degrees.");
      }
      if (longitude < -180.00 || longitude > 180.00) {
        throw new UnparsableInputException(
            "Incorrect longitude value. Please, enter longitude between -180 and 180 degrees.");
      }
      return true;
    } else {
      throw new UnparsableInputException("Incorrect input.\n"
          + "Latitude and longitude should contain double values with up to 6 digits "
          + "in decimal part separated by space.\n"
          + "Example: \"12.345678 123.456789\"");
    }
  }

  private LocalDate parseDate(String string, DateTimeFormatter formatter) {
    final LocalDate date = LocalDate.parse(string, formatter);
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
  }
}
