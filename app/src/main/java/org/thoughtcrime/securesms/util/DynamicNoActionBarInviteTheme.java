package org.thoughtcrime.securesms.util;

import android.app.Activity;

import org.thoughtcrime.securesms.R;

public class DynamicNoActionBarInviteTheme extends DynamicTheme {
  @Override
  protected int getSelectedTheme(Activity activity) {
    String theme = TextSecurePreferences.getTheme(activity);

    if (theme.equals("dark")) return R.style.Signal_NoActionBar_Invite;
    else if (theme.equals("oled")) return R.style.Signal_NoActionBar_Invite_Oled; // JW: added
    else if (theme.equals("green")) return R.style.Signal_Light_NoActionBar_Invite_Green; // JW: added
    else if (theme.equals("blue")) return R.style.Signal_Light_NoActionBar_Invite_Blue; // JW: added

    return R.style.Signal_Light_NoActionBar_Invite;
  }
}
