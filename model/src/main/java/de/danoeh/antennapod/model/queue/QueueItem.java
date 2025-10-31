package de.danoeh.antennapod.model.queue;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

import de.danoeh.antennapod.model.feed.FeedItem;

/**
 * Data object for a single item within a playback queue.
 */
public class QueueItem implements Serializable {

    private long id;
    private long queueId;
    private long feedItemId;
    private FeedItem feedItem;
    private long feedId;
    private int position;

    public QueueItem(long id, long queueId, long feedItemId, long feedId, int position) {
        this.id = id;
        this.queueId = queueId;
        this.feedItemId = feedItemId;
        this.feedId = feedId;
        this.position = position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public long getFeedItemId() {
        return feedItemId;
    }

    public void setFeedItemId(long feedItemId) {
        this.feedItemId = feedItemId;
    }

    public FeedItem getFeedItem() {
        return feedItem;
    }

    public void setFeedItem(FeedItem feedItem) {
        this.feedItem = feedItem;
    }

    public long getFeedId() {
        return feedId;
    }

    public void setFeedId(long feedId) {
        this.feedId = feedId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueueItem queueItem = (QueueItem) o;
        return getId() == queueItem.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @NonNull
    @Override
    public String toString() {
        return "QueueItem{"
                + "id=" + getId()
                + ", queueId=" + getQueueId()
                + ", feedItemId=" + getFeedItemId()
                + ", feedItem=" + (getFeedItem() != null ? getFeedItem().getTitle() : "null")
                + ", feedId=" + getFeedId()
                + ", position=" + getPosition()
                + '}';
    }
}
