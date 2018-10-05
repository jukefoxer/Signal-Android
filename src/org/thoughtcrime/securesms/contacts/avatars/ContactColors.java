package org.thoughtcrime.securesms.contacts.avatars;

import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.color.MaterialColor;
import org.thoughtcrime.securesms.color.MaterialColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactColors {

  public static final MaterialColor UNKNOWN_COLOR = MaterialColor.GREY; // JW: change color

  // JW: change colors
  private static final List<MaterialColor> CONVERSATION_PALETTE = new ArrayList<>(Arrays.asList(
    MaterialColor.RED,
    MaterialColor.PINK,
    MaterialColor.PURPLE,
    MaterialColor.DEEP_PURPLE,
    MaterialColor.INDIGO,
    MaterialColor.BLUE,
    MaterialColor.LIGHT_BLUE,
    MaterialColor.CYAN,
    MaterialColor.TEAL,
    MaterialColor.GREEN,
    MaterialColor.LIGHT_GREEN,
    MaterialColor.LIME,
    MaterialColor.YELLOW,
    MaterialColor.ORANGE,
    MaterialColor.DEEP_ORANGE,
    MaterialColor.BROWN,
    MaterialColor.AMBER,
    MaterialColor.BLUE_GREY,
    MaterialColor.GREY
  ));

  public static MaterialColor generateFor(@NonNull String name) {
    return CONVERSATION_PALETTE.get(Math.abs(name.hashCode()) % CONVERSATION_PALETTE.size());
  }
}
