package org.thoughtcrime.securesms.util;

import androidx.annotation.StyleRes;

import org.thoughtcrime.securesms.R;

public class DynamicDarkActionBarTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightTheme_Conversation;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkTheme_Conversation;
  }

  // JW: added
  protected @StyleRes int getDarkThemeOledStyle() {
    return R.style.TextSecure_DarkTheme_Conversation;
  }
  // JW: added
  protected @StyleRes int getLightThemeBlueStyle() {
    return R.style.TextSecure_LightTheme_Conversation;
  }
  // JW: added
  protected @StyleRes int getLightThemeGreenStyle() {
    return R.style.TextSecure_LightTheme_Conversation;
  }

}
