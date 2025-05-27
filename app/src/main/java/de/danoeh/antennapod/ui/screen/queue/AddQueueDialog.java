package de.danoeh.antennapod.ui.screen.queue;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.EditTextDialogBinding;
import de.danoeh.antennapod.storage.database.DBWriter;

public class AddQueueDialog {

    private final WeakReference<Activity> activityRef;

    public AddQueueDialog(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    public void show() {
        Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }

        final EditTextDialogBinding binding = EditTextDialogBinding.inflate(LayoutInflater.from(activity));
        String title = activity.getString(R.string.add_queue_dialog_default_title);

        binding.textInput.setHint(title);
        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot())
                .setTitle(R.string.add_queue_label)
                .setPositiveButton(android.R.string.ok, (d, input) -> {
                    String queueName = binding.textInput.getText().toString();
                    DBWriter.addQueue(queueName);
                })
                .setNeutralButton(R.string.reset, null)
                .setNegativeButton(R.string.cancel_label, null)
                .show();

        // To prevent cancelling the dialog on button click
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                (view) -> binding.textInput.setText(""));
    }
}
