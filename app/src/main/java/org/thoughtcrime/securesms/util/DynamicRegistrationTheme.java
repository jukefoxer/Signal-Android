package org.thoughtcrime.securesms.util;

import androidx.annotation.StyleRes;

import org.thoughtcrime.securesms.R;

public class DynamicRegistrationTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightRegistrationTheme;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkRegistrationTheme;
  }

  // JW: added
  protected @StyleRes int getDarkThemeOledStyle() {
    return R.style.TextSecure_DarkRegistrationThemeOled;
  }
  // JW: added
  protected @StyleRes int getLightThemeBlueStyle() {
    return R.style.TextSecure_LightRegistrationThemeBlue;
  }
  // JW: added
  protected @StyleRes int getLightThemeGreenStyle() {
    return R.style.TextSecure_LightRegistrationThemeGreen;
  }
}
