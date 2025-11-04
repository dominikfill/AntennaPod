package de.danoeh.antennapod.ui.screen.queue;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import de.danoeh.antennapod.databinding.QueuesDialogListItemBinding;
import de.danoeh.antennapod.model.queue.Queue;

public class QueuesRecyclerAdapter extends ListAdapter<Queue, QueuesRecyclerAdapter.QueueViewHolder> {

    public interface QueueClickListener {
        void onQueueClicked(Queue queue);

        void onDeleteClicked(Queue queue);
    }

    private static final DiffUtil.ItemCallback<Queue> QUEUE_DIFF_CALLBACK = new DiffUtil.ItemCallback<Queue>() {
        @Override
        public boolean areItemsTheSame(@NonNull Queue oldItem, @NonNull Queue newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Queue oldItem, @NonNull Queue newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    private final QueueClickListener clickListener;

    public QueuesRecyclerAdapter(@NonNull QueueClickListener clickListener) {
        super(QUEUE_DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        QueuesDialogListItemBinding binding = QueuesDialogListItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new QueueViewHolder(binding, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Queue queue = getItem(position);
        holder.bind(queue);
    }

    public static class QueueViewHolder extends RecyclerView.ViewHolder {
        private final QueuesDialogListItemBinding binding;
        private final QueueClickListener listener;

        QueueViewHolder(@NonNull QueuesDialogListItemBinding binding, @NonNull QueueClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Queue queue) {
            binding.queueName.setText(queue.getName());
            binding.deleteQueueButton.setOnClickListener(v -> listener.onDeleteClicked(queue));
            itemView.setOnClickListener(v -> listener.onQueueClicked(queue));
        }
    }

}

