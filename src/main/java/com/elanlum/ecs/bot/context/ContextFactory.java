package com.elanlum.ecs.bot.context;

import static com.elanlum.ecs.bot.util.ConstantStorage.cancelCommand;

import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.UserContext;

import com.elanlum.ecs.bot.util.ConstantStorage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ContextFactory {

  /**
   * Creates UserContext instance of certain ContextType.
   *
   * @param telegramId is a telegram id of a user who is trying to create a context
   * @param contextType is a type of context which is created
   * @return newly created {@link UserContext}
   */
  public UserContext createUserContext(String telegramId, ContextType contextType) {
    Map<FieldName, String> fieldsToFill = new HashMap<>();
    fieldsToFill.put(FieldName.ROLE, contextType.getRole());
    fieldsToFill.put(FieldName.TELEGRAM_ID, telegramId);

    UserContext userContext = new UserContext(contextType, fieldsToFill);

    Set<String> availableCommands = new HashSet<>();
    availableCommands.add(cancelCommand);

    userContext.setAvailableCommands(availableCommands);
    return userContext;
  }
}
