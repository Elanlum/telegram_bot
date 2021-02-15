package com.elanlum.ecs.user.service;

import com.elanlum.ecs.user.exceptions.InvalidTelegramChatIdUpdatingException;
import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.repository.UserRepository;
import com.elanlum.ecs.validation.ValidationForSave;
import com.elanlum.ecs.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepo;
  private final ValidationService<User> validationService;

  public Mono<User> findById(String id) {
    return userRepo.findById(id);
  }

  public Mono<User> findByTelegramId(String telegramId) {
    return userRepo.findByTelegramId(telegramId);
  }

  /**
   * Method validates User entity and saves it into repo.
   *
   * @param user represents User entity with model defined fields.
   */
  public Mono<User> save(User user) {
    return validationService.entityValidate(user, ValidationForSave.class)
        .flatMap(userRepo::save)
        .doOnNext(user1 -> log.debug("User with id {} was saved", user.getId()));
  }

  /**
   * Update field "telegramChatId" for concrete telegramId.
   *
   * @param userId is a telegramId of a user which telegramChatId we want to update
   * @param telegramChatId is a new telegramChatId
   * @return updated user
   */
  public Mono<User> updateTelegramChatId(String userId, Long telegramChatId) {
    if (userId == null) {
      return Mono.error(new InvalidTelegramChatIdUpdatingException("Telegram Id was null"));
    }
    if (telegramChatId == null) {
      return Mono.error(new InvalidTelegramChatIdUpdatingException("Telegram Chat Id was null"));
    }

    return userRepo.updateTelegramChatId(userId, telegramChatId)
        .doOnNext(user -> log.debug("TelegramId of the user with id {} was updated", user.getId()));
  }
}
