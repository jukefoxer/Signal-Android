package org.thoughtcrime.securesms.util;

import androidx.annotation.StyleRes;

import org.thoughtcrime.securesms.R;

public class DynamicIntroTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightIntroTheme;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkIntroTheme;
  }

  // JW: added
  protected @StyleRes int getDarkThemeOledStyle() {
    return R.style.TextSecure_DarkIntroThemeOled;
  }
  // JW: added
  protected @StyleRes int getLightThemeBlueStyle() {
    return R.style.TextSecure_LightIntroThemeBlue;
  }
  // JW: added
  protected @StyleRes int getLightThemeGreenStyle() {
    return R.style.TextSecure_LightIntroThemeGreen;
  }
}
