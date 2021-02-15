package com.elanlum.ecs;

import com.elanlum.ecs.bot.TelegramBotEcs;
import com.elanlum.ecs.bot.config.TelegramBotConfig;
import com.elanlum.ecs.bot.notification.NotificationService;
import com.elanlum.ecs.ride.scheduling.config.NotificationConfiguration;
import com.elanlum.ecs.ride.scheduling.notifying.QuartzNotificationScheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@TestConfiguration
@ComponentScan(basePackages = {"com.elanlum.ecs.user", "com.elanlum.ecs.validation", "com.elanlum.ecs.ride",
    "com.elanlum.ecs.bot", "com.elanlum.ecs.notification", "com.elanlum.ecs.map"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        value = {QuartzNotificationScheduler.class, NotificationService.class,
            TelegramBotEcs.class, TelegramBotConfig.class}))
@Import({com.elanlum.ecs.TestConfiguration.class, NotificationConfiguration.class,
    MongoReactiveAutoConfiguration.class, MongoReactiveDataAutoConfiguration.class,
    EmbeddedMongoAutoConfiguration.class, ObjectMapper.class,
    ValidationAutoConfiguration.class})
public class IntegrationTestsConfig {

}
