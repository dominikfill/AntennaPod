package de.danoeh.antennapod.storage.database.mapper.queue;

import android.database.Cursor;
import android.database.CursorWrapper;

import androidx.annotation.NonNull;

import de.danoeh.antennapod.model.queue.Queue;
import de.danoeh.antennapod.storage.database.PodDBAdapter;

/**
 * A {@link CursorWrapper} that provides a convenience method
 * ({@link #getQueue()}) to map the current cursor row
 * to a {@link Queue} data object.
 */
public class QueueCursor extends CursorWrapper {

    private final int indexId;
    private final int indexName;

    /**
     * Creates a new QueueCursor.
     *
     * @param cursor The cursor to wrap.
     */
    public QueueCursor(Cursor cursor) {
        super(cursor);
        this.indexId = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_ID);
        this.indexName = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_NAME);
    }

    /**
     * Reads all queue data from the current cursor row
     * and returns a new {@link Queue} object.
     *
     * <p>This method does NOT move the cursor.</p>
     * <p>The returned {@link Queue} object will have an empty list of items.</p>
     *
     * @return A new, non-null {@link Queue} object.
     */
    @NonNull
    public Queue getQueue() {
        return new Queue(getLong(indexId), getString(indexName));
    }
}
