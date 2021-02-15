package com.elanlum.ecs.user.model;

import com.elanlum.ecs.validation.ValidationForSave;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter(AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "user")
public class User implements Serializable {

  @Null(groups = ValidationForSave.class, message = "User id should be null. ")
  @NotNull(groups = Default.class, message = "User id should not be null. ")
  @Id
  String id;

  @NotNull(groups = ValidationForSave.class, message = "Login should not be null. ")
  @Size(min = 2, max = 32, groups = ValidationForSave.class,
      message = "Login size should be between 2 and 32 symbols. ")
  @Field
  String login;

  @NotNull(groups = ValidationForSave.class, message = "Name should not be null. ")
  @Size(min = 2, max = 32, groups = ValidationForSave.class,
      message = "Name size should be between 2 and 32 symbols. ")
  @Field
  String name;

  @Field
  String telegramId;

  @Field
  Long telegramChatId;
}
