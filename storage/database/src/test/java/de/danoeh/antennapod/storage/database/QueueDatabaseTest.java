package de.danoeh.antennapod.storage.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedItemFilter;
import de.danoeh.antennapod.model.feed.FeedMedia;
import de.danoeh.antennapod.model.queue.Queue;
import de.danoeh.antennapod.model.queue.QueueItem;
import de.danoeh.antennapod.net.download.serviceinterface.AutoDownloadManager;
import de.danoeh.antennapod.net.download.serviceinterface.DownloadServiceInterface;
import de.danoeh.antennapod.net.download.serviceinterface.DownloadServiceInterfaceStub;
import de.danoeh.antennapod.net.sync.serviceinterface.SynchronizationQueue;
import de.danoeh.antennapod.net.sync.serviceinterface.SynchronizationQueueStub;
import de.danoeh.antennapod.storage.preferences.PlaybackPreferences;
import de.danoeh.antennapod.storage.preferences.UserPreferences;

@RunWith(RobolectricTestRunner.class)
public class QueueDatabaseTest {
    private Context context;
    private List<FeedItem> testItems;

    private static final long DEFAULT_QUEUE_ID = 1;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.getApplication();
        UserPreferences.init(context);
        PlaybackPreferences.init(context);
        PodDBAdapter.init(context);
        SynchronizationQueue.setInstance(new SynchronizationQueueStub());
        AutoDownloadManager.setInstance(new AutoDownloadManager() {
            @Override
            public Future<?> autodownloadUndownloadedItems(Context context) {
                return null;
            }

            @Override
            public void performAutoCleanup(Context context) {

            }
        });

        DownloadServiceInterface.setImpl(new DownloadServiceInterfaceStub());

        PodDBAdapter.deleteDatabase();
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.close();
        SynchronizationQueue.setInstance(new SynchronizationQueueStub());

        testItems = createTestFeed(4);
        DBWriter.addNewFeed(context, testItems.get(0).getFeed()).get();
        DBWriter.setItemList(testItems).get();

        testItems = DBReader.getFeedItemList(testItems.get(0).getFeed(),
                FeedItemFilter.unfiltered(), null, 0, 10);
    }

    @Test
    public void testRemoveItemsFromMultipleQueues() throws Exception {
        // --- Setup ---
        // 1. We need 4 items
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        FeedItem item3 = testItems.get(2);
        FeedItem item4 = testItems.get(3);



        DBWriter.df_createQueue(context, "Queue B").get();
        long queueBId = DBReader.df_getAllQueues().get(1).getId();

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2).get();
        DBWriter.df_addFeedItemToQueue(context, queueBId, item3, item4).get();

        assertEquals(2, DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID).size());
        assertEquals(2, DBReader.df_getFeedItemsInQueue(queueBId).size());

        DBWriter.df_dequeueFeedItems(context, false, item1.getId(), item3.getId()).get();

        List<FeedItem> queueA = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(1, queueA.size());
        assertEquals(item2.getId(), queueA.get(0).getId());

        List<FeedItem> queueB = DBReader.df_getFeedItemsInQueue(queueBId);
        assertEquals(1, queueB.size());
        assertEquals(item4.getId(), queueB.get(0).getId());

        List<QueueItem> rawItems = DBReader.df_getAllQueueItems();
        assertEquals(2, rawItems.size());

        QueueItem itemFromA = rawItems.get(0).getFeedItemId() == item2.getId() ? rawItems.get(0) : rawItems.get(1);
        assertEquals(DEFAULT_QUEUE_ID, itemFromA.getQueueId());
        assertEquals(item2.getId(), itemFromA.getFeedItemId());
        assertEquals(0, itemFromA.getPosition());

        QueueItem itemFromB = rawItems.get(0).getFeedItemId() == item4.getId() ? rawItems.get(0) : rawItems.get(1);
        assertEquals(queueBId, itemFromB.getQueueId());
        assertEquals(item4.getId(), itemFromB.getFeedItemId());
        assertEquals(0, itemFromB.getPosition());
    }

    @After
    public void tearDown() throws Exception {
        PodDBAdapter.tearDownTests();
        DBWriter.tearDownTests();
    }

    @Test
    public void testDefaultQueueIsCreated() throws Exception {
        // This test verifies the PodDBHelper.df_createQueue method
        List<Queue> queues = DBReader.df_getAllQueues();

        assertNotNull(queues);
        assertEquals(1, queues.size());
        assertEquals(DEFAULT_QUEUE_ID, queues.get(0).getId());
        assertEquals("Queue", queues.get(0).getName());
    }

    @Test
    public void testCreateNewQueue() throws Exception {
        DBWriter.df_createQueue(context, "Test Queue 2").get();

        List<Queue> queues = DBReader.df_getAllQueues();
        assertEquals(2, queues.size());
        assertEquals("Queue", queues.get(0).getName());
        assertEquals("Test Queue 2", queues.get(1).getName());
    }

    @Test
    public void testAddFeedItemsToQueue() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2).get();

        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(2, queue.size());
        assertEquals(item1.getId(), queue.get(0).getId());
        assertEquals(item2.getId(), queue.get(1).getId());
    }

    @Test
    public void testAddFeedItemsMarksNewAsUnplayed() throws Exception {
        FeedItem item1 = testItems.get(0);
        item1.setNew();
        DBWriter.setFeedItem(item1).get();

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1).get();

        FeedItem reloadedItem = DBReader.getFeedItem(item1.getId());
        assertNotNull(reloadedItem);
        assertTrue(!reloadedItem.isPlayed() && !reloadedItem.isNew());
        assertEquals(FeedItem.UNPLAYED, reloadedItem.getPlayState());
    }

    @Test
    public void testAddFeedItemsSkipsDuplicates() throws Exception {
        FeedItem item1 = testItems.get(0);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1).get();
        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1).get();

        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(1, queue.size());
    }

    @Test
    public void testRemoveFeedItemFromQueue() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        FeedItem item3 = testItems.get(2);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2, item3).get();

        DBWriter.df_dequeueFeedItem(context, false, item2).get();

        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(2, queue.size());
        assertEquals(item1.getId(), queue.get(0).getId());
        assertEquals(item3.getId(), queue.get(1).getId());
    }

    @Test
    public void testRemoveMultipleNonConcurrentItems() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        FeedItem item3 = testItems.get(2);
        FeedItem item4 = testItems.get(3);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2, item3, item4).get();

        DBWriter.df_dequeueFeedItems(context, false, item1.getId(), item3.getId()).get();

        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(2, queue.size());

        assertEquals(item2.getId(), queue.get(0).getId());
        assertEquals(item4.getId(), queue.get(1).getId());

        List<QueueItem> rawItems = DBReader.df_getAllQueueItems();
        assertEquals(item2.getId(), rawItems.get(0).getFeedItemId());
        assertEquals(0, rawItems.get(0).getPosition());
        assertEquals(item4.getId(), rawItems.get(1).getFeedItemId());
        assertEquals(1, rawItems.get(1).getPosition());
    }

    @Test
    public void testClearQueue() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2).get();

        assertEquals(2, DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID).size());

        DBWriter.df_clearQueue(DEFAULT_QUEUE_ID).get();

        assertEquals(0, DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID).size());
    }

    @Test
    public void testRemoveQueue() throws Exception {
        DBWriter.df_createQueue(context, "Queue To Delete").get();
        long queueToDeleteId = DBReader.df_getAllQueues().get(1).getId();
        DBWriter.df_addFeedItemToQueue(context, queueToDeleteId, testItems.get(0)).get();

        assertEquals(2, DBReader.df_getAllQueues().size());
        assertEquals(1, DBReader.df_getFeedItemsInQueue(queueToDeleteId).size());
        assertEquals(1, DBReader.df_getAllQueueItems().size());

        DBWriter.df_removeQueue(context, queueToDeleteId).get();

        assertEquals(1, DBReader.df_getAllQueues().size());
        assertEquals(DEFAULT_QUEUE_ID, DBReader.df_getAllQueues().get(0).getId());
        assertEquals(0, DBReader.df_getAllQueueItems().size());
    }

    @Test
    public void testQueueTagging() throws Exception {
        FeedItem itemInQueue = testItems.get(0);
        FeedItem itemNotInQueue = testItems.get(1);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, itemInQueue).get();

        List<FeedItem> itemsToLoad = List.of(itemInQueue, itemNotInQueue);
        DBReader.loadAdditionalFeedItemListData(itemsToLoad);

        assertTrue(itemInQueue.isTagged(FeedItem.TAG_QUEUE));
        assertFalse(itemNotInQueue.isTagged(FeedItem.TAG_QUEUE));
    }

    @Test
    public void testGetAllQueueItems() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2).get();

        List<QueueItem> queueItems = DBReader.df_getAllQueueItems();

        assertEquals(2, queueItems.size());
        assertEquals(item1.getId(), queueItems.get(0).getFeedItemId());
        assertEquals(item2.getId(), queueItems.get(1).getFeedItemId());
        assertEquals(0, queueItems.get(0).getPosition());
        assertEquals(1, queueItems.get(1).getPosition());
    }

    @Test
    public void testRemoveFeedItemsFix() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2).get();
        assertEquals(2, DBReader.df_getAllQueueItems().size());

        DBWriter.deleteFeedItems(context, List.of(item1)).get();

        List<QueueItem> queueItems = DBReader.df_getAllQueueItems();
        assertEquals(1, queueItems.size()); // Should be 1, not 2
        assertEquals(item2.getId(), queueItems.get(0).getFeedItemId());
    }

    @Test
    public void testMoveQueueItem() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        FeedItem item3 = testItems.get(2);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2, item3).get();

        DBWriter.df_moveQueueItem(DEFAULT_QUEUE_ID, 2, 0, true).get();

        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(3, queue.size());
        assertEquals(item3.getId(), queue.get(0).getId());
        assertEquals(item1.getId(), queue.get(1).getId());
        assertEquals(item2.getId(), queue.get(2).getId());
    }

    @Test
    public void testMoveQueueItemsToTop() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        FeedItem item3 = testItems.get(2);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2, item3).get();

        List<FeedItem> itemsToMove = List.of(item2, item3);
        DBWriter.df_moveQueueItemsToTop(DEFAULT_QUEUE_ID, itemsToMove).get();

        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(3, queue.size());
        assertEquals(item2.getId(), queue.get(0).getId());
        assertEquals(item3.getId(), queue.get(1).getId());
        assertEquals(item1.getId(), queue.get(2).getId());
    }

    @Test
    public void testMoveQueueItemsToBottom() throws Exception {
        FeedItem item1 = testItems.get(0);
        FeedItem item2 = testItems.get(1);
        FeedItem item3 = testItems.get(2);

        DBWriter.df_addFeedItemToQueue(context, DEFAULT_QUEUE_ID, item1, item2, item3).get();

        List<FeedItem> itemsToMove = List.of(item1, item2);
        DBWriter.df_moveQueueItemsToBottom(DEFAULT_QUEUE_ID, itemsToMove).get();
        
        List<FeedItem> queue = DBReader.df_getFeedItemsInQueue(DEFAULT_QUEUE_ID);
        assertEquals(3, queue.size());
        assertEquals(item3.getId(), queue.get(0).getId());
        assertEquals(item1.getId(), queue.get(1).getId());
        assertEquals(item2.getId(), queue.get(2).getId());
    }

    private List<FeedItem> createTestFeed(int numItems) {
        Feed feed = new Feed("http://example.com/feed", null, "Test Feed");
        feed.setItems(new ArrayList<>());

        List<FeedItem> items = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            FeedItem item = new FeedItem(0, "Item " + i, "item-" + i,
                    "http://example.com/item-" + i, new Date(), FeedItem.UNPLAYED, feed);

            // Add media, as df_addFeedItemToQueue checks for it
            FeedMedia media = new FeedMedia(item, "http://example.com/media-" + i, 10, "audio/mpeg");
            item.setMedia(media);

            feed.getItems().add(item);
            items.add(item);
        }
        return items;
    }
}
