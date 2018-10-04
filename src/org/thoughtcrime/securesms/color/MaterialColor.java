package org.thoughtcrime.securesms.color;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.R;

import java.util.HashMap;
import java.util.Map;

import static org.thoughtcrime.securesms.util.ThemeUtil.isDarkTheme;

// JW: put original colors back
public enum MaterialColor {
  RED        (R.color.red_700,         R.color.red_400,         R.color.red_900,         "red"),
  PINK       (R.color.pink_700,        R.color.pink_400,        R.color.pink_900,        "pink"),
  PURPLE     (R.color.purple_700,      R.color.purple_400,      R.color.purple_900,      "purple"),
  DEEP_PURPLE(R.color.deep_purple_700, R.color.deep_purple_400, R.color.deep_purple_900, "deep_purple"),
  INDIGO     (R.color.indigo_700,      R.color.indigo_400,      R.color.indigo_900,      "indigo"),
  BLUE       (R.color.blue_700,        R.color.blue_500,        R.color.blue_900,        "blue"),
  LIGHT_BLUE (R.color.light_blue_700,  R.color.light_blue_500,  R.color.light_blue_900,  "light_blue"),
  CYAN       (R.color.cyan_700,        R.color.cyan_500,        R.color.cyan_900,        "cyan"),
  TEAL       (R.color.teal_700,        R.color.teal_500,        R.color.teal_900,        "teal"),
  GREEN      (R.color.green_700,       R.color.green_500,       R.color.green_900,       "green"),
  LIGHT_GREEN(R.color.light_green_700, R.color.light_green_600, R.color.light_green_900, "light_green"),
  LIME       (R.color.lime_700,        R.color.lime_500,        R.color.lime_900,        "lime"),
  YELLOW     (R.color.yellow_700,      R.color.yellow_500,      R.color.yellow_900,      "yellow"),
  AMBER      (R.color.amber_700,       R.color.amber_600,       R.color.amber_900,       "amber"),
  ORANGE     (R.color.orange_700,      R.color.orange_500,      R.color.orange_900,      "orange"),
  DEEP_ORANGE(R.color.deep_orange_700, R.color.deep_orange_500, R.color.deep_orange_900, "deep_orange"),
  BROWN      (R.color.brown_700,       R.color.brown_500,       R.color.brown_900,       "brown"),
  GREY       (R.color.grey_700,        R.color.grey_500,        R.color.grey_900,        "grey"),
  BLUE_GREY  (R.color.blue_grey_700,   R.color.blue_grey_500,   R.color.blue_grey_900,   "blue_grey");

  // JW: no translation of colors
  private static final Map<String, MaterialColor> COLOR_MATCHES = new HashMap<String, MaterialColor>() {{
    put("red", RED);
    put("brown", BROWN);
    put("pink", PINK);
    put("purple", PURPLE);
    put("deep_purple", DEEP_PURPLE);
    put("indigo", INDIGO);
    put("blue", BLUE);
    put("light_blue", LIGHT_BLUE);
    put("cyan", CYAN);
    put("blue_grey", BLUE_GREY);
    put("teal", TEAL);
    put("green", GREEN);
    put("light_green", LIGHT_GREEN);
    put("lime", LIME);
    put("orange", ORANGE);
    put("amber", AMBER);
    put("deep_orange", DEEP_ORANGE);
    put("yellow", YELLOW);
    put("grey", GREY);
    put("group_color", BLUE);
  }};

  private final @ColorRes int mainColor;
  private final @ColorRes int tintColor;
  private final @ColorRes int shadeColor;

  private final String serialized;


  MaterialColor(@ColorRes int mainColor, @ColorRes int tintColor, @ColorRes int shadeColor, String serialized) {
    this.mainColor  = mainColor;
    this.tintColor  = tintColor;
    this.shadeColor = shadeColor;
    this.serialized = serialized;
  }

  public @ColorInt int toConversationColor(@NonNull Context context) {
    return context.getResources().getColor(mainColor);
  }

  public @ColorInt int toAvatarColor(@NonNull Context context) {
    return context.getResources().getColor(isDarkTheme(context) ? shadeColor : mainColor);
  }

  public @ColorInt int toActionBarColor(@NonNull Context context) {
    return context.getResources().getColor(mainColor);
  }

  public @ColorInt int toStatusBarColor(@NonNull Context context) {
    return context.getResources().getColor(shadeColor);
  }

  public @ColorRes int toQuoteBarColorResource(@NonNull Context context, boolean outgoing) {
    if (outgoing) {
      return isDarkTheme(context) ? R.color.core_black : R.color.core_white;
    }
    return isDarkTheme(context) ? tintColor : shadeColor;
  }

  public @ColorInt int toQuoteBackgroundColor(@NonNull Context context, boolean outgoing) {
    return context.getResources().getColor(isDarkTheme(context) ? shadeColor : tintColor);
  }

  public @ColorInt int toQuoteFooterColor(@NonNull Context context, boolean outgoing) {
    return context.getResources().getColor(isDarkTheme(context) ? tintColor : shadeColor);
  }

  public boolean represents(Context context, int colorValue) {
    return context.getResources().getColor(mainColor)  == colorValue ||
           context.getResources().getColor(tintColor)  == colorValue ||
           context.getResources().getColor(shadeColor) == colorValue;
  }

  public String serialize() {
    return serialized;
  }

  public static MaterialColor fromSerialized(String serialized) throws UnknownColorException {
    if (COLOR_MATCHES.containsKey(serialized)) {
      return COLOR_MATCHES.get(serialized);
    }

    throw new UnknownColorException("Unknown color: " + serialized);
  }

  public static class UnknownColorException extends Exception {
    public UnknownColorException(String message) {
      super(message);
    }
  }
}
