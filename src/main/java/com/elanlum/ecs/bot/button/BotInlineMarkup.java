package com.elanlum.ecs.bot.button;

import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class BotInlineMarkup {

  private final InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
  private static final String RED_CROSS_EMOJI = "\u274c";//red cross emoji
  private static final String GREEN_CHECK_MARK_EMOJI = "\u2705";//green check mark emoji

  /**
   * Allows to create keyboard markup.
   *
   * @param rideId id of a Ride necessary to pass as a part of reply string
   * @param rideUserId user id necessary to pass as a part of reply string
   * @return Inline keyboard markup entity
   */
  public InlineKeyboardMarkup createFeedBackReplyMarkup(String rideId, String rideUserId) {
    StringParser stringParser = new StringParser();
    InlineKeyboardButton occurButton = new InlineKeyboardButton()
        .setText("Ride has occurred " + GREEN_CHECK_MARK_EMOJI)
        .setCallbackData(stringParser.combine(
            new ButtonCallback("occur_button", rideId, rideUserId)));
    InlineKeyboardButton cancelButton = new InlineKeyboardButton()
        .setText("Ride has been canceled " + RED_CROSS_EMOJI)
        .setCallbackData(stringParser.combine(
            new ButtonCallback("cancel_button", rideId, rideUserId)));

    keyboardMarkup.setKeyboard(List.of(List.of(occurButton, cancelButton)));

    return keyboardMarkup;
  }
}
