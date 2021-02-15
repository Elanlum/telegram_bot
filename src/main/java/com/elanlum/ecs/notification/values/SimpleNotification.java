package com.elanlum.ecs.notification.values;

import com.elanlum.ecs.user.model.User;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SimpleNotification extends Notification implements Serializable {

  private String message;

  public SimpleNotification(User user, String message) {
    super(user);
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
