package com.elanlum.ecs.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

  @Mock
  private ReactiveMongoTemplate template;
  @InjectMocks
  private UserRepository userRepo;

  @Test
  void saveTest() {
    User user1 = new User(
        null, "testLogin", "testName", "1", null);
    when(template.save(user1)).thenReturn(Mono.just(user1));
    Mono<User> monoUser1 = userRepo.save(user1);
    assertEquals(monoUser1.block().getLogin(), "testLogin");
    verify(template, times(1)).save(user1);
  }

  @Test
  void findByIdTest() {
    User user2 = new User(
        "1", "testLogin1", "testName1", "1", null);
    when(template.findById("1", User.class)).thenReturn(Mono.just(user2));
    Mono<User> monoUser2 = userRepo.findById("1");
    assertEquals(monoUser2.block().getLogin(), "testLogin1");
    verify(template, times(1)).findById("1", User.class);
  }

  @Test
  void findByTelegramIdTest() {
    User user = new User("1", "testLogin1", "testName1", "1", null);
    doReturn(Flux.just(user)).when(template)
        .find(Query.query(Criteria.where("telegramId").is("1")), User.class);
    Mono<User> monoUser = userRepo.findByTelegramId("1");
    assertEquals("testLogin1", monoUser.block().getLogin());
    verify(template, times(1)).find(Query.query(Criteria.where("telegramId").is("1")), User.class);
  }

  @Test
  void updateByTelegramIdTest() {
    User user = new User("1", "testLogin1", "testName1", "1", 1L);

    when(template.findAndModify(eq(
        Query.query(Criteria.where("_id").is("1"))),
        eq(Update.update("telegramChatId", 1L)),
        any(FindAndModifyOptions.class), eq(User.class))).thenReturn(Mono.just(user));

    StepVerifier.create(userRepo.updateTelegramChatId("1", 1L))
        .assertNext(user1 -> assertEquals(user1.getTelegramChatId(), Long.valueOf(1L)))
        .verifyComplete();
    verify(template, times(1))
        .findAndModify(any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(User.class));
    verifyNoMoreInteractions(template);
  }

}
