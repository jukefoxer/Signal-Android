package org.thoughtcrime.securesms.megaphone;

import org.thoughtcrime.securesms.dependencies.ApplicationDependencies; // JW
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.util.FeatureFlags;
import org.thoughtcrime.securesms.util.TextSecurePreferences; // JW

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    if (!SignalStore.kbsValues().hasPin()) {
      return false;
    }
    // JW
    if (TextSecurePreferences.isPinV2ReminderDisabled(ApplicationDependencies.getApplication())) {
      return false;
    }
    if (!FeatureFlags.pinsForAll()) {
      return false;
    }

    long lastSuccessTime = SignalStore.pinValues().getLastSuccessfulEntryTime();
    long interval        = SignalStore.pinValues().getCurrentInterval();

    return currentTime - lastSuccessTime >= interval;
  }
}
