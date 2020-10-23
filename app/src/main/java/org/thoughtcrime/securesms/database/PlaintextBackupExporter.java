package org.thoughtcrime.securesms.database;


import android.content.Context;

import org.thoughtcrime.securesms.database.model.MmsMessageRecord;
import org.thoughtcrime.securesms.database.model.SmsMessageRecord;
import org.thoughtcrime.securesms.logging.Log;
import org.thoughtcrime.securesms.util.FileUtilsJW;
import org.thoughtcrime.securesms.util.StorageUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.io.File;
import java.io.IOException;

public class PlaintextBackupExporter {
  private static final String TAG = PlaintextBackupExporter.class.getSimpleName();

  private static final String FILENAME = "SignalPlaintextBackup.xml";
  private static final String ZIPFILENAME = "SignalPlaintextBackup.zip";

  public static void exportPlaintextToSd(Context context)
      throws NoExternalStorageException, IOException
  {
    exportPlaintext(context);
  }

  public static File getPlaintextExportFile() throws NoExternalStorageException {
    return new File(StorageUtil.getBackupPlaintextDirectory(), FILENAME);
  }

  private static File getPlaintextZipFile() throws NoExternalStorageException {
    return new File(StorageUtil.getBackupPlaintextDirectory(), ZIPFILENAME);
  }

  private static void exportPlaintext(Context context)
      throws NoExternalStorageException, IOException
  {
    Log.w(TAG, "exportPlaintext");
    SmsDatabase database = (SmsDatabase)DatabaseFactory.getSmsDatabase(context);
    MmsDatabase      mmsdb    = (MmsDatabase)DatabaseFactory.getMmsDatabase(context);
    int              count    = database.getMessageCount();
    //int              mmscount = mmsdb.getMessageCount();
    int              mmscount = 0;
    XmlBackup.Writer writer   = new XmlBackup.Writer(getPlaintextExportFile().getAbsolutePath(), count + mmscount);

    SmsMessageRecord record;
    MmsMessageRecord mmsrecord;

    SmsDatabase.Reader smsreader = null;
    MmsDatabase.Reader mmsreader = null;
    int                skip      = 0;
    int                ROW_LIMIT = 500;

    do {
      if (smsreader != null)
        smsreader.close();

      smsreader = database.readerFor(database.getMessages(skip, ROW_LIMIT));

      while ((record = smsreader.getNext()) != null) {
        XmlBackup.XmlBackupItem item =
            new XmlBackup.XmlBackupItem(0,
                                        record.getIndividualRecipient().requireSmsAddress(),
                                        record.getIndividualRecipient().getName(context),
                                        record.getDateReceived(),
                                        MmsSmsColumns.Types.translateToSystemBaseType(record.getType()),
                                        null,
                                        record.getDisplayBody(context).toString(),
                                        null,
                                        1,
                                        record.getDeliveryStatus());

        writer.writeItem(item);
      }

      skip += ROW_LIMIT;
    } while (smsreader.getCount() > 0);
/*
    int i = 0;
    Log.w(TAG, "Number of mms to export: " + mmscount);

    do {
      i++;
      Log.w(TAG, "Exporting mms: " + i);

      if (mmsreader != null)
        mmsreader.close();

      mmsreader = mmsdb.readerFor(mmsdb.getMessages(skip, ROW_LIMIT));

      while ((mmsrecord = (MmsMessageRecord)mmsreader.getNext()) != null) {
        XmlBackup.XmlBackupItem item =
            new XmlBackup.XmlBackupItem(0,
                                        mmsrecord.getIndividualRecipient().requireSmsAddress(),
                                        mmsrecord.getIndividualRecipient().getName(context),
                                        mmsrecord.getDateReceived(),
                                        MmsSmsColumns.Types.translateToSystemBaseType(record.getType()),
                                        null,
                                        mmsrecord.getDisplayBody(context).toString(),
                                        null,
                                        1,
                                        mmsrecord.getDeliveryStatus());
        Log.w(TAG, "mmsrecord exported: " + mmsrecord.getIndividualRecipient().requireSmsAddress() + ": " + mmsrecord.getDisplayBody(context).toString());
        writer.writeItem(item);
      }

      skip += ROW_LIMIT;
    } while (mmsreader.getCount() > 0);

    Log.w(TAG, "Total number of exportied mms: " + i);
 */
    writer.close();

    if (TextSecurePreferences.isPlainBackupInZipfile(context)) {
      File test = new File(getPlaintextZipFile().getAbsolutePath());
      if (test.exists()) {
        test.delete();
      }
      FileUtilsJW.createEncryptedPlaintextZipfile(context, getPlaintextZipFile().getAbsolutePath(), getPlaintextExportFile().getAbsolutePath());
      FileUtilsJW.secureDelete(getPlaintextExportFile());
    }
  }
}
