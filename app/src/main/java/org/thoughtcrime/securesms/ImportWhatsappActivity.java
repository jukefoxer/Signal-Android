package org.thoughtcrime.securesms;

import android.os.Bundle;
import android.view.MenuItem;

import org.thoughtcrime.securesms.util.DynamicLanguage;
import org.thoughtcrime.securesms.util.DynamicTheme;


public class ImportWhatsappActivity extends PassphraseRequiredActivity {

  @SuppressWarnings("unused")
  private static final String TAG = ImportWhatsappActivity.class.getSimpleName();
  

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    setContentView(R.layout.activity_standalone_importer);
  }

  @Override
  public void onResume() {
    super.onResume();
  }
}
