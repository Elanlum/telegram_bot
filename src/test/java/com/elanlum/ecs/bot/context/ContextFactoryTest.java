package com.elanlum.ecs.bot.context;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.UserContext;
import com.elanlum.ecs.utils.TestCategory;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class ContextFactoryTest {

  ContextFactory contextFactory = new ContextFactory();

  @Test
  void givenTelegramIdAndDriverContextType_createUserContext_shouldReturnUserContext() {
    UserContext userContext = contextFactory
        .createUserContext("1", ContextType.CREATE_DRIVER_REQUEST);
    Map<FieldName, String> fields = userContext.getFieldsToFill();
    assertEquals(ContextType.CREATE_DRIVER_REQUEST, userContext.getContextType());
    assertEquals("1", fields.get(FieldName.TELEGRAM_ID));
    assertEquals("driver", fields.get(FieldName.ROLE));
  }

  @Test
  void givenTelegramIdAndPassengerContextType_createUserContext_shouldReturnUserContext() {
    UserContext userContext = contextFactory
        .createUserContext("1", ContextType.CREATE_PASSENGER_REQUEST);
    Map<FieldName, String> fields = userContext.getFieldsToFill();
    assertEquals(ContextType.CREATE_PASSENGER_REQUEST, userContext.getContextType());
    assertEquals("1", fields.get(FieldName.TELEGRAM_ID));
    assertEquals("passenger", fields.get(FieldName.ROLE));
  }
}