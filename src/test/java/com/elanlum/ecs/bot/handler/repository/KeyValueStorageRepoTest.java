package com.elanlum.ecs.bot.handler.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.utils.TestCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class KeyValueStorageRepoTest {


  private ObjectMapper mockMapper = mock(ObjectMapper.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  private KeyValueStorageRepo keyValueStorageRepo = new KeyValueStorageRepo(objectMapper);
  private KeyValueStorageRepo keyValueStorageRepoWithMock =
      new KeyValueStorageRepo(mockMapper);

  @BeforeEach
  public void setUpFactory() {
    doReturn(objectMapper.getTypeFactory()).when(mockMapper).getTypeFactory();
  }

  @Test
  void saveForTheFirstTime() {
    StepVerifier.create(keyValueStorageRepo.save("1", "testtest"))
        .verifyComplete();
  }

  @Test
  void update() {
    keyValueStorageRepo.save("2", "firstValue").block();
    StepVerifier.create(keyValueStorageRepo.save("2", "secondValue"))
        .assertNext(s -> assertEquals("firstValue", s))
        .verifyComplete();
  }

  @Test
  void whenRightKey_returnValue() {
    StepVerifier.create(keyValueStorageRepo.save("3", "testtest"))
        .verifyComplete();
    StepVerifier.create(keyValueStorageRepo.findByKey("3", String.class))
        .assertNext(s -> assertEquals("testtest", s))
        .verifyComplete();
  }

  @Test
  void whenWrongKey_returnEmptyMono() {
    StepVerifier.create(keyValueStorageRepo.findByKey("100", String.class))
        .verifyComplete();
    StepVerifier.create(keyValueStorageRepo.findByKey("0", String.class))
        .verifyComplete();
    StepVerifier.create(keyValueStorageRepo.findByKey("wrongKey", String.class))
        .verifyComplete();
  }


  @Test
  void whenWrongValue_throwsException() throws IOException {

    doReturn("firstly").doReturn("secondly").doThrow(new JsonProcessingException("") {
    }).when(mockMapper).writeValueAsString(any(Position.class));

    doThrow(new JsonProcessingException("") {
    }).when(mockMapper).readValue(anyString(), any(JavaType.class));

    StepVerifier.create(keyValueStorageRepoWithMock
        .save("superKey", new Position(2.0f, 3.02f)))
        .verifyComplete();

    StepVerifier.create(keyValueStorageRepoWithMock
        .save("superKey", new Position(2.0f, 3.05f)))
        .verifyError(JsonProcessingException.class);

    StepVerifier.create(keyValueStorageRepoWithMock
        .save("superKey", new Position(2.9f, 3.06f)))
        .verifyError(JsonProcessingException.class);
  }

  @Test
  void whenDelete_returnEmptyMono() {
    keyValueStorageRepo.save("2", "firstValue").block();

    StepVerifier.create(keyValueStorageRepo.delete("3"))
        .assertNext(actual -> Assertions.assertFalse(actual))
        .expectComplete().verify();

    StepVerifier.create(keyValueStorageRepo.delete("2"))
        .assertNext(actual -> Assertions.assertTrue(actual))
        .expectComplete().verify();
  }
}