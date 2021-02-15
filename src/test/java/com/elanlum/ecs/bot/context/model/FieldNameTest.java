package com.elanlum.ecs.bot.context.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class FieldNameTest {

  @ParameterizedTest
  @EnumSource(value = FieldName.class, mode = Mode.EXCLUDE, names = {"TELEGRAM_ID",
      "ROLE"})
  void getInfo_returnsInfo(FieldName fieldName) {
    int state = fieldName.getState();
    switch (state) {
      case 1:
        assertTrue(
            FieldName.getInfo(fieldName.getState()).contains("Please enter your departure point."));
        break;
      case 2:
        assertTrue(FieldName.getInfo(fieldName.getState())
            .contains("Please enter your destination point."));
        break;
      case 3:
        assertTrue(
            FieldName.getInfo(fieldName.getState()).contains("Please enter your ride date."));
        break;
      case 4:
        assertTrue(
            FieldName.getInfo(fieldName.getState()).contains("Please enter your ride time."));
        break;
      case 5:
        assertTrue(FieldName.getInfo(fieldName.getState())
            .contains("Please enter an expectation period."));
        break;
      default:
    }
  }

  @Test
  void givenInvalidState_getInfo_throwsException() {
    assertThrows(UnsupportedOperationException.class, () -> FieldName.getInfo(0));
  }
}