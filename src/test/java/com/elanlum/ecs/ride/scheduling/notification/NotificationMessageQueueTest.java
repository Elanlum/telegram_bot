package com.elanlum.ecs.ride.scheduling.notification;

import com.elanlum.ecs.bot.TelegramBotEcs;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.notification.values.SimpleNotification;
import com.elanlum.ecs.user.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class NotificationMessageQueueTest {

  @Mock
  TelegramBotEcs botEcs;

  private User user = new User(null, "log", "name", "sididi", null);
  private Notification notification = new SimpleNotification(user, "user1");
  private NotificationMessageQueue notificationMessageQueue = new NotificationMessageQueue();

  @Test
  void getNotificationStream() {
    StepVerifier.create(notificationMessageQueue.getNotificationStream())
        .then(() -> notificationMessageQueue.sendNotification(notification))
        .assertNext(notification1 -> Assertions.assertThat(notification1.getMessage())
            .isEqualTo("user1"))
        .then(notificationMessageQueue::shutdown)
        .verifyComplete();

  }
}