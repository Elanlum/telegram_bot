package com.elanlum.ecs.bot.button;

import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class InitialButtons {

  /**
   * This method creates 2 buttons and adds it to the keyboard.
   *
   * @return our keyboard with reply options
   */
  public ReplyKeyboardMarkup createInitialButtons() {

    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

    KeyboardButton createDriverRequest = new KeyboardButton("Create driver request");
    KeyboardButton createPassengerRequest = new KeyboardButton("Create passenger request");

    KeyboardRow keyboardFirstRow = new KeyboardRow();
    keyboardFirstRow.add(createDriverRequest);
    keyboardFirstRow.add(createPassengerRequest);

    List<KeyboardRow> keyboard = List.of(keyboardFirstRow);

    replyKeyboardMarkup.setResizeKeyboard(true)
        .setOneTimeKeyboard(true)
        .setKeyboard(keyboard);

    return replyKeyboardMarkup;
  }

}
