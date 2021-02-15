package com.elanlum.ecs.bot.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.bot.context.ContextFactory;
import com.elanlum.ecs.bot.context.model.ContextType;
import com.elanlum.ecs.bot.context.model.FieldName;
import com.elanlum.ecs.bot.context.model.UserContext;
import com.elanlum.ecs.bot.handler.repository.KeyValueStorageRepo;
import com.elanlum.ecs.utils.TestCategory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class GeneralContextCommandsHandlerTest {

  private static final String CREATE_DRR_COMMAND = "Create driver request";
  private static final String SOME_COMMAND = "/someCommand";

  @Mock
  ContextFactory contextFactory;
  @Mock
  KeyValueStorageRepo kvStorage;
  @Mock
  RideRequestCreationHandler rideRequestCreationHandler;
  @InjectMocks
  GeneralContextCommandsHandler generalHandler;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  Update update;

  @Test
  void whenNoContext_processCommand_handlesNewContext() {
    Map<FieldName, String> newContextFields = new HashMap<>();
    newContextFields.put(FieldName.TELEGRAM_ID, "1");
    newContextFields.put(FieldName.ROLE, "driver");

    UserContext newUserContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST,
        newContextFields);

    Set<String> newContextCommands = new HashSet<>();
    newContextCommands.add("newContextCommand");
    newUserContext.setAvailableCommands(newContextCommands);

    Set<String> handledContextCommands = new HashSet<>();
    handledContextCommands.add("filledCommand1");
    handledContextCommands.add("filledCommand2");
    handledContextCommands.add("filledCommand3");
    UserContext handledContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST,
        newContextFields);
    handledContext.setAvailableCommands(handledContextCommands);

    when(update.getMessage().getFrom().getId()).thenReturn(1);
    when(update.getMessage().getText()).thenReturn(CREATE_DRR_COMMAND);
    doReturn(Mono.empty()).when(kvStorage).findByKey("1", UserContext.class);
    doReturn(newUserContext).when(contextFactory)
        .createUserContext("1", ContextType.CREATE_DRIVER_REQUEST);
    doReturn(Mono.just(newUserContext)).when(kvStorage)
        .save(eq("1"), any(UserContext.class));
    doReturn(Mono.just(handledContext)).when(rideRequestCreationHandler)
        .handle(newUserContext, update);

    ArgumentCaptor<UserContext> contextCaptor = ArgumentCaptor.forClass(UserContext.class);

    StepVerifier.create(generalHandler.processCommand(update))
        .expectNextMatches(testSet -> {
          assertTrue(testSet.contains("filledCommand1"));
          assertTrue(testSet.contains("filledCommand2"));
          assertTrue(testSet.contains("filledCommand3"));
          return true;
        })
        .expectComplete()
        .verify();
    verify(kvStorage).save(eq("1"), contextCaptor.capture());
    verify(kvStorage, times(1)).save(any(), any());
    assertEquals("1", contextCaptor.getValue().getFieldsToFill().get(FieldName.TELEGRAM_ID));
  }

  @Test
  void whenKvStorageHasContext_processCommand_returnsHandledContext() {
    Map<FieldName, String> kvContextMap = new HashMap<>();
    kvContextMap.put(FieldName.TELEGRAM_ID, "1");
    kvContextMap.put(FieldName.ROLE, "driver");

    UserContext kvUserContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST, kvContextMap);

    Set<String> contextCommands = new HashSet<>();
    contextCommands.add("newContextCommand1");
    kvUserContext.setAvailableCommands(contextCommands);

    Map<FieldName, String> updatedContextMap = new HashMap<>();
    updatedContextMap.put(FieldName.TELEGRAM_ID, "1");
    updatedContextMap.put(FieldName.ROLE, "driver");
    updatedContextMap.put(FieldName.EXPECTATION_PERIOD, "15");

    UserContext updatedUserContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST,
        updatedContextMap);

    Set<String> updatedCommands = new HashSet<>();
    updatedCommands.add("newContextCommand2");
    kvUserContext.setAvailableCommands(updatedCommands);
    updatedUserContext.setAvailableCommands(updatedCommands);

    when(update.getMessage().getFrom().getId()).thenReturn(1);
    when(update.getMessage().getText()).thenReturn(SOME_COMMAND);
    doReturn(Mono.just(kvUserContext)).when(kvStorage).findByKey("1", UserContext.class);
    doReturn(Mono.just(updatedUserContext)).when(rideRequestCreationHandler)
        .handle(kvUserContext, update);
    doReturn(Mono.just(kvUserContext)).when(kvStorage)
        .save(eq("1"), any(UserContext.class));

    ArgumentCaptor<UserContext> contextCaptor = ArgumentCaptor.forClass(UserContext.class);

    StepVerifier.create(generalHandler.processCommand(update))
        .expectNextMatches(testSet -> {
          assertTrue(testSet.contains("newContextCommand2"));
          return true;
        })
        .expectComplete()
        .verify();
    verify(kvStorage).save(eq("1"), contextCaptor.capture());
    verify(kvStorage, times(1)).save(any(), any());
    assertEquals("1",
        contextCaptor.getValue().getFieldsToFill().get(FieldName.TELEGRAM_ID));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2})
  void whenKvStorageContextIsFinished_processCommand_deletesFromKvStorage(int mode) {
    Map<FieldName, String> kvContextMap = new HashMap<>();
    kvContextMap.put(FieldName.TELEGRAM_ID, "1");

    final UserContext kvUserContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST,
        kvContextMap);
    final UserContext updatedContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST,
        kvContextMap);
    if (mode == 1) {
      updatedContext.setCanceled(true);
    } else {
      updatedContext.setCompleted(true);
    }
    updatedContext.setAvailableCommands(new HashSet<>());

    when(update.getMessage().getFrom().getId()).thenReturn(1);
    when(update.getMessage().getText()).thenReturn(SOME_COMMAND);
    doReturn(Mono.just(kvUserContext)).when(kvStorage).findByKey("1", UserContext.class);
    doReturn(Mono.just(updatedContext)).when(rideRequestCreationHandler)
        .handle(kvUserContext, update);
    doReturn(Mono.just(kvUserContext)).when(kvStorage).save(eq("1"), any(UserContext.class));
    doReturn(Mono.just(true)).when(kvStorage)
        .delete(kvContextMap.get(FieldName.TELEGRAM_ID));

    StepVerifier.create(generalHandler.processCommand(update))
        .expectNextMatches(set -> {
          assertNotNull(set);
          return true;
        })
        .expectComplete()
        .verify();
    verify(kvStorage, times(1)).delete("1");
    verify(kvStorage, times(1)).save(any(), any());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5})
  void givenUserContextNotZero_processCommand_returnsInfo(int state) {
    Map<FieldName, String> kvContextMap = new HashMap<>();
    kvContextMap.put(FieldName.TELEGRAM_ID, "1");

    UserContext kvUserContext = new UserContext(ContextType.CREATE_DRIVER_REQUEST, kvContextMap);
    kvUserContext.setAvailableCommands(new HashSet<>());
    kvUserContext.setState(state);

    when(update.getMessage().getFrom().getId()).thenReturn(1);
    when(update.getMessage().getText()).thenReturn(SOME_COMMAND);
    doReturn(Mono.just(kvUserContext)).when(rideRequestCreationHandler)
        .handle(kvUserContext, update);
    doReturn(Mono.just(kvUserContext)).when(kvStorage).findByKey("1", UserContext.class);
    doReturn(Mono.just(kvUserContext)).when(kvStorage).save(eq("1"), any(UserContext.class));

    StepVerifier.create(generalHandler.processCommand(update))
        .assertNext(set -> assertTrue(set.contains(FieldName.getInfo(state))))
        .verifyComplete();
  }
}