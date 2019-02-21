/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.database;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.thoughtcrime.securesms.crypto.AttachmentSecret;
import org.thoughtcrime.securesms.crypto.AttachmentSecretProvider;
import org.thoughtcrime.securesms.backup.BackupPassphrase;
import org.thoughtcrime.securesms.crypto.DatabaseSecret;
import org.thoughtcrime.securesms.crypto.DatabaseSecretProvider;
import org.thoughtcrime.securesms.crypto.KeyStoreHelper;
import org.thoughtcrime.securesms.logging.Log;
import org.thoughtcrime.securesms.util.Base64;
import org.thoughtcrime.securesms.util.JsonUtils;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

public class EncryptedBackupExporter {
  
  private static final String TAG = EncryptedBackupExporter.class.getSimpleName();

  // File used to store the DatabaseSecret and AttachmentSecret, required after the transfer to SQLCipher in Signal 4.16
  private static final String databaseSecretFile = "databasesecret.txt";
  private static final String attachmentSecretFile = "attachmentsecret.txt";
  private static final String logSecretFile = "logsecret.txt";
  private static final String backupKeyFile = "backupkey.txt";
  private static final String exportDirectory = "SignalExport";
  private static final String secretsExportDirectory = "SignalSecrets";

  public static void exportToSd(Context context) throws NoExternalStorageException, IOException {
    verifyExternalStorageForExport();
    DatabaseSecretProvider dsp = new DatabaseSecretProvider(context);
    AttachmentSecretProvider asp = AttachmentSecretProvider.getInstance(context);
    DatabaseSecret dbs = dsp.getOrCreateDatabaseSecret();
    AttachmentSecret ats = asp.getOrCreateAttachmentSecret();
    byte[] lgs = getOrCreateLogSecret(context);
    String bks = BackupPassphrase.get(context);
    exportDirectory(context, "");
    exportSecrets(context, dbs, ats, lgs, bks);
  }

  public static void importFromSd(Context context) throws NoExternalStorageException, IOException {
    verifyExternalStorageForImport();
    DatabaseSecret dbs = getDatabaseSecretFromBackup();
    AttachmentSecret ats = getAttachmentSecretFromBackup();
    byte[] lgs = getLogSecretFromBackup(context);
    String bks = getBackupKeyFromBackup(context);
    importDirectory(context, "");
    if (dbs != null) {
      overwriteDatabaseSecret(context, dbs);
    }
    if (ats != null) {
      overwriteAttachmentSecret(context, ats);
    }
    if (lgs != null) {
      overwriteLogSecret(context, lgs);
    }
    if (bks != null) {
	    BackupPassphrase.set(context, bks);
    }
  }

  private static String getExportDatabaseSecretFullName() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
           File.separator + secretsExportDirectory +
           File.separator + databaseSecretFile;
  }

    private static String getExportAttachmentSecretFullName() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
           File.separator + secretsExportDirectory +
           File.separator + attachmentSecretFile;
  }

  private static String getExportLogSecretFullName() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
            File.separator + secretsExportDirectory +
            File.separator + logSecretFile;
  }
  
  private static String getExportBackupKeyFullName() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
            File.separator + secretsExportDirectory +
            File.separator + backupKeyFile;
  }

  private static String getExportSecretsDirectory() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
            File.separator + secretsExportDirectory + File.separator;
  }

  private static String getExportDirectoryPath() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() + File.separator + exportDirectory;
  }

  private static void verifyExternalStorageForExport() throws NoExternalStorageException {
    if (!Environment.getExternalStorageDirectory().canWrite())
      throw new NoExternalStorageException();

    String exportDirectoryPath = getExportDirectoryPath();
    File exportDirectory       = new File(exportDirectoryPath);

    if (!exportDirectory.exists())
      exportDirectory.mkdir();
  }

  private static void verifyExternalStorageForImport() throws NoExternalStorageException {
    if (!Environment.getExternalStorageDirectory().canRead() ||
        !(new File(getExportDirectoryPath()).exists()))
        throw new NoExternalStorageException();
  }

  private static void migrateFile(File from, File to) {
    try {
      if (from.exists()) {
        FileChannel source      = new FileInputStream(from).getChannel();
        FileChannel destination = new FileOutputStream(to).getChannel();

        destination.transferFrom(source, 0, source.size());
        source.close();
        destination.close();
      }
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
    }
  }

  private static void exportDirectory(Context context, String directoryName) throws IOException {
    if (!directoryName.equals("/lib") && !directoryName.equals("/code_cache") && !directoryName.equals("/cache")) {
      File directory       = new File(context.getFilesDir().getParent() + File.separatorChar + directoryName);
      File exportDirectory = new File(getExportDirectoryPath() + File.separatorChar + directoryName);

      if (directory.exists()) {
        exportDirectory.mkdirs();

        File[] contents = directory.listFiles();

        if (contents == null)
          throw new IOException("directory.listFiles() is null for " + context.getFilesDir().getParent() + File.separatorChar + directoryName + "!");

        for (int i=0;i<contents.length;i++) {
          File localFile = contents[i];

          // Don't export the libraries
          if ( localFile.getAbsolutePath().contains("libcurve25519.so") ||
               localFile.getAbsolutePath().contains("libnative-utils.so") ||
               localFile.getAbsolutePath().contains("libjingle_peerconnection_so.so") ||
               localFile.getAbsolutePath().contains("libsqlcipher.so") ) {
           // Do nothing
          } else if (localFile.isFile()) {        
            File exportedFile = new File(exportDirectory.getAbsolutePath() + File.separator + localFile.getName());
            migrateFile(localFile, exportedFile);
          } else {
            exportDirectory(context, directoryName + File.separator + localFile.getName());
          }
        }
      } else {
        Log.w(TAG, "Could not find directory: " + directory.getAbsolutePath());
      }
    }
  }

  private static void importDirectory(Context context, String directoryName) throws IOException {
    File directory       = new File(getExportDirectoryPath() + File.separator + directoryName);
    File importDirectory = new File(context.getFilesDir().getParent() + File.separator + directoryName);

    if (directory.exists() && directory.isDirectory()) {
      importDirectory.mkdirs();

      File[] contents = directory.listFiles();

      for (File exportedFile : contents) {
        if (exportedFile.isFile()) {
          File localFile = new File(importDirectory.getAbsolutePath() + File.separator + exportedFile.getName());
          migrateFile(exportedFile, localFile);
        } else if (exportedFile.isDirectory()) {
          importDirectory(context, directoryName + File.separator + exportedFile.getName());
        }
      }
    }
  }

  // Store the encrypted secrets in a file
  private static void exportSecrets(Context context, DatabaseSecret dbs, AttachmentSecret ats, byte[] lgs, String bks) {
    File exportDirectory = new File(getExportSecretsDirectory());
    if (!exportDirectory.exists()) {
      exportDirectory.mkdir();
    }
    writeStringToFile(new File(getExportDatabaseSecretFullName()), dbs.asString());
    writeStringToFile(new File(getExportAttachmentSecretFullName()), ats.serialize());
    writeStringToFile(new File(getExportLogSecretFullName()), Base64.encodeBytes(lgs));
    if (bks != null) { // Backupkey is optional
      writeStringToFile(new File(getExportBackupKeyFullName()), bks);
    }
  }

  private static void writeStringToFile(File file, String str) {
    try {
      file.createNewFile();
      FileOutputStream fOut = new FileOutputStream(file);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
      outputStreamWriter.write(str);
      outputStreamWriter.close();
      fOut.flush();
      fOut.close();
    } catch (IOException e) {
      Log.v(TAG, "File write failed: " + e.toString());
    }
  }

  private static DatabaseSecret getDatabaseSecretFromBackup() {
    DatabaseSecret dbs = null;
    File databaseSecretExportFile = new File(getExportDatabaseSecretFullName());

    try {
      if (databaseSecretExportFile.exists()) {
        String encoded = "";
        FileInputStream fIn = new FileInputStream(databaseSecretExportFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fIn);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(bufferedReader.readLine());
        encoded = stringBuilder.toString();

        inputStreamReader.close();
        fIn.close();

        dbs = new DatabaseSecret(encoded);
      }
    } catch (IOException e) {
      Log.w(TAG, "getDatabaseSecretFromBackup file read failed: " + e.toString());
    }
    return dbs;
  }

  private static AttachmentSecret getAttachmentSecretFromBackup() {
    AttachmentSecret ats = null;
    File attachmentSecretExportFile = new File(getExportAttachmentSecretFullName());

    try {
      if (attachmentSecretExportFile.exists()) {
        String encoded = "";
        FileInputStream fIn = new FileInputStream(attachmentSecretExportFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fIn);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(bufferedReader.readLine());
        encoded = stringBuilder.toString();

        inputStreamReader.close();
        fIn.close();

        ats = new AttachmentSecret();
        try {
          ats = JsonUtils.fromJson(encoded, AttachmentSecret.class);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    } catch (IOException e) {
      Log.w(TAG, "getAttachmentSecretFromBackup file read failed: " + e.toString());
    }
    return ats;
  }

  private static byte[] getLogSecretFromBackup(Context context) {
    byte[] lgs = null;
    File logSecretExportFile = new File(getExportLogSecretFullName());

    try {
      if (logSecretExportFile.exists()) {
        String encoded = "";
        FileInputStream fIn = new FileInputStream(logSecretExportFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fIn);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(bufferedReader.readLine());
        encoded = stringBuilder.toString();

        inputStreamReader.close();
        fIn.close();

        lgs = new byte[32];
        lgs = Base64.decode(encoded);
      }
    } catch (IOException e) {
      Log.w(TAG, "getLogSecretFromBackup file read failed: " + e.toString());
    }
    return lgs;
  }
  
  private static String getBackupKeyFromBackup(Context context) {
	  String bks = null;
    // Standard backup is optional, so it can be quite normal if the file does not exist
    File backupPasswordExportFile = new File(getExportBackupKeyFullName());

    try {
      if (backupPasswordExportFile.exists()) {
        FileInputStream fIn = new FileInputStream(backupPasswordExportFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fIn);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(bufferedReader.readLine());
        bks = stringBuilder.toString();
      }
    } catch (IOException e) {
      Log.w(TAG, "getBackupKeyFromBackup file read failed: " + e.toString());
    }
    return bks;
  }

  // JW: store an existing DatabaseSecret in the settingsfile.
  private static void overwriteDatabaseSecret(@NonNull Context context, @NonNull DatabaseSecret databaseSecret) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(databaseSecret.asBytes());
      TextSecurePreferences.setDatabaseEncryptedSecret(context, encryptedSecret.serialize());
    } else {
      TextSecurePreferences.setDatabaseUnencryptedSecret(context, databaseSecret.asString());
    }
  }

  private static void overwriteAttachmentSecret(@NonNull Context context, @NonNull AttachmentSecret attachmentSecret) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(attachmentSecret.serialize().getBytes());
      TextSecurePreferences.setAttachmentEncryptedSecret(context, encryptedSecret.serialize());
    } else {
      TextSecurePreferences.setAttachmentUnencryptedSecret(context, attachmentSecret.serialize());
    }
  }

  private static void overwriteLogSecret(@NonNull Context context, @NonNull byte[] lgs) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(lgs);
      TextSecurePreferences.setLogEncryptedSecret(context, encryptedSecret.serialize());
    } else {
      TextSecurePreferences.setLogUnencryptedSecret(context, Base64.encodeBytes(lgs));
    }
  }

  // LogSecretProvider class functions. Copied here because they are all private
  static byte[] getOrCreateLogSecret(@NonNull Context context) {
    String unencryptedSecret = TextSecurePreferences.getLogUnencryptedSecret(context);
    String encryptedSecret   = TextSecurePreferences.getLogEncryptedSecret(context);

    if      (unencryptedSecret != null) return parseUnencryptedSecret(unencryptedSecret);
    else if (encryptedSecret != null)   return parseEncryptedSecret(encryptedSecret);
    else                                return createAndStoreSecret(context);
  }

  private static byte[] parseUnencryptedSecret(String secret) {
    try {
      return Base64.decode(secret);
    } catch (IOException e) {
      throw new AssertionError("Failed to decode the unecrypted secret.");
    }
  }

  private static byte[] parseEncryptedSecret(String secret) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.SealedData.fromString(secret);
      return KeyStoreHelper.unseal(encryptedSecret);
    } else {
      throw new AssertionError("OS downgrade not supported. KeyStore sealed data exists on platform < M!");
    }
  }

  private static byte[] createAndStoreSecret(@NonNull Context context) {
    SecureRandom random = new SecureRandom();
    byte[]       secret = new byte[32];
    random.nextBytes(secret);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      KeyStoreHelper.SealedData encryptedSecret = KeyStoreHelper.seal(secret);
      TextSecurePreferences.setLogEncryptedSecret(context, encryptedSecret.serialize());
    } else {
      TextSecurePreferences.setLogUnencryptedSecret(context, Base64.encodeBytes(secret));
    }

    return secret;
  }
}
