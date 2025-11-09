package de.danoeh.antennapod.ui.screen.queue;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.QueuesDialogBinding;
import de.danoeh.antennapod.model.queue.Queue;

public class QueuesDialogFragment extends DialogFragment implements QueuesRecyclerAdapter.QueueClickListener {

    public static final String TAG = "QueuesDialogFragment";

    private QueuesViewModel viewModel;
    private QueuesRecyclerAdapter adapter;

    public static QueuesDialogFragment newInstance() {
        return new QueuesDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        QueuesDialogBinding binding = QueuesDialogBinding.inflate(inflater, null, false);

        viewModel = new ViewModelProvider(requireActivity()).get(QueuesViewModel.class);
        adapter = new QueuesRecyclerAdapter(this);
        binding.queuesList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.queuesList.setAdapter(adapter);

        viewModel.getQueues().observe(this, queues -> {
            if (queues != null) {
                adapter.submitList(queues);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        binding.newQueueTextInput.setEndIconOnClickListener(v -> {
            String queueName = Objects.requireNonNull(binding.newQueueName.getText())
                    .toString()
                    .trim();
            if (!queueName.isEmpty()) {
                viewModel.addQueue(getContext(), queueName);
                binding.newQueueName.setText("");
            } else {
                Toast.makeText(getContext(), "Queue name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadQueues();

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.queues_dialog_label)
                .setView(binding.getRoot())
                .create();

    }

    /**
     * Called when the user taps on a queue item in the list.
     */
    @Override
    public void onQueueClicked(Queue queue) {
        viewModel.onQueueSelected(queue);
        dismiss();
    }

    /**
     * Called when the user taps the delete icon on a queue item.
     */
    @Override
    public void onDeleteClicked(Queue queue) {
        viewModel.removeQueue(getContext(), queue.getId());
    }

}
