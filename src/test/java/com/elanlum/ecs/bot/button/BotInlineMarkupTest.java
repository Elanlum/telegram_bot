package com.elanlum.ecs.bot.button;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Tag(TestCategory.UNIT)
class BotInlineMarkupTest {

  private static final String RED_CROSS_EMOJI = "\u274c";//red cross emoji
  private static final String GREEN_CHECK_MARK_EMOJI = "\u2705";//green check mark emoji

  @Test
  void createFeedBackReplyMarkup() {
    BotInlineMarkup botInlineMarkup = new BotInlineMarkup();
    InlineKeyboardMarkup keyboardMarkup = botInlineMarkup
        .createFeedBackReplyMarkup("rideId", "rideUserId");

    assertEquals(1, keyboardMarkup.getKeyboard().size());
    assertEquals("Ride has occurred " + GREEN_CHECK_MARK_EMOJI,
        keyboardMarkup.getKeyboard().get(0).get(0).getText());
    assertEquals("Ride has been canceled " + RED_CROSS_EMOJI,
        keyboardMarkup.getKeyboard().get(0).get(1).getText());
  }
}