package com.elanlum.ecs.bot.handler;

import com.elanlum.ecs.bot.handler.repository.KeyValueStorageRepo;
import com.elanlum.ecs.bot.context.ContextFactory;
import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.UserContext;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
@RequiredArgsConstructor
public class GeneralContextCommandsHandler {

  private final ContextFactory contextFactory;
  private final KeyValueStorageRepo keyValueStorageRepo;
  private final RideRequestCreationHandler rideRequestCreationHandler;

  /**
   * Method checks KVStorage for existent user's context, creates it if needed. If there is a
   * context, corresponding handler is invoked to edit it.
   *
   * @param update is passed from {@link BotCommandsHandler} and contains
   *     user's message, telegramId, etc.
   * @return Set of available commands which depends on current userContext state
   */
  public Mono<Set<String>> processCommand(Update update) {

    String telegramId = String.valueOf(update.getMessage().getFrom().getId());
    String command = update.getMessage().getText();

    return getContext(telegramId, command)
        .flatMap(userContext -> rideRequestCreationHandler.handle(userContext, update))
        .flatMap(userContext -> Mono
            .zip(keyValueStorageRepo.save(telegramId, userContext)
                    .map(context -> true)
                    .defaultIfEmpty(false),
                Mono.just(userContext)))
        .map(Tuple2::getT2)
        .flatMap(userContext -> {
          if (userContext.isCompleted() || userContext.isCanceled()) {
            return
                Mono.zip(Mono.just(userContext), keyValueStorageRepo.delete(telegramId));
          }
          return Mono.just(Tuples.of(userContext, false));
        })
        .map(userContextAndWasDeleted -> {
          UserContext userContext = userContextAndWasDeleted.getT1();
          Set<String> commands = userContext.getAvailableCommands();
          if (userContext.getState() != 0) {
            commands.clear();
            commands.add(FieldName.getInfo(userContext.getState()));
          }
          if (userContextAndWasDeleted.getT2()) {
            commands.clear();
            commands.add("Ride request creation is finished");
          }
          return userContext.getAvailableCommands();
        });
  }

  private Mono<UserContext> getContext(String telegramId, String command) {
    return keyValueStorageRepo.findByKey(telegramId, UserContext.class)
        .map(userContext -> userContext)
        .switchIfEmpty(
            Mono.create(sink -> {
                  UserContext newUserContext = null;
                  for (ContextType contextType : ContextType.values()) {
                    if (command.equals(contextType.getCommandName())) {
                      newUserContext = contextFactory.createUserContext(telegramId, contextType);
                      break;
                    }
                  }
                  sink.success(newUserContext);
                }
            ));
  }
}
