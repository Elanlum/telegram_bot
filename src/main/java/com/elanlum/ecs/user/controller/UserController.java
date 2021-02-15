package com.elanlum.ecs.user.controller;

import com.elanlum.ecs.user.model.User;
import com.elanlum.ecs.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping(path = "/{id}")
  Mono<User> findById(@PathVariable String id) {
    return userService.findById(id);
  }

  @GetMapping(path = "/telegramId/{telegramId}")
  Mono<User> findByTelegramId(@PathVariable String telegramId) {
    return userService.findByTelegramId(telegramId);
  }

  @PostMapping("/save")
  @ResponseStatus(HttpStatus.CREATED)
  Mono<User> save(@RequestBody User user) {
    return userService.save(user);
  }
}
