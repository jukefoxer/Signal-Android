package org.thoughtcrime.securesms.util;

import androidx.annotation.StyleRes;

import org.thoughtcrime.securesms.R;

public class DynamicNoActionBarTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightNoActionBar;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkNoActionBar;
  }

  // JW: added
  protected @StyleRes int getDarkThemeOledStyle() {
    return R.style.TextSecure_DarkNoActionBarOled;
  }
  // JW: added
  protected @StyleRes int getLightThemeBlueStyle() {
    return R.style.TextSecure_LightNoActionBarBlue;
  }
  // JW: added
  protected @StyleRes int getLightThemeGreenStyle() {
    return R.style.TextSecure_LightNoActionBarGreen;
  }
}
