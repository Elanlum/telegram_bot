package com.elanlum.ecs.bot.handler.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class KeyValueStorageRepo {

  private final Map<String, String> keyValueMap = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  /**
   * Save a key and a value to a storage.
   *
   * @param key is an unique string
   * @param value can contain any type of value
   * @param <T> is a type of value
   * @return a previous value from tne storage
   */
  public <T> Mono<T> save(@Nonnull String key, @Nonnull T value) {
    return Mono.fromSupplier(() -> writeValue(value))
        .flatMap(value1 -> value1)
        .flatMap(value1 -> {
          Mono<String> savedMono = Mono.justOrEmpty(keyValueMap.put(key, value1));
          log.debug("A pair {}/{} was saved", key, value1);
          return savedMono;
        })
        .flatMap(value1 -> readValue(value1, value.getClass()));
  }

  /**
   * Find a value by its key.
   *
   * @param key is an unique string
   * @param <T> is a type of value
   * @return a value for this key from tne storage
   */
  public <T> Mono<T> findByKey(@Nonnull String key, Class<T> valueType) {
    return Mono.fromCallable(() -> Mono.justOrEmpty(keyValueMap.get(key)))
        .flatMap(value -> value)
        .flatMap(value -> readValue(value, valueType));
  }

  /**
   * Delete a value by its key.
   *
   * @param key is an unique string
   * @return true if delete operation is successful
   */
  public Mono<Boolean> delete(@Nonnull String key) {
    return Mono.fromSupplier(() -> keyValueMap.remove(key))
        .map(Objects::nonNull)
        .defaultIfEmpty(false);
  }

  private <T> Mono<T> readValue(@Nonnull String value, Class<?> valueType) {
    try {
      JavaType javaType = objectMapper.getTypeFactory().constructType(valueType);
      return Mono.just(objectMapper.readValue(value, javaType));
    } catch (IOException e) {
      log.error("Error occurred ", e);
      return Mono.error(e);
    }
  }

  private <T> Mono<String> writeValue(@Nonnull T value) {
    try {
      return Mono.justOrEmpty(objectMapper.writeValueAsString(value));
    } catch (IOException e) {
      log.error("Error occurred ", e);
      return Mono.error(e);
    }
  }
}
