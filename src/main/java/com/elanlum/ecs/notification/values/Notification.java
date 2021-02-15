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
public abstract class Notification implements Serializable {

  private User user;

  public abstract String getMessage();
}
