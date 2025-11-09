package de.danoeh.antennapod.ui.screen.queue;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import de.danoeh.antennapod.model.queue.Queue;
import de.danoeh.antennapod.storage.database.DBReader;
import de.danoeh.antennapod.storage.database.DBWriter;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for managing the list of all queues (as in, the different named lists a user can create,
 * not the contents of a single queue).
 *
 * <p>This class handles the business logic for fetching, creating, and deleting queues,
 * and provides {@link LiveData} streams for the UI to observe.
 */
public class QueuesViewModel extends ViewModel {

    public static final String TAG = "QueuesViewModel";

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<Queue>> queues = new MutableLiveData<>();
    private final MutableLiveData<Queue> selectedQueue = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Returns the LiveData list of all queues.
     * The UI can observe this to update when the list of queues changes.
     *
     * @return A LiveData object containing the list of {@link Queue}s.
     */
    public LiveData<List<Queue>> getQueues() {
        return queues;
    }

    /**
     * Returns the LiveData for error messages.
     * The UI can observe this to display errors (e.g., in a Snackbar or Toast).
     *
     * @return A LiveData object containing error message strings.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the LiveData for the currently selected queue.
     * The UI can observe this to react to queue selection changes.
     *
     * @return A LiveData object containing the selected {@link Queue}.
     */
    public LiveData<Queue> getSelectedQueue() {
        return selectedQueue;
    }

    /**
     * Updates the selected queue LiveData.
     * This is typically called when the user taps on a queue in the list.
     *
     * @param queue The {@link Queue} object that was selected by the user.
     */
    public void onQueueSelected(Queue queue) {
        selectedQueue.postValue(queue);
    }

    /**
     * Fetches the list of all queues from the database asynchronously.
     * Work is performed on an IO thread, and the result (or error) is posted
     * to the {@link #queues} or {@link #errorMessage} LiveData on the main thread.
     */
    public void loadQueues() {
        disposables.add(
                Observable.fromCallable(DBReader::df_getAllQueues)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                queues::postValue,
                                error -> {
                                    Log.e(TAG, "Failed to load queues", error);
                                    errorMessage.postValue("Failed to load queues");
                                }
                        )
        );
    }

    /**
     * Adds a new queue to the database.
     *
     * <p>Performs validation to ensure the queue name is not empty.
     * The database operation is performed on an IO thread. On success,
     * {@link #loadQueues()} is called to refresh the list. On failure,
     * an error message is posted to {@link #errorMessage}.
     *
     * @param context   The application context, required for database operations.
     * @param queueName The name for the new queue. Must not be null or empty.
     */
    public void addQueue(Context context, String queueName) {
        if (queueName == null || queueName.trim().isEmpty()) {
            errorMessage.postValue("Queue name cannot be empty");
            return;
        }
        disposables.add(
                Observable.fromCallable(() -> {
                    DBWriter.df_createQueue(context, queueName).get();
                    return true;
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> loadQueues(),
                                error -> {
                                    Log.e(TAG, "Failed to add queue", error);
                                    errorMessage.postValue("Failed to add queue: " + error.getMessage());
                                }
                        )
        );
    }

    /**
     * Removes a queue from the database.
     *
     * <p>Performs validation to prevent deleting a queue with ID 0 or
     * deleting the last remaining queue.
     * The database operation is performed on an IO thread. On success,
     * {@link #loadQueues()} is called to refresh the list. On failure,
     * an error message is posted to {@link #errorMessage}.
     *
     * @param context The application context, required for database operations.
     * @param queueId The ID of the queue to be removed.
     */
    public void removeQueue(Context context, long queueId) {
        if (queueId == 0) {
            errorMessage.postValue("Queue id cannot be 0");
            return;
        }

        List<Queue> currentQueues = queues.getValue();
        if (currentQueues == null || currentQueues.size() <= 1) {
            errorMessage.postValue("Cannot delete the last remaining queue");
            return;
        }

        disposables.add(
                Observable.fromCallable(() -> {
                    DBWriter.df_removeQueue(context, queueId).get();
                    return true;
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> loadQueues(),
                                error -> {
                                    Log.e(TAG, "Failed to delete queue", error);
                                    errorMessage.postValue("Failed to delete queue: " + error.getMessage());
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
