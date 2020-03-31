package org.thoughtcrime.securesms.util;

import androidx.annotation.StyleRes;

import org.thoughtcrime.securesms.R;

public class DynamicNoActionBarInviteTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.Signal_Light_NoActionBar_Invite;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.Signal_NoActionBar_Invite;
  }

  // JW: added
  protected @StyleRes int getDarkThemeOledStyle() {
    return R.style.Signal_NoActionBar_Invite_Oled;
  }
  // JW: added
  protected @StyleRes int getLightThemeBlueStyle() {
    return R.style.Signal_Light_NoActionBar_Invite_Blue;
  }
  // JW: added
  protected @StyleRes int getLightThemeGreenStyle() {
    return R.style.Signal_Light_NoActionBar_Invite_Green;
  }
}
