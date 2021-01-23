package org.thoughtcrime.securesms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class ImportWhatsappDialog {

    private static boolean importGroups = false;
    private static boolean avoidDuplicates = true;
    private static boolean importMedia = true;
    private static int numDays = -1;

    @SuppressWarnings("CodeBlock2Expr")
    @SuppressLint("InlinedApi")
    public static AlertDialog.Builder getWhatsappBackupDialog(Activity activity) {
        View checkBoxView = View.inflate(activity, R.layout.dialog_import_whatsapp, null);
        CheckBox importGroupsCheckbox = checkBoxView.findViewById(R.id.import_groups_checkbox);
        importGroupsCheckbox.setChecked(importGroups);
        importGroupsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ImportWhatsappDialog.importGroups = isChecked;
        });

        CheckBox avoidDuplicatesCheckbox = checkBoxView.findViewById(R.id.avoid_duplicates_checkbox);
        avoidDuplicatesCheckbox.setChecked(avoidDuplicates);
        avoidDuplicatesCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ImportWhatsappDialog.avoidDuplicates = isChecked;
        });

        CheckBox importMediaCheckbox = checkBoxView.findViewById(R.id.import_media_checkbox);
        importMediaCheckbox.setChecked(importMedia);
        importMediaCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ImportWhatsappDialog.importMedia = isChecked;
        });

        CheckBox numDaysCheckbox = checkBoxView.findViewById(R.id.import_x_days);
        numDaysCheckbox.setChecked(numDays != -1);
        numDaysCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            EditText numDaysEditText = checkBoxView.findViewById(R.id.numDaysEditText);
            if (isChecked) {
                numDaysEditText.setEnabled(true);
                ImportWhatsappDialog.numDays = Integer.parseInt(numDaysEditText.getText().toString());
                if (ImportWhatsappDialog.numDays < 1) ImportWhatsappDialog.numDays = -1;
            } else {
                numDaysEditText.setEnabled(true);
                ImportWhatsappDialog.numDays = -1;
            }
        });

        EditText numDaysEditText = checkBoxView.findViewById(R.id.numDaysEditText);
        numDaysEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    ImportWhatsappDialog.numDays = Integer.parseInt(numDaysEditText.getText().toString());
                    if (ImportWhatsappDialog.numDays < 1) ImportWhatsappDialog.numDays = -1;
                } catch (Exception e) {}
                return false;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_warning);
        builder.setTitle(activity.getString(R.string.ImportFragment_import_whatsapp_backup));
        builder.setMessage(activity.getString(R.string.ImportFragment_this_will_import_messages_from_whatsapp_backup))
                .setView(checkBoxView);
        return builder;
    }

    public static boolean isImportGroups() {
        return importGroups;
    }

    public static boolean isAvoidDuplicates() {
        return avoidDuplicates;
    }

    public static boolean isImportMedia() {
        return importMedia;
    }

    public static int getNumDays() {
        return numDays;
    }
}
