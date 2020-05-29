package com.java_podio.code_gen.static_interface;

import com.podio.item.ItemAPI;
import com.podio.item.ItemBadge;
import com.podio.item.filter.ItemFilter;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.java_podio.code_gen.static_interface.GenericPodioImpl.DEFAULT_OFFSET;

class ItemFilterSpliterator extends Spliterators.AbstractSpliterator<ItemBadge> {

        private static final Logger LOGGER = Logger.getLogger(ItemFilterSpliterator.class.getName());
        private final ConcurrentLinkedQueue<ItemBadge> queue = new ConcurrentLinkedQueue<>();
        private final AtomicInteger jobsStarted = new AtomicInteger(0);
        private final AtomicInteger jobsFinished = new AtomicInteger(0);
        private final AtomicInteger splits = new AtomicInteger(0);
        protected long est = Long.MAX_VALUE;

        public ItemFilterSpliterator(int appId, final ItemFilter filter, ItemAPI api) {
                super(Long.MAX_VALUE, Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE);
                Integer originalLimit = filter.getLimit();
                if (filter.getLimit() == null || filter.getLimit() > DEFAULT_OFFSET) {
                        filter.setLimit(DEFAULT_OFFSET);
                }
                filter.setOffset(0);
                startJob();
                CompletableFuture.supplyAsync(() -> api.filterItems(appId, filter))
                        .thenAccept(r -> {
                                int actualLimit = originalLimit != null ? Math.min(originalLimit, r.getFiltered()) : r.getFiltered();
                                est = actualLimit;
                                queue.addAll(r.getItems());
                                for (int offset = DEFAULT_OFFSET; offset < actualLimit; offset += DEFAULT_OFFSET) {
                                        ItemFilter offsetFilter = new ItemFilter(filter);
                                        offsetFilter.setOffset(offset);
                                        startJob();
                                        /* use separate threads for providing values - otherwise deadlocks can occur
                                        as all threads are employed with waiting for values! */
                                        Executor service = Executors.newCachedThreadPool();
                                        CompletableFuture.supplyAsync(() -> api.filterItems(appId, offsetFilter), service)
                                                .thenAccept(result -> queue.addAll(result.getItems()))
                                                .thenAccept(r2 -> jobFinished());
                                }
                        })
                        .thenAccept(r3 -> jobFinished());
        }

        private void startJob() {
                int no = jobsStarted.incrementAndGet();
                LOGGER.info("Started fetch job  #" + no);
        }

        private void jobFinished() {
                int no = jobsFinished.incrementAndGet();
                LOGGER.info("Finished fetch job #" + no);
        }

        @Override
        public long estimateSize() {
                return est / (splits.get() + 1);
        }

        @Override
        public Spliterator<ItemBadge> trySplit() {
                int currentSplits = splits.get();
                if (currentSplits >= maxSplits()) {
                        return null;
                }
                boolean increased = splits.compareAndSet(currentSplits, currentSplits + 1);
                if (!increased) {
                        return trySplit();
                }
                LOGGER.info("created split #" + (currentSplits + 1));
                return new Spliterator<ItemBadge>() {
                        @Override
                        public boolean tryAdvance(Consumer<? super ItemBadge> action) {
                                return ItemFilterSpliterator.this.tryAdvance(action);
                        }

                        @Override
                        public Spliterator<ItemBadge> trySplit() {
                                return ItemFilterSpliterator.this.trySplit();
                        }

                        @Override
                        public long estimateSize() {
                                return ItemFilterSpliterator.this.estimateSize();
                        }

                        @Override
                        public int characteristics() {
                                return ItemFilterSpliterator.this.characteristics();
                        }
                };
        }

        protected int maxSplits() {
                return 10;
        }

        @Override
        public boolean tryAdvance(Consumer<? super ItemBadge> action) {
                ItemBadge element;
                while ((element = queue.poll()) == null && !(jobsStarted.get() == jobsFinished.get())) {
                        try {
                                Thread.sleep(10);
                        } catch (InterruptedException e) {
                                throw new IllegalStateException("Failed to wait for first task!", e);
                        }
                }
                if (element == null) {
                        return false;
                } else {
                        action.accept(element);
                        return true;
                }
        }
}
