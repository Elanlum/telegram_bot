package com.elanlum.ecs.user.repository;

import com.elanlum.ecs.user.model.User;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepository {

  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<User> save(User user) {
    return reactiveMongoTemplate.save(user);
  }

  public Mono<User> findById(String id) {
    return reactiveMongoTemplate.findById(id, User.class);
  }

  public Mono<User> findByTelegramId(String telegramId) {
    return Mono.from(reactiveMongoTemplate
        .find(Query.query(Criteria.where("telegramId").is(telegramId)), User.class));
  }

  /**
   * Update field "telegramChatId" for concrete telegramId. {@link FindAndModifyOptions} allows
   * return updated entity. Without this parameter method returns entity before updating.
   *
   * @param userId is an id of our user which telegramChatId we want to update
   * @param telegramChatId is a new telegramChatId
   * @return updated user
   */
  public Mono<User> updateTelegramChatId(@Nonnull String userId,
      @Nonnull Long telegramChatId) {
    return reactiveMongoTemplate.findAndModify(
        Query.query(Criteria.where("_id").is(userId)),
        Update.update("telegramChatId", telegramChatId),
        new FindAndModifyOptions().returnNew(true), User.class);
  }
}
