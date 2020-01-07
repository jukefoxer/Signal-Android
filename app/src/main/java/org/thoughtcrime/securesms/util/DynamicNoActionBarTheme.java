package org.thoughtcrime.securesms.util;

import android.app.Activity;

import org.thoughtcrime.securesms.R;

public class DynamicNoActionBarTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) return R.style.TextSecure_DarkNoActionBar;
    else if (theme.equals("oled")) return R.style.TextSecure_DarkNoActionBarOled; // JW: added
    else if (theme.equals("green")) return R.style.TextSecure_LightNoActionBarGreen; // JW: added
    else if (theme.equals("blue")) return R.style.TextSecure_LightNoActionBarBlue; // JW: added

    return R.style.TextSecure_LightNoActionBar;
  }
}
