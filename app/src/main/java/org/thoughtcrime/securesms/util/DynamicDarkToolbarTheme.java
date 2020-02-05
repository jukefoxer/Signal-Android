package org.thoughtcrime.securesms.util;

import android.app.Activity;

import org.thoughtcrime.securesms.R;

public class DynamicDarkToolbarTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) {
      return R.style.TextSecure_DarkNoActionBar_DarkToolbar;
    }
    else if (theme.equals("oled")) {
      return R.style.TextSecure_DarkNoActionBar_DarkToolbar; // JW: added
    }
    else if (theme.equals("green")) {
      return R.style.TextSecure_LightNoActionBar_DarkToolbar; // JW: added
    }
    else if (theme.equals("blue")) {
      return R.style.TextSecure_LightNoActionBar_DarkToolbar; // JW: added
    }

    return R.style.TextSecure_LightNoActionBar_DarkToolbar;
  }
}
