package com.elanlum.ecs.bot.button;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elanlum.ecs.utils.TestCategory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@Tag(TestCategory.UNIT)
class InitialButtonsTest {

  InitialButtons initialButtons = new InitialButtons();

  @Test
  void createInitialButtonsTest() {
    ReplyKeyboardMarkup replyKeyboardMarkup;
    replyKeyboardMarkup = initialButtons.createInitialButtons();
    assertTrue(replyKeyboardMarkup.getOneTimeKeyboard());
    assertTrue(replyKeyboardMarkup.getResizeKeyboard());
    assertEquals(1, replyKeyboardMarkup.getKeyboard().size());
    assertEquals(new KeyboardButton(
        "Create driver request"), replyKeyboardMarkup.getKeyboard().get(0).get(0));
    assertEquals(new KeyboardButton(
        "Create passenger request"), replyKeyboardMarkup.getKeyboard().get(0).get(1));
  }
}