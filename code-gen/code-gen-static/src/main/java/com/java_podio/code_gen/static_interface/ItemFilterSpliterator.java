package com.java_podio.code_gen.static_interface;

import com.podio.item.ItemAPI;
import com.podio.item.ItemBadge;
import com.podio.item.filter.ItemFilter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

class ItemFilterSpliterator extends Spliterators.AbstractSpliterator<ItemBadge> {

        private static final Logger LOGGER = Logger.getLogger(ItemFilterSpliterator.class.getName());

        private List<ItemBadge> queue = Collections.synchronizedList(new LinkedList<>());
        private AtomicInteger jobsStarted = new AtomicInteger(0);
        private AtomicInteger jobsFinished = new AtomicInteger(0);

        public ItemFilterSpliterator(int appId, final ItemFilter filter, ItemAPI api) {
                super(Long.MAX_VALUE, Spliterator.DISTINCT | Spliterator.NONNULL);
                filter.setLimit(GenericPodioImpl.DEFAULT_OFFSET);
                filter.setOffset(0);
                startJob();
                // TODO propagate total to spliterator?
                CompletableFuture.supplyAsync(() -> api.filterItems(appId, filter))
                        .thenAccept(r -> {
                                queue.addAll(r.getItems());
                                for (int offset = GenericPodioImpl.DEFAULT_OFFSET; offset < r.getFiltered(); offset += GenericPodioImpl.DEFAULT_OFFSET) {
                                        ItemFilter offsetFilter = new ItemFilter(filter);
                                        offsetFilter.setOffset(offset);
                                        startJob();
                                        CompletableFuture.supplyAsync(() -> api.filterItems(appId, offsetFilter))
                                                .thenAccept(result -> queue.addAll(result.getItems()))
                                                .thenAccept(r2 -> jobFinished());
                                }
                        })
                        .thenAccept(r3 -> jobFinished());
        }

        private void startJob() {
                int no = jobsStarted.incrementAndGet();
                LOGGER.info("Started filter job  #" + no);
        }

        private void jobFinished() {
                int no = jobsFinished.incrementAndGet();
                LOGGER.info("Finished filter job #" + no);
        }

        @Override
        public boolean tryAdvance(Consumer<? super ItemBadge> action) {
                while (queue.isEmpty() && !(jobsStarted.get() == jobsFinished.get())) {
                        try {
                                Thread.sleep(10);
                        } catch (InterruptedException e) {
                                throw new IllegalStateException("Failed to wait for first task!", e);
                        }
                }
                if (queue.isEmpty()) {
                        return false;
                } else {
                        action.accept(queue.remove(0));
                        return true;
                }
        }
}
