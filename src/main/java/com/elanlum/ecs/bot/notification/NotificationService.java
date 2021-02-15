package com.elanlum.ecs.bot.notification;

import com.elanlum.ecs.bot.TelegramBotEcs;
import com.elanlum.ecs.bot.button.BotInlineMarkup;
import com.elanlum.ecs.notification.NotificationMessageQueue;
import com.elanlum.ecs.notification.values.Notification;
import com.elanlum.ecs.notification.values.RideMatchingNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class NotificationService {

  private static final String CAR_EMOJI = "\ud83d\ude95";//car emoji
  private final TelegramBotEcs bot;
  private final BotInlineMarkup botInlineMarkup;

  /**
   * This constructor initialize NotificationService.
   *
   * @param notificationQueue is a queue of notifications
   * @param bot is a TelegramBot for execute method
   */
  @Autowired
  public NotificationService(NotificationMessageQueue notificationQueue, TelegramBotEcs bot,
      BotInlineMarkup botInlineMarkup) {
    this.bot = bot;
    this.botInlineMarkup = botInlineMarkup;
    notificationQueue.getNotificationStream()
        .filter(notification -> notification.getUser() != null
            && notification.getUser().getTelegramChatId() != null)
        .subscribe(notification -> {
          log.debug("Got notification: [{}]", notification);
          if (notification.getClass().equals(RideMatchingNotification.class)) {
            sendFeedbackNotification((RideMatchingNotification) notification);
            sendRideMessageToUser(notification);
          } else {
            sendMessageToUser(notification);
          }
        });
  }

  /**
   * This method sends notifications to a user.
   *
   * @param notification is a current notification
   */
  private void sendMessageToUser(Notification notification) {
    SendMessage message = new SendMessage();

    message.setChatId(notification.getUser().getTelegramChatId());
    message.setParseMode(ParseMode.MARKDOWN)
        .setText(notification.getMessage());
    sendNotification(message);
  }

  private void sendRideMessageToUser(Notification notification) {
    SendMessage message = new SendMessage();

    message.setChatId(notification.getUser().getTelegramChatId());
    message.setParseMode(ParseMode.MARKDOWN)
        .setText(notification.getMessage());

    sendNotification(message);
  }

  private void sendFeedbackNotification(RideMatchingNotification notification) {
    String rideId = notification.getRide().getId();
    String rideUserId = notification.getUser().getId();
    SendMessage sendMessage = new SendMessage();

    sendMessage.setChatId(notification.getUser().getTelegramChatId());
    sendMessage
        .setReplyMarkup(
            botInlineMarkup.createFeedBackReplyMarkup(rideId, rideUserId));
    sendMessage.setText("Please, provide a feedBack on your Ride! " + CAR_EMOJI);
    sendNotification(sendMessage);
  }

  private void sendNotification(SendMessage message) {
    try {
      bot.execute(message);
      log.debug("Notification sent: [{}]", message);
    } catch (TelegramApiException e) {
      log.warn("Sending failed", e);
    }
  }
}
