package com.elanlum.ecs.bot.button;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.utils.TestCategory;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
class StringParserTest {

  private static Stream<Arguments> createInvalidString() {
    return Stream.of(
        Arguments.of("button_name:rideId"),
        Arguments.of("button_name:"),
        Arguments.of("button_name"),
        Arguments.of(""),
        Arguments.of("button_name:rideId:1111:2222")
    );
  }

  @ParameterizedTest
  @MethodSource("createInvalidString")
  @DisplayName("Create invalid string")
  void givenInvalidStringReturnsMonoEmpty(String input) {
    StringParser stringParser = new StringParser();

    StepVerifier.create(stringParser.parse(input))
        .verifyComplete();
  }

  @Test
  @DisplayName("Parse of valid string results Mono")
  void givenValidStringReturnsMonoButtonCallback() {
    StringParser stringParser = new StringParser();
    String stringForParse = "button_name:rideId:rideUserId";

    StepVerifier.create(stringParser.parse(stringForParse))
        .assertNext(buttonCallback -> {
          assertEquals("button_name", buttonCallback.getCommand());
          assertEquals("rideId", buttonCallback.getRideId());
          assertEquals("rideUserId", buttonCallback.getRideUserId());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Combine method creates string with delimiter")
  void combineReturnsStringWithDelimiter() {
    StringParser stringParser = new StringParser();
    assertEquals("first:second:third",
        stringParser.combine(new ButtonCallback("first", "second", "third")));
  }
}