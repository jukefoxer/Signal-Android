package org.thoughtcrime.securesms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import net.sqlcipher.database.SQLiteDatabase;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.backup.BackupDialog;
import org.thoughtcrime.securesms.backup.BackupPassphrase;
import org.thoughtcrime.securesms.backup.FullBackupImporter;
import org.thoughtcrime.securesms.crypto.AttachmentSecretProvider;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.EncryptedBackupExporter;
import org.thoughtcrime.securesms.database.NoExternalStorageException;
import org.thoughtcrime.securesms.database.WhatsappBackup;
import org.thoughtcrime.securesms.database.WhatsappBackupImporter;
import org.thoughtcrime.securesms.database.whatsapp.WaDbContext;
import org.thoughtcrime.securesms.database.whatsapp.WaDbOpenHelper;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.notifications.NotificationChannels;
import org.thoughtcrime.securesms.permissions.Permissions;
import org.thoughtcrime.securesms.util.BackupUtil;
import org.thoughtcrime.securesms.util.StorageUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class ImportWhatsappActivity extends Activity {

  private static final int SUCCESS    = 0;
  private static final int NO_SD_CARD = 1;
  private static final int ERROR_IO   = 2;

  private static final int REQUEST_BACKUP_DIR = 123;

  private ProgressDialog progressDialog;

  private boolean permissionsGranted = false;
  private boolean dbExists = false;

  @SuppressWarnings("unused")
  private static final String TAG = ImportWhatsappActivity.class.getSimpleName();
  private BackupUtil.BackupInfo existingBackupInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_standalone_importer);
  }

  private void initViews() {
    checkPermissions();
    checkMsgStore();
    findViewById(R.id.importButton).setOnClickListener(v -> {
      doImport();
    });
    CheckBox mergeCheckbox = findViewById(R.id.merge_backup_checkbox);
    mergeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
      updateBackupDir();
    });
  }

  private void updateBackupDir() {
    runOnUiThread(() -> {
      CheckBox mergeCheckbox = findViewById(R.id.merge_backup_checkbox);
      if (mergeCheckbox.isChecked()) {
        if (SignalStore.settings().getSignalBackupDirectory() != null) {
          findViewById(R.id.passphraseEditText).setVisibility(View.VISIBLE);
          findViewById(R.id.backupInfoLabel).setVisibility(View.VISIBLE);
          findLatestBackup();
        } else {
          showChooseBackupLocationDialog();
        }
      } else {
        findViewById(R.id.passphraseEditText).setVisibility(View.GONE);
        findViewById(R.id.backupInfoLabel).setVisibility(View.GONE);
      }
      updateImportButton();
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (Build.VERSION.SDK_INT >= 29                         &&
            requestCode == REQUEST_BACKUP_DIR &&
            resultCode == Activity.RESULT_OK                    &&
            data != null                                        &&
            data.getData() != null)
    {
      Uri backupDirectoryUri = data.getData();
      SignalStore.settings().setSignalBackupDirectory(backupDirectoryUri);
      updateBackupDir();
    } else if (Build.VERSION.SDK_INT >= 29 && requestCode == REQUEST_BACKUP_DIR) {
      runOnUiThread(() -> {
        CheckBox mergeCheckbox = findViewById(R.id.merge_backup_checkbox);
        mergeCheckbox.setChecked(false);
      });
    }
  }

  private void showChooseBackupLocationDialog() {
    new AlertDialog.Builder(this)
            .setView(R.layout.backup_choose_location_dialog)
            .setCancelable(true)
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
              dialog.dismiss();
            })
            .setPositiveButton(R.string.BackupDialog_choose_folder, ((dialog, which) -> {
              Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
              if (Build.VERSION.SDK_INT >= 26) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, SignalStore.settings().getLatestSignalBackupDirectory());
              }
              intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                      Intent.FLAG_GRANT_WRITE_URI_PERMISSION       |
                      Intent.FLAG_GRANT_READ_URI_PERMISSION);

              try {
                this.startActivityForResult(intent, REQUEST_BACKUP_DIR);
              } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.BackupDialog_no_file_picker_available, Toast.LENGTH_LONG).show();
              }
              dialog.dismiss();
            }))
            .create()
            .show();
  }

  public void updateImportButton() {
    runOnUiThread(() -> {
      CheckBox mergeCheckbox = findViewById(R.id.merge_backup_checkbox);
      if (permissionsGranted && (!mergeCheckbox.isChecked() || existingBackupInfo != null)) {
        findViewById(R.id.importButton).setEnabled(true);
      } else {
        findViewById(R.id.importButton).setEnabled(false);
      }
    });
  }

  private void checkMsgStore() {
    WaDbContext dbContext = new WaDbContext(this);
    File dbFile = dbContext.getDatabasePath("msgstore.db");
    dbExists = dbFile.exists();
    runOnUiThread(() -> {
      TextView dbLabel = findViewById(R.id.dbFoundLabel);
      if (dbExists) {
        dbLabel.setText(R.string.msgstore_db_found);
        dbLabel.setTextColor(Color.GREEN);
      } else {
        dbLabel.setText(R.string.msgstore_db_not_found_please_put_in_the_root_directory_of_the_internal_phone_storage_not_on_the_sd_card);
        dbLabel.setTextColor(Color.RED);
      }
    });
    updateImportButton();
  }

  private void checkPermissions() {
    Button grantButton = findViewById(R.id.grantPermissionButton);

    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
      grantButton.setOnClickListener(v -> {
        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        ImportWhatsappActivity.this.startActivity(intent);
      });
      updatePermissionsGranted(Environment.isExternalStorageManager());

    } else {
      grantButton.setOnClickListener(v -> {
        Permissions.with(ImportWhatsappActivity.this)
                .request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .ifNecessary()
                .withPermanentDenialDialog(getString(R.string.ImportExportFragment_signal_needs_the_storage_permission_in_order_to_read_from_external_storage_but_it_has_been_permanently_denied))
                .onAllGranted(() -> updatePermissionsGranted(true))
                .onAnyDenied(() -> updatePermissionsGranted(false))
                .execute();
      });
    }
  }

  private void updatePermissionsGranted(boolean granted) {
      runOnUiThread(() -> {
        TextView permissionsLabel = findViewById(R.id.grantedLabel);
        permissionsGranted = granted;
        if (granted) {
          permissionsLabel.setText(R.string.granted);
          permissionsLabel.setTextColor(Color.GREEN);
          findViewById(R.id.grantPermissionButton).setVisibility(View.GONE);
        } else {
          permissionsLabel.setText(R.string.not_granted);
          permissionsLabel.setTextColor(Color.RED);
          findViewById(R.id.grantPermissionButton).setVisibility(View.VISIBLE);
        }
        updateImportButton();
      });
  }

  private void doImport() {
    CheckBox mergeCheckbox = findViewById(R.id.merge_backup_checkbox);
    if (mergeCheckbox.isChecked()) {
      EditText passphraseEditText = findViewById(R.id.passphraseEditText);
      String passphrase = passphraseEditText.getText().toString();
      restoreAsynchronously(this, existingBackupInfo, passphrase);
    } else {
      importWhatsApp();
    }
  }

  private void importWhatsApp() {
    CheckBox groupsCheckbox = findViewById(R.id.import_groups_checkbox);
    CheckBox mediaCheckbox = findViewById(R.id.import_media_checkbox);
    CheckBox avoidDuplicatesCheckbox = findViewById(R.id.avoid_duplicates_checkbox);
    new ImportWhatsappActivity.ImportWhatsappBackupTask(groupsCheckbox.isChecked(), avoidDuplicatesCheckbox.isChecked(), mediaCheckbox.isChecked()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  private String getDateTimeString(long timestampMs) {
    Calendar calendar = Calendar.getInstance();
    TimeZone tz = TimeZone.getDefault();
    calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    java.util.Date currenTimeZone=new java.util.Date((long)1379487711*1000);
    return sdf.format(currenTimeZone);
  }

  private void findLatestBackup() {
    try {
      existingBackupInfo = null;
      existingBackupInfo = BackupUtil.getLatestBackup();
    } catch (NoExternalStorageException e) {
      e.printStackTrace();
    }
    runOnUiThread(() -> {
      if (existingBackupInfo != null) {
        findViewById(R.id.importButton).setEnabled(true);
        TextView backupInfoText = findViewById(R.id.backupInfoLabel);
        String infoText = getString(R.string.signal_backup_found) + " " + getDateTimeString(existingBackupInfo.getTimestamp());
        backupInfoText.setText(infoText);
        backupInfoText.setTextColor(Color.GREEN);
      } else {
        SignalStore.settings().clearSignalBackupDirectory();
        findViewById(R.id.importButton).setEnabled(false);
        TextView backupInfoText = findViewById(R.id.backupInfoLabel);
        backupInfoText.setText(R.string.no_signal_backup_found);
        backupInfoText.setTextColor(Color.RED);
      }
    });
  }

  @SuppressLint("StaticFieldLeak")
  private void restoreAsynchronously(@NonNull Context context,
                                     @NonNull BackupUtil.BackupInfo backup,
                                     @NonNull String passphrase)
  {
    new AsyncTask<Void, Void, ImportWhatsappActivity.BackupImportResult>() {
      @Override
      protected ImportWhatsappActivity.BackupImportResult doInBackground(Void... voids) {
        try {
          Log.i(TAG, "Starting backup restore.");

          SQLiteDatabase database = DatabaseFactory.getBackupDatabase(context);

          BackupPassphrase.set(context, passphrase);
          FullBackupImporter.importFile(context,
                  AttachmentSecretProvider.getInstance(context).getOrCreateAttachmentSecret(),
                  database,
                  backup.getUri(),
                  passphrase);

          DatabaseFactory.upgradeRestored(context, database);
          NotificationChannels.restoreContactNotificationChannels(context);

          AppInitialization.onPostBackupRestore(context);

          Log.i(TAG, "Backup restore complete.");
          return ImportWhatsappActivity.BackupImportResult.SUCCESS;
        } catch (FullBackupImporter.DatabaseDowngradeException e) {
          Log.w(TAG, "Failed due to the backup being from a newer version of Signal.", e);
          return ImportWhatsappActivity.BackupImportResult.FAILURE_VERSION_DOWNGRADE;
        } catch (IOException e) {
          Log.w(TAG, e);
          return ImportWhatsappActivity.BackupImportResult.FAILURE_UNKNOWN;
        }
      }

      @Override
      protected void onPostExecute(@NonNull ImportWhatsappActivity.BackupImportResult result) {

        switch (result) {
          case SUCCESS:
            Log.i(TAG, "Successful backup restore.");
            importWhatsApp();
            break;
          case FAILURE_VERSION_DOWNGRADE:
            Toast.makeText(context, R.string.RegistrationActivity_backup_failure_downgrade, Toast.LENGTH_LONG).show();
            break;
          case FAILURE_UNKNOWN:
            Toast.makeText(context, R.string.RegistrationActivity_incorrect_backup_passphrase, Toast.LENGTH_LONG).show();
            break;
        }
      }
    }.execute();
  }

  private enum BackupImportResult {
    SUCCESS,
    FAILURE_VERSION_DOWNGRADE,
    FAILURE_UNKNOWN
  }

  @Override
  protected void onResume() {
    super.onResume();
    initViews();
  }

  @SuppressLint("StaticFieldLeak")
  public class ImportWhatsappBackupTask extends AsyncTask<Void, Void, Integer> {

    private final boolean importGroups;
    private final boolean importMedia;
    private final boolean avoidDuplicates;

    public ImportWhatsappBackupTask(boolean importGroups, boolean avoidDuplicates, boolean importMedia) {
      this.importGroups = importGroups;
      this.avoidDuplicates = avoidDuplicates;
      this.importMedia = importMedia;
    }

    @Override
    protected void onPreExecute() {
      progressDialog = new ProgressDialog(ImportWhatsappActivity.this);
      progressDialog.setTitle(ImportWhatsappActivity.this.getString(R.string.ImportFragment_importing));
      progressDialog.setMessage(ImportWhatsappActivity.this.getString(R.string.ImportFragment_import_whatsapp_backup_elipse));
      progressDialog.setCancelable(false);
      progressDialog.setIndeterminate(false);
      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressDialog.show();
    }

    protected void onPostExecute(Integer result) {
      Context context = ImportWhatsappActivity.this;

      if (progressDialog != null)
        progressDialog.dismiss();

      if (context == null)
        return;

      switch (result) {
        case NO_SD_CARD:
          Toast.makeText(context,
                  context.getString(R.string.ImportFragment_no_whatsapp_backup_found),
                  Toast.LENGTH_LONG).show();
          break;
        case ERROR_IO:
          Toast.makeText(context,
                  context.getString(R.string.ImportFragment_error_importing_backup),
                  Toast.LENGTH_LONG).show();
          break;
        case SUCCESS:
          Toast.makeText(context,
                  context.getString(R.string.ImportFragment_import_complete),
                  Toast.LENGTH_LONG).show();

          break;
      }
    }

    @Override
    protected Integer doInBackground(Void... params) {
      try {
        WhatsappBackupImporter.importWhatsappFromSd(ImportWhatsappActivity.this, progressDialog, importGroups, avoidDuplicates, importMedia);
        runOnUiThread(() ->  {
          progressDialog.setIndeterminate(true);
          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          progressDialog.setTitle(ImportWhatsappActivity.this.getString(R.string.WhatsApp_exporting));
          progressDialog.setMessage(ImportWhatsappActivity.this.getString(R.string.WhatsApp_exporting_backup_elipse));
        });
        EncryptedBackupExporter.exportToSd(ImportWhatsappActivity.this);
        return SUCCESS;
      } catch (NoExternalStorageException e) {
        Log.w(TAG, e);
        return NO_SD_CARD;
      } catch (IOException e) {
        Log.w(TAG, e);
        return ERROR_IO;
      }
    }
  }

}