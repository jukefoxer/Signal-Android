package org.thoughtcrime.securesms.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import org.thoughtcrime.securesms.R;

public class DynamicTheme {

  public static final String DARK   = "dark";
  public static final String LIGHT  = "light";
  public static final String SYSTEM = "system";
  public static final String OLED = "oled"; // JW: added
  public static final String GREEN = "green"; // JW: added
  public static final String BLUE = "blue"; // JW: added

  private int currentTheme;

  public void onCreate(Activity activity) {
    currentTheme = getSelectedTheme(activity);
    activity.setTheme(currentTheme);
  }

  public void onResume(Activity activity) {
    if (currentTheme != getSelectedTheme(activity)) {
      Intent intent = activity.getIntent();
      activity.finish();
      OverridePendingTransition.invoke(activity);
      activity.startActivity(intent);
      OverridePendingTransition.invoke(activity);
    }
  }

  private @StyleRes int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity); // JW: added

    if (isDarkTheme(activity)) {
      return getDarkThemeStyle();
    } else if (theme.equals(OLED)) { // JW: added
      return getDarkThemeOledStyle();
    } else if (theme.equals(GREEN)) { // JW: added
      return getLightThemeGreenStyle();
    } else if (theme.equals(BLUE)) { // JW: added
      return getLightThemeBlueStyle();
    } else {
      return getLightThemeStyle();
    }
  }

  // JW: added
  protected @StyleRes int getDarkThemeOledStyle() {
    return R.style.TextSecure_DarkThemeOled;
  }
  // JW: added
  protected @StyleRes int getLightThemeBlueStyle() {
    return R.style.TextSecure_LightThemeBlue;
  }
  // JW: added
  protected @StyleRes int getLightThemeGreenStyle() {
    return R.style.TextSecure_LightThemeGreen;
  }

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightTheme;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkTheme;
  }

  public static boolean systemThemeAvailable() {
    return Build.VERSION.SDK_INT >= 29;
  }

  /**
   * Takes the system theme into account.
   */
  public static boolean isDarkTheme(@NonNull Context context) {
    String theme = TextSecurePreferences.getTheme(context);

    if (theme.equals(SYSTEM) && systemThemeAvailable()) {
      return isSystemInDarkTheme(context);
    } else {
      return theme.equals(DARK);
    }
  }

  private static boolean isSystemInDarkTheme(@NonNull Context context) {
    return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
  }

  private static final class OverridePendingTransition {
    static void invoke(Activity activity) {
      activity.overridePendingTransition(0, 0);
    }
  }
}
