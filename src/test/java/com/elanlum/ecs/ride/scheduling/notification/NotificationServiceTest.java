package com.elanlum.ecs.ride.scheduling.notification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elanlum.ecs.bot.TelegramBotEcs;
import com.elanlum.ecs.bot.button.BotInlineMarkup;
import com.elanlum.ecs.bot.notification.NotificationService;
import com.elanlum.ecs.utils.TestCategory;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.notification.values.NotificationRecipient;
import com.elanlum.ecs.notification.values.RideMatchingNotification;
import com.elanlum.ecs.notification.values.SimpleNotification;
import com.elanlum.ecs.ride.model.common.DriverRideRequest;
import com.elanlum.ecs.ride.model.common.PassengerRideRequest;
import com.elanlum.ecs.ride.model.common.Ride;
import com.elanlum.ecs.ride.model.values.Interval;
import com.elanlum.ecs.ride.model.values.Position;
import com.elanlum.ecs.ride.model.values.RideRequestStatus;
import com.elanlum.ecs.user.model.User;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

@Tag(TestCategory.UNIT)
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  NotificationMessageQueue notificationMessageQueue;
  @Mock
  private TelegramBotEcs botEcs;
  @Mock
  private BotInlineMarkup botInlineMarkup;

  @Test
  void notificationServiceConstructor() throws TelegramApiException {
    var user = new User(null, "log", "name", "sididi", 1L);
    var notification = new SimpleNotification(user, "user1");
    doReturn(Flux.just(notification)).when(notificationMessageQueue).getNotificationStream();
    NotificationService service = new NotificationService(notificationMessageQueue, botEcs,
        botInlineMarkup);
    StepVerifier.create(notificationMessageQueue.getNotificationStream())
        .assertNext(notification1 -> Assertions.assertThat(notification1.getMessage())
            .isEqualTo("user1"))
        .then(notificationMessageQueue::shutdown)
        .verifyComplete();
    verify(botEcs, times(1)).execute(any(SendMessage.class));
  }

  @Test
  void whenChatIdIsNull_ThenNotificationShouldNotBeSent() throws TelegramApiException {
    var user = new User(null, "log", "name", null, null);
    var notification = new SimpleNotification(user, "user1");
    var notification1 = new SimpleNotification(null, "user1");
    ;
    Flux<SimpleNotification> notificationFlux = Flux.just(notification, notification1)
        .publish()
        .autoConnect(2);
    PublisherProbe<Notification> publisherProbe = PublisherProbe.of(notificationFlux);
    doReturn(publisherProbe.flux()).when(notificationMessageQueue)
        .getNotificationStream();

    new NotificationService(notificationMessageQueue, botEcs, botInlineMarkup);
    publisherProbe.assertWasSubscribed();

    StepVerifier.create(notificationFlux)
        .expectNextCount(2)
        .thenAwait(Duration.ofSeconds(2))
        .verifyComplete();

    verify(botEcs, never()).execute(any(SendMessage.class));
  }

  @Test
  void sendMessageToUserFailed() throws TelegramApiException {
    var user = new User(null, "log", "name", "sididi", 1L);
    var notification = new SimpleNotification(user, "user1");
    doReturn(Flux.just(notification, notification)).when(notificationMessageQueue)
        .getNotificationStream();

    Semaphore semaphore = new Semaphore(-1);
    when(botEcs.execute(any(SendMessage.class)))
        .thenAnswer(invocation -> {
          semaphore.release();
          throw new TelegramApiException();
        })
        .thenAnswer(invocation -> {
          semaphore.release();
          return new Serializable[1];
        });
    new NotificationService(notificationMessageQueue, botEcs, botInlineMarkup);
    org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2),
        (Executable) semaphore::acquire);

    verify(botEcs, times(2)).execute(any(SendMessage.class));
  }

  @Test
  void whenRideMathcingNotification_ThenSendItWithMention4Driver() throws TelegramApiException {
    var user = new User("1", "log", "name", "420", 1L);
    var userCompanion = new User("2", "log1", "name1", "228", 1L);

    DriverRideRequest driverRequest = new DriverRideRequest(
        "1", "1",
        new Interval(LocalDateTime.of(2050, 12, 12, 12, 12),
            LocalDateTime.of(2050, 12, 12, 12, 21)),
        new Position(0, 0),
        new Position(0, 0),
        RideRequestStatus.AVAILABLE);

    PassengerRideRequest passengerRequest = new PassengerRideRequest(
        "1", "2",
        new Interval(LocalDateTime.of(2050, 12, 12, 12, 12),
            LocalDateTime.of(2050, 12, 12, 12, 21)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.AVAILABLE);

    Ride ride = new Ride(user, userCompanion, driverRequest,
        passengerRequest);

    var notification = new RideMatchingNotification(user, "user1", ride,
        NotificationRecipient.PASSENGER);
    doReturn(Flux.just(notification)).when(notificationMessageQueue).getNotificationStream();
    NotificationService service = new NotificationService(notificationMessageQueue, botEcs,
        botInlineMarkup);

    assertNotNull(service);

    SendMessage message = new SendMessage();

    message.setChatId(notification.getUser().getTelegramChatId());
    message.setParseMode(ParseMode.MARKDOWN)
        .setText(notification.getMessage());

    verify(botEcs, times(1)).execute(message);
  }

  @Test
  void whenRideMathcingNotification_ThenSendItWithMention4Passenger() throws TelegramApiException {
    var user = new User("1", "log", "name", "420", 1L);
    var userCompanion = new User("2", "log1", "name1", "228", 1L);

    DriverRideRequest driverRequest = new DriverRideRequest(
        "1", "1",
        new Interval(LocalDateTime.of(2050, 12, 12, 12, 12),
            LocalDateTime.of(2050, 12, 12, 12, 21)),
        new Position(0, 0),
        new Position(0, 0),
        RideRequestStatus.AVAILABLE);

    PassengerRideRequest passengerRequest = new PassengerRideRequest(
        "1", "2",
        new Interval(LocalDateTime.of(2050, 12, 12, 12, 12),
            LocalDateTime.of(2050, 12, 12, 12, 21)),
        new Position(0, 0),
        new Position(0, 0), RideRequestStatus.AVAILABLE);

    Ride ride = new Ride(user, userCompanion, driverRequest,
        passengerRequest);

    var notification = new RideMatchingNotification(userCompanion, "user1", ride,
        NotificationRecipient.DRIVER);
    doReturn(Flux.just(notification)).when(notificationMessageQueue).getNotificationStream();
    NotificationService service = new NotificationService(notificationMessageQueue, botEcs,
        botInlineMarkup);

    assertNotNull(service);

    SendMessage message = new SendMessage();

    message.setChatId(notification.getUser().getTelegramChatId());
    message.setParseMode(ParseMode.MARKDOWN)
        .setText(notification.getMessage());

    verify(botEcs, times(1)).execute(message);
  }
}
