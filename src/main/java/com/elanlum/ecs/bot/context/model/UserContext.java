package com.elanlum.ecs.bot.context.model;

import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserContext {

  @Setter(AccessLevel.PROTECTED)
  private ContextType contextType;
  @Setter(AccessLevel.PROTECTED)
  private Map<FieldName, String> fieldsToFill;

  @Autowired
  public UserContext(ContextType contextType, Map<FieldName, String> fieldsToFill) {
    this.contextType = contextType;
    this.fieldsToFill = fieldsToFill;
  }

  @Setter
  private Set<String> availableCommands;
  @Setter
  private int state;
  @Setter
  private boolean completed;
  @Setter
  private boolean canceled;
}
