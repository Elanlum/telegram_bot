package com.elanlum.ecs.validation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService<T> {

  private final Validator validator;

  /**
   * This method look on annotations on field of entity's class and validate it.
   *
   * @param entity - param that method will validate
   * @param defaultValidation - group name for validation cases
   * @return {@link Mono} that contains entity or error {@link ConstraintViolationException}
   */
  public Mono<T> entityValidate(T entity, Class defaultValidation) {
    return Mono.fromCallable(() -> validator.validate(entity, defaultValidation))
        .map(constraintViolationSet -> {
          if (CollectionUtils.isNotEmpty(constraintViolationSet)) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : constraintViolationSet) {
              sb.append(violation.getMessage());
            }
            log.debug(sb.toString());
            throw new ConstraintViolationException(sb.toString(), constraintViolationSet);
          }
          return entity;
        }).subscribeOn(Schedulers.elastic());
  }
}
