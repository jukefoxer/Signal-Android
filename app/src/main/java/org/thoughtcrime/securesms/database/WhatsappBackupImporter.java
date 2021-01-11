package org.thoughtcrime.securesms.database;

import android.content.Context;

import net.sqlcipher.database.SQLiteStatement;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.database.whatsapp.WaDbOpenHelper;
import org.thoughtcrime.securesms.mms.IncomingMediaMessage;
import org.thoughtcrime.securesms.mms.MmsException;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientId;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WhatsappBackupImporter {

    private static final String TAG = org.thoughtcrime.securesms.database.PlaintextBackupImporter.class.getSimpleName();

    private static android.database.sqlite.SQLiteDatabase openWhatsappDb(Context context) {
        try {
            android.database.sqlite.SQLiteOpenHelper db = new WaDbOpenHelper(context);
            android.database.sqlite.SQLiteDatabase newdb = db.getReadableDatabase();
            return newdb;
        }catch(Exception e2){
            Log.w(TAG, e2.getMessage());
        }
        return null;
    }

    public static void importWhatsappFromSd(Context context)
            throws NoExternalStorageException, IOException
    {
        Log.w(TAG, "importWhatsapp()");
        android.database.sqlite.SQLiteDatabase whatsappDb = openWhatsappDb(context);
        SmsDatabase smsDb          = (SmsDatabase) DatabaseFactory.getSmsDatabase(context);
        MmsDatabase mmsDb          = (MmsDatabase) DatabaseFactory.getMmsDatabase(context);
        SQLiteDatabase transaction = smsDb.beginTransaction();

        try {
            ThreadDatabase threads         = DatabaseFactory.getThreadDatabase(context);
            GroupDatabase groups           = DatabaseFactory.getGroupDatabase(context);
            WhatsappBackup backup          = new WhatsappBackup(whatsappDb);
            Set<Long>      modifiedThreads = new HashSet<>();
            WhatsappBackup.WhatsappBackupItem item;

            while ((item = backup.getNext()) != null) {
                Recipient recipient = getRecipient(context, item);
                long threadId = getThreadId(item, groups, threads, recipient);

                if (threadId == -1) continue;

                if (isMms(item)) {
                    List<Attachment> attachments = WhatsappBackup.getMediaAttachments(whatsappDb, item);
                    if (attachments != null && attachments.size() > 0) insertMms(mmsDb, item, recipient, threadId, attachments);
                } else {
                    insertSms(smsDb, transaction, item, recipient, threadId);
                }
                modifiedThreads.add(threadId);
            }

            for (long threadId : modifiedThreads) {
                threads.update(threadId, true);
            }

            Log.w(TAG, "Exited loop");
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new IOException("Whatsapp Import error!");
        } finally {
            whatsappDb.close();
            smsDb.endTransaction(transaction);
        }

    }

    private static Recipient getRecipient(Context context, WhatsappBackup.WhatsappBackupItem item) {
        Recipient recipient;
        if (item.getAddress() == null) {
            recipient = Recipient.self();
        } else {
            recipient = Recipient.external(context, item.getAddress());
        }
        return recipient;
    }

    private static long getThreadId(WhatsappBackup.WhatsappBackupItem item, GroupDatabase groups, ThreadDatabase threads, Recipient recipient) {
        long threadId;
        if (isGroupMessage(item)) {
            RecipientId threadRecipientId = getGroupId(groups, item, recipient);
            if (threadRecipientId == null) return -1;
            try {
                Recipient threadRecipient = Recipient.resolved(threadRecipientId);
                threadId = threads.getThreadIdFor(threadRecipient);
            } catch (Exception e) {
                Log.v(TAG, "Group not found: " + item.getGroupName());
                return -1;
            }
        } else {
            threadId = threads.getThreadIdFor(recipient);
        }
        return threadId;
    }

    private static boolean isMms(WhatsappBackup.WhatsappBackupItem item) {
        if (item.getMediaWaType() != 0) return true;
        return false;
    }

    private static void insertMms(MmsDatabase mmsDb, WhatsappBackup.WhatsappBackupItem item, Recipient recipient, long threadId, List<Attachment> attachments) {

        IncomingMediaMessage retrieved = new IncomingMediaMessage(recipient.getId(),
                recipient.getGroupId(),
                item.getBody(),
                item.getDate(),
                item.getDate(),
                attachments,
                0,
                0l,
                false,
                false,
                false,
                null);
        String contentLocation = "";
        try {
            mmsDb.insertMessageInbox(retrieved, contentLocation, threadId);
        } catch (MmsException e) {
            e.printStackTrace();
        }
    }

    private static void insertSms(SmsDatabase smsDb, SQLiteDatabase transaction, WhatsappBackup.WhatsappBackupItem item, Recipient recipient, long threadId) {
        SQLiteStatement statement  = smsDb.createInsertStatement(transaction);

        addStringToStatement(statement, 1, recipient.getId().serialize());
        addNullToStatement(statement, 2);
        addLongToStatement(statement, 3, item.getDate());
        addLongToStatement(statement, 4, item.getDate());
        addLongToStatement(statement, 5, item.getProtocol());
        addLongToStatement(statement, 6, item.getRead());
        addLongToStatement(statement, 7, item.getStatus());
        addTranslatedTypeToStatement(statement, 8, item.getType());
        addNullToStatement(statement, 9);
        addStringToStatement(statement, 10, item.getSubject());
        addStringToStatement(statement, 11, item.getBody());
        addStringToStatement(statement, 12, item.getServiceCenter());
        addLongToStatement(statement, 13, threadId);

        statement.execute();
        statement.close();
    }

    private static RecipientId getGroupId(GroupDatabase groups, WhatsappBackup.WhatsappBackupItem item, Recipient recipient) {
        if (item.getGroupName() == null) return null;
        List<GroupDatabase.GroupRecord> groupRecords = groups.getGroupsContainingMember(recipient.getId(), false);
        for (GroupDatabase.GroupRecord group : groupRecords) {
            if (group.getTitle().equals(item.getGroupName())) {
                return group.getRecipientId();
            }
        }
        return null;
    }

    private static boolean isGroupMessage(WhatsappBackup.WhatsappBackupItem item) {
        if (item.getGroupName() != null) return true;
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private static void addTranslatedTypeToStatement(SQLiteStatement statement, int index, int type) {
        statement.bindLong(index, SmsDatabase.Types.translateFromSystemBaseType(type));
    }

    private static void addStringToStatement(SQLiteStatement statement, int index, String value) {
        if (value == null || value.equals("null")) statement.bindNull(index);
        else                                       statement.bindString(index, value);
    }

    private static void addNullToStatement(SQLiteStatement statement, int index) {
        statement.bindNull(index);
    }

    private static void addLongToStatement(SQLiteStatement statement, int index, long value) {
        statement.bindLong(index, value);
    }

    private static boolean isAppropriateTypeForImport(long theirType) {
        long ourType = SmsDatabase.Types.translateFromSystemBaseType(theirType);

        return ourType == MmsSmsColumns.Types.BASE_INBOX_TYPE ||
                ourType == MmsSmsColumns.Types.BASE_SENT_TYPE ||
                ourType == MmsSmsColumns.Types.BASE_SENT_FAILED_TYPE;
    }
}
