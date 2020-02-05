package org.thoughtcrime.securesms.util;

import android.app.Activity;

import org.thoughtcrime.securesms.R;

public class DynamicRegistrationTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) return R.style.TextSecure_DarkRegistrationTheme;
    else if (theme.equals("oled")) return R.style.TextSecure_DarkRegistrationThemeOled; // JW: added
    else if (theme.equals("green")) return R.style.TextSecure_LightRegistrationThemeGreen; // JW: added
    else if (theme.equals("blue")) return R.style.TextSecure_LightRegistrationThemeBlue; // JW: added

    return R.style.TextSecure_LightRegistrationTheme;
  }
}
