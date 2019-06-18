package org.thoughtcrime.securesms.util;

import android.app.Activity;

import org.thoughtcrime.securesms.R;

public class DynamicIntroTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) return R.style.TextSecure_DarkIntroTheme;
    else if (theme.equals("oled")) return R.style.TextSecure_DarkIntroThemeOled; // JW: added
    else if (theme.equals("green")) return R.style.TextSecure_LightIntroThemeGreen; // JW: added

    return R.style.TextSecure_LightIntroTheme;
  }
}
