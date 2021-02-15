package com.elanlum.ecs.bot.context.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elanlum.ecs.bot.context.exceptions.UnparsableInputException;
import com.elanlum.ecs.bot.context.exceptions.WrongCommandException;
import com.elanlum.ecs.utils.TestCategory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Tag(TestCategory.UNIT)
class RideRequestFieldParserTest {

  private static RideRequestFieldParser parser = new RideRequestFieldParser();

  private static Stream<Arguments> createValidPosition() {
    return Stream.of(
        Arguments.of(40.433453f, parser
            .getParsedPosition(" 40.433453 43.945443").getLatitude()),
        Arguments.of(43.945443f, parser
            .getParsedPosition(" 40.433453 43.945443").getLongitude()),
        Arguments.of(44.4f, parser
            .getParsedPosition(" 44.4 43.943").getLatitude()),
        Arguments.of(43.943f, parser
            .getParsedPosition(" 44.43 43.943").getLongitude()),
        Arguments.of(-170.0434f, parser
            .getParsedPosition(" 44.43 -170.0434").getLongitude())
    );
  }

  private static Stream<Arguments> createValidPositionStrings() {
    return Stream.of(
        Arguments.of(" 40.433453 43.945443"),
        Arguments.of(" 40.433453 43.945443"),
        Arguments.of(" 44.4 43.943"),
        Arguments.of(" 44.43 43.943"),
        Arguments.of(" 44.43 -170.0434")
    );
  }

  private static Stream<Arguments> createInvalidPosition() {
    return Stream.of(
        Arguments.of(" 91.45543 -170.65465"),
        Arguments.of(" 45.43432 -190.9944"),
        Arguments.of(" 45.43432"),
        Arguments.of("0 10 52")
    );
  }

  private static Stream<Arguments> createValidDate() {
    return Stream.of(
        Arguments.of(2018, parser.getParsedDate(" 2018-11-01").getYear()),
        Arguments.of(11, parser.getParsedDate(" 2018-11-01").getMonthValue()),
        Arguments.of(1, parser.getParsedDate(" 2018-11-01").getDayOfMonth()),

        Arguments.of(2024, parser.getParsedDate("02-12-2024").getYear()),
        Arguments.of(12, parser.getParsedDate("02-12-2024").getMonthValue()),
        Arguments.of(2, parser.getParsedDate("02-12-2024").getDayOfMonth())
    );
  }

  private static Stream<Arguments> createValidDateStrings() {
    return Stream.of(
        Arguments.of(" 2200-11-01"),
        Arguments.of(" 2200-11-01"),
        Arguments.of(" 2200-11-01"),

        Arguments.of("02-12-2200"),
        Arguments.of("02-12-2200"),
        Arguments.of("02-12-2200")
    );
  }

  private static Stream<Arguments> createInvalidDate() {
    return Stream.of(
        Arguments.of("32-12-2000 "),
        Arguments.of("31-13-2000 "),
        Arguments.of("31-12-1800 "),
        Arguments.of("29-02-2019 ")
    );
  }

  private static Stream<Arguments> createValidTime() {
    return Stream.of(
        Arguments.of(4, parser.getParsedTime("4:20").getHour()),
        Arguments.of(14, parser.getParsedTime("14:01").getHour()),
        Arguments.of(20, parser.getParsedTime("4:20").getMinute()),
        Arguments.of(1, parser.getParsedTime("14:01").getMinute())
    );
  }

  private static Stream<Arguments> createValidTimeStrings() {
    return Stream.of(
        Arguments.of("4:20"),
        Arguments.of("14:01"),
        Arguments.of("4:20"),
        Arguments.of("14:01")
    );
  }

  private static Stream<Arguments> createInvalidTime() {
    return Stream.of(Arguments.of("31-12-2000 24:51"), Arguments.of("31-12-2000 22:61"));
  }

  private static Stream<Arguments> createValidDuration() {
    return Stream.of(
        Arguments.of(160L, parser.getParsedExpectationTime("160")),
        Arguments.of(40L, parser.getParsedExpectationTime("40")),
        Arguments.of(99L, parser.getParsedExpectationTime("99"))
    );
  }

  private static Stream<Arguments> createInvalidDuration() {
    return Stream.of(
        Arguments.of("-1"),
        Arguments.of(".10"),
        Arguments.of("1o1"),
        Arguments.of("1 1"),
        Arguments.of("non-number")
    );
  }

  @ParameterizedTest
  @MethodSource("createValidPosition")
  void checkValidPositionArguments(float parameter, float actual) {
    assertEquals(parameter, actual);
  }

  @ParameterizedTest
  @MethodSource("createInvalidPosition")
  void checkInvalidPositionArguments(String str) {
    assertThrows(UnparsableInputException.class, () -> parser.validateCoordinate(str));
  }

  @ParameterizedTest
  @MethodSource("createValidDate")
  void checkValidDate(int parameter, int actual) {
    assertEquals(parameter, actual);
  }

  @ParameterizedTest
  @MethodSource("createInvalidDate")
  void checkInvalidDate(String s) {
    assertThrows(UnparsableInputException.class, () -> parser.getParsedDate(s));
  }

  @ParameterizedTest
  @MethodSource("createValidTime")
  void checkValidTime(int parameter, int actual) {
    assertEquals(parameter, actual);
  }

  @ParameterizedTest
  @MethodSource("createInvalidTime")
  void checkInvalidTime(String s) {
    assertThrows(UnparsableInputException.class, () -> parser.validateTime(s, "01-01-2200"));
  }

  @ParameterizedTest
  @MethodSource("createValidDuration")
  void checkValidDuration(long parameter, long actual) {
    assertEquals(parameter, actual);
  }

  @ParameterizedTest
  @MethodSource("createInvalidDuration")
  void checkInvalidDuration(String s) {
    assertThrows(UnparsableInputException.class, () -> parser.getParsedExpectationTime(s));
  }

  @ParameterizedTest
  @MethodSource("createValidDateStrings")
  void validateValidDate(String date) {
    assertTrue(parser.validateDate(date));
  }

  @ParameterizedTest
  @MethodSource("createInvalidDate")
  void validateInvalidDates(String date) {
    assertThrows(UnparsableInputException.class, () -> parser.validateDate(date));
  }

  @ParameterizedTest
  @MethodSource("createValidTimeStrings")
  void validateValidTime(String time) {
    assertTrue(parser.validateTime(time, "01-01-2200"));
  }

  @Test
  void givenMoreThanTwoCoordinates_validateCoordinate_throwsException() {
    assertThrows(UnparsableInputException.class,
        () -> parser.validateCoordinate(" 40.433453 43.945443 -170.0434"));
  }

  @ParameterizedTest
  @MethodSource("createValidPositionStrings")
  void validateValidPositions(String coordinate) {
    assertTrue(parser.validateCoordinate(coordinate));
  }

  @Test
  void validateInvalidCoordinates() {
    assertThrows(UnparsableInputException.class,
        () -> parser.validateCoordinate("invalid coordinate"));
  }

  @Test
  void passingDateBeforeCurrentThrows_Exception() {
    assertThrows(WrongCommandException.class,
        () -> parser.validateDate("27-01-1944"));
  }

  @Test
  void givenCurrentDate_passingTimeBeforeCurrent_throwsException() {
    assertThrows(WrongCommandException.class,
        () -> parser.validateTime(LocalTime.now().minus(1, ChronoUnit.MINUTES)
                .format(DateTimeFormatter.ofPattern("HH:mm")),
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"))));
  }
}