package de.danoeh.antennapod.model.queue;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data object that represents a complete playback queue.
 */
public class Queue implements Serializable {

    private long id;
    private String name;
    private List<QueueItem> items;

    /**
     * This constructor is used for restoring a queue from the database.
     * It creates the queue object first, and the items are added later.
     */
    public Queue(long id, String name) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<>();;
    }

    /**
     * This constructor is for creating a brand new queue
     * that hasn't been saved to the database yet.
     */
    public Queue(String name) {
        this(0, name);
    }

    /**
     * This "full" constructor is useful for testing
     * or for when all the data is already available.
     */
    public Queue(long id, String name, List<QueueItem> items) {
        this.id = id;
        this.name = name;
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QueueItem> getItems() {
        return items;
    }

    public void setItems(List<QueueItem> items) {
        this.items = items;
    }

    /**
     * Retrieves the QueueItem at a specific position in the queue.
     *
     * @param position The 0-based index of the item to retrieve.
     * @return The {@link QueueItem} at the specified position, or {@code null} if the
     *      position is out of bounds (less than 0 or greater than or equal to
     *      the queue size) or if the item list is not initialized.
     */
    public QueueItem getItemAtIndex(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    /**
     *
     * @return The number of items, or 0 if the item list is null.
     */
    public int size() {
        return (items != null) ? items.size() : 0;
    }

    /**
     *
     * @return {@code true} if the queue contains no items, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Queue queue = (Queue) o;
        return getId() == queue.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @NonNull
    @Override
    public String toString() {
        return "Queue{"
                + "id=" + getId()
                + ", name='" + getName() + '\''
                + ", size=" + size()
                + '}';
    }
}
