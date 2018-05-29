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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import org.thoughtcrime.securesms.crypto.DatabaseSecret;
import org.thoughtcrime.securesms.crypto.DatabaseSecretProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.nio.channels.FileChannel;

public class EncryptedBackupExporter {
  
  private static final String TAG = EncryptedBackupExporter.class.getSimpleName();

  // File used to store the DatabaseSecret, required after the transfer to SQLCipher in Signal 4.16
  private static final String databaseSecretFile = "databasesecret.txt";

  public static void exportToSd(Context context) throws NoExternalStorageException, IOException {
    verifyExternalStorageForExport();
    DatabaseSecretProvider dsp = new DatabaseSecretProvider(context);
    DatabaseSecret dbs = dsp.getOrCreateDatabaseSecret();
    exportDirectory(context, "");
    exportDatabaseSecret(context, dbs);
  }

  public static void importFromSd(Context context) throws NoExternalStorageException, IOException {
    verifyExternalStorageForImport();
    DatabaseSecret dbs = getDatabaseSecretFromBackup();
    importDirectory(context, "");
    if (dbs != null) {
      importAndOverwriteDatabaseSecret(context, dbs);
    }
  }

  private static String getExportDatabaseSecretFullName() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
           File.separator + "TextSecureExportDatabaseSecret" +
           File.separator + databaseSecretFile;
  }

  private static String getExportDatabaseSecretDirectory() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() +
            File.separator + "TextSecureExportDatabaseSecret" + File.separator;
  }

  private static String getExportDirectoryPath() {
    File sdDirectory  = Environment.getExternalStorageDirectory();
    return sdDirectory.getAbsolutePath() + File.separator + "TextSecureExport";
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
    if (!directoryName.equals("/lib")) {  
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

  // Store the DatabaseSecret in a file
  private static void exportDatabaseSecret(Context context, DatabaseSecret dbs) {
    File exportDirectory = new File(getExportDatabaseSecretDirectory());
    if (!exportDirectory.exists()) {
      exportDirectory.mkdir();
    }
    File databaseSecretExportFile = new File(getExportDatabaseSecretFullName());

    try {
      databaseSecretExportFile.createNewFile();
      FileOutputStream fOut = new FileOutputStream(databaseSecretExportFile);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
      outputStreamWriter.write(dbs.asString());
      outputStreamWriter.close();
      fOut.flush();
      fOut.close();
    }
    catch (IOException e) {
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
      Log.v(TAG, "File read failed: " + e.toString());
    }
    return dbs;
  }

  private static void importAndOverwriteDatabaseSecret(@NonNull Context context, DatabaseSecret dbs) {
    new DatabaseSecretProvider(context).storeOrOverwriteDatabaseSecret(context, dbs);
  }
}
