package org.thoughtcrime.securesms.util;

import android.app.Activity;

import org.thoughtcrime.securesms.R;

public class DynamicDarkActionBarTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) {
      return R.style.TextSecure_DarkTheme_Conversation;
    }
    else if (theme.equals("oled")) {
      return R.style.TextSecure_DarkTheme_Conversation; // JW: added
    }
    else if (theme.equals("green")) {
      return R.style.TextSecure_LightTheme_Conversation; // JW: added
    }
    else if (theme.equals("blue")) {
      return R.style.TextSecure_LightTheme_Conversation; // JW: added
    }

    return R.style.TextSecure_LightTheme_Conversation;
  }
}
