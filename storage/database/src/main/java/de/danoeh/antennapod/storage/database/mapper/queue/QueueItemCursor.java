package de.danoeh.antennapod.storage.database.mapper.queue;

import android.database.Cursor;
import android.database.CursorWrapper;

import androidx.annotation.NonNull;

import de.danoeh.antennapod.model.queue.QueueItem;
import de.danoeh.antennapod.storage.database.PodDBAdapter;

/**
 * A {@link CursorWrapper} that provides a convenience method
 * ({@link #getQueueItem()}) to map the current cursor row
 * to a {@link QueueItem} data object.
 */
public class QueueItemCursor extends CursorWrapper {

    private final int indexId;
    private final int indexQueueId;
    private final int indexFeedItemId;
    private final int indexFeedId;
    private final int indexPosition;

    /**
     * Creates a new QueueItemCursor.
     *
     * @param cursor The cursor to wrap.
     */
    public QueueItemCursor(Cursor cursor) {
        super(cursor);
        this.indexId = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_ID);
        this.indexQueueId = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_QUEUE);
        this.indexFeedItemId = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_FEEDITEM);
        this.indexFeedId = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_FEED);
        this.indexPosition = cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_POSITION);
    }

    /**
     * Reads all queue item data from the current cursor row
     * and returns a new {@link QueueItem} object.
     *
     * <p> This method does NOT move the cursor.</p>
     *
     * @return A new, non-null {@link QueueItem} object.
     */
    @NonNull
    public QueueItem getQueueItem() {
        return new QueueItem(
                getLong(indexId),
                getLong(indexQueueId),
                getLong(indexFeedItemId),
                getLong(indexFeedId),
                getInt(indexPosition));
    }
}
