package com.java_podio.code_gen.static_interface;

import com.java_podio.code_gen.static_classes.AppWrapper;
import com.podio.APIApplicationException;
import com.podio.BaseAPI;
import com.podio.file.FileAPI;
import com.podio.filter.FilterBy;
import com.podio.filter.FilterByValue;
import com.podio.item.*;
import com.podio.item.filter.ItemFilter;

import javax.swing.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic interface methods for generated {@link AppWrapper} classes.
 */
public abstract class GenericPodioImpl implements GenericPodioInterface {

        private static final Logger LOGGER = Logger.getLogger(GenericPodioImpl.class.getName());
        public static final int DEFAULT_OFFSET = 500;

        /**
     * This is not thread safe! If used concurrently this needs to be changed to {@link ThreadLocal}!
     */
    public static SimpleDateFormat defaultDateFormatNoTime = new SimpleDateFormat("dd.MM.yyyy");
    
    /**
     * User or app credentials might be used here.
     * 
     * @param appId
     * @param type
     *            api type
     * @return
     */
    protected abstract <T extends BaseAPI> T getAPI(Integer appId, Class<T> type);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #uploadFile(java.io.File, int, java.lang.String)
     */
    public int uploadFile(File file, int appId, String filename) {
	return getAPI(appId, FileAPI.class).uploadFile(filename, file);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #addItem(com.java_podio.code_gen.static_classes.AppWrapper)
     */
    public int addItem(AppWrapper item) {
	ItemCreate itemCreate = item.getItemCreate();
	// TODO set external id?
	int podioId = getAPI(item.getAppId(), ItemAPI.class).addItem(item.getAppId(), itemCreate, false);
	item.setPodioId(podioId);
	return podioId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #getItemById(java.lang.Class, java.lang.Integer)
     */
    public <T extends AppWrapper> T getItemById(Class<T> type, Integer podioId) {
	T result = null;
	Item item = null;
	try {
	    result = newAppWrapper(type, null);
	    item = getAPI(result.getAppId(), ItemAPI.class).getItem(podioId);
	    result.setValue(item);
	} catch (PodioApiWrapperException e1) {
	    LOGGER.log(Level.SEVERE, "Could not create AppWrapper of type=" + type, e1);
	} catch (ParseException e) {
	    LOGGER.log(Level.SEVERE, "Could not parse item: " + item + " to type: " + type, e);
	}
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #getItemsById(java.lang.Class, java.util.List)
     */
    public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds) {
	return getItemsById(type, podioIds, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #getItemsById(java.lang.Class, java.util.List, javax.swing.JProgressBar)
     */
    public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds, JProgressBar progressBar) {
	if (progressBar != null) {
	    progressBar.setValue(progressBar.getMinimum());
	    progressBar.setString("downloading items");
	    progressBar.setMaximum(podioIds.size());
	}
	ArrayList<T> result = new ArrayList<T>(podioIds.size());
	for (int i = 0; i < podioIds.size(); i++) {
	    Integer id = podioIds.get(i);
	    result.add(getItemById(type, id));
	    if (progressBar != null)
		progressBar.setValue(i + 1);
	}
	if (progressBar != null)
	    progressBar.setString(null);
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #updateItem(T)
     */
    @SuppressWarnings("unchecked")
    public <T extends AppWrapper> T updateItem(T item) throws PodioConflictException {
	try {
	    getAPI(item.getAppId(), ItemAPI.class)
		    .updateItem(item.getPodioId().intValue(), item.getItemCreate(), false, false);
	    return (T) getItemById(item.getClass(), item.getPodioId());
	} catch (APIApplicationException e) {
	    if (e.getError() != null && e.getError().equals("conflict")) {
		throw new PodioConflictException(e);
	    } else {
		throw e;
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #updateItems(java.util.List)
     */
    public <T extends AppWrapper> List<T> updateItems(List<T> items) throws ParseException, PodioConflictException {
	return updateItems(items, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #updateItems(java.util.List, javax.swing.JProgressBar)
     */
    public <T extends AppWrapper> List<T> updateItems(Collection<T> items, JProgressBar progressBar)
	    throws ParseException, PodioConflictException {
	LOGGER.info("Updateing " + items.size() + " items.");
	if (progressBar != null) {
	    progressBar.setValue(progressBar.getMinimum());
	    progressBar.setString("sending updates");
	    progressBar.setMaximum(items.size());
	}

	List<T> updatedItems = new ArrayList<T>();
	try {
	    int i = 1;
	    for (T t : items) {
		LOGGER.info("Update item: " + t.toString());
		T updatedItem = updateItem(t);
		updatedItems.add(updatedItem);
		if (progressBar != null)
		    progressBar.setValue(i++);
	    }
	} catch (APIApplicationException e) {
	    if (e.getError() != null && e.getError().equals("conflict")) {
		throw new PodioConflictException(e);
	    } else {
		throw e;
	    }
	}
	if (progressBar != null)
	    progressBar.setString(null);
	return updatedItems;
    }

    /**
     *
     * @param app
     * @param filter attributes 'offset' and 'limit' are assumed not to be set (this method fetches _all_ filtered items!)
     * @param <T>
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
	public <T extends AppWrapper> List<T> filterAllItems(Class<T> app, ItemFilter filter) throws InterruptedException, ExecutionException {
		List<T> result = new ArrayList<T>();

		ExecutorService executor = Executors.newCachedThreadPool();

                filter.setLimit(DEFAULT_OFFSET);
                filter.setOffset(0);
                FilterItemsJob<T> firstResult = new FilterItemsJob<>(app, filter);
		FutureTask<List<T>> firstTask = new FutureTask<>(firstResult);
		executor.execute(firstTask);
		do {
			Thread.sleep(10);
		} while (!firstTask.isDone());
		int total = firstResult.getTotal();
		result.addAll(firstTask.get());

		List<FutureTask<List<T>>> tasks = new ArrayList<>();
		for (int offset = DEFAULT_OFFSET; offset < total; offset += DEFAULT_OFFSET) {
                        ItemFilter offsetFilter = new ItemFilter(filter);
                        offsetFilter.setOffset(offset);
			tasks.add(new FutureTask<>(new FilterItemsJob<>(app, offsetFilter)));
		}

		for (FutureTask<List<T>> task : tasks) {
			executor.execute(task);
		}
		boolean finished;
		do {
			Thread.sleep(100);
			finished = true;
			for (FutureTask<List<T>> task : tasks) {
				if (!task.isDone()) {
					finished = false;

				}
			}
		} while (!finished);
		for (FutureTask<List<T>> task : tasks) {
			result.addAll(task.get());
		}
		return result;
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #getAllItems(java.lang.Class)
     */
    public <T extends AppWrapper> List<T> getAllItems(Class<T> app) throws InterruptedException, ExecutionException {
		return filterAllItems(app, new ItemFilter()); // TODO validate same behavior as before!
    }

        protected class FilterItemsJob<T extends AppWrapper> extends FetchItemsJob<T> {

                private final ItemFilter filter;

                public FilterItemsJob(Class<T> type, ItemFilter filter) {
                        super(type);
                        this.filter = filter;
                }

                public List<T> call() throws Exception {
                        Integer appId = computeAppIdFromType();
                        ItemsResponse response = getAPI(appId, ItemAPI.class).filterItems(appId, filter);
                        return transformItemsResponse(response);
                }

        }

    protected abstract class FetchItemsJob<T extends AppWrapper> implements Callable<List<T>> {
            protected int total;
            protected final Class<T> type;

            protected FetchItemsJob(Class<T> type) {
                    this.type = type;
            }

            protected List<T> transformItemsResponse(ItemsResponse response) throws PodioApiWrapperException {
                    List<T> result = new ArrayList<T>();
                    total = response.getFiltered();
                    LOGGER.info("filtered: " + response.getFiltered() + " / total: " + response.getTotal());
                    for (ItemBadge itemBadge : response.getItems()) {
                            Item item = PodioMapper.toItem(itemBadge);
                            result.add(newAppWrapper(type, item));
                    }
                    return result;
            }

            public Integer computeAppIdFromType() throws PodioApiWrapperException {
                    Integer appId = newAppWrapper(type, null).getAppId();
                    if (appId == null) {
                            throw new IllegalStateException("Could not get App-Id for type=" + type);
                    }
                    return appId;
            }

            public int getTotal() {
                    return total;
            }
    }
    /**
     * Job retrieving N (default: 500) items.
     * 
     * @param <T>
     */
    protected class GetItems<T extends AppWrapper> extends FetchItemsJob<T> {

	private final FilterByValue<?>[] filters;
	private int offset;
	private int amount = 500;

	public GetItems(int offset, Class<T> type, FilterByValue<?>... filters) {
                super(type);
                this.offset = offset;
		this.filters = filters;
	}

	public List<T> call() throws Exception {
            Integer appId = computeAppIdFromType();
	    ItemsResponse response = getAPI(appId, ItemAPI.class).getItems(appId, amount, offset, null, null, filters);
            return transformItemsResponse(response);
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.java_podio.code_gen.static_interface.GenericPodioInterfaceInterface
     * #getAssociatedItems(java.lang.Class, java.lang.String, java.lang.Integer)
     */
    public <T extends AppWrapper> List<T> getAssociatedItems(Class<T> type, final String fieldId, Integer podioId)
	    throws PodioApiWrapperException {
	int appId = newAppWrapper(type, null).getAppId();
	ItemsResponse response = getAPI(appId, ItemAPI.class).getItems(appId, 500, 0, null, null,
		new FilterByValue<Integer>(new FilterBy<Integer>() {

		    public String getKey() {
			return fieldId; // TODO check: "kunde" ?
		    }

		    public String format(Integer value) {
			return value.toString();
		    }

		    public Integer parse(String value) {
			return Integer.parseInt(value);
		    }
		}, podioId));
	// ItemsResponse response = getAPI(appId, ItemAPI.class).getItems(appId,
	// 500, 0, null, null,
	// new FilterByValue<String>(new FilterBy<String>() {
	//
	// public String getKey() {
	// return String.valueOf(CHANGE_APP_CUSTOMER_FIELD_ID);
	// }
	//
	// public String format(String value) {
	// return value;
	// }
	//
	// public String parse(String value) {
	// return value;
	// }
	// }, String.valueOf(podioId)));
	List<T> result = new ArrayList<T>(response.getTotal());
	for (ItemBadge itemBadge : response.getItems()) {
	    result.add(newAppWrapper(type, PodioMapper.toItem(itemBadge)));
	}
	return result;
    }

    /**
     * Creates a new {@link AppWrapper} instance of type T with values from
     * {@code item}.
     * 
     * @param type
     * @param item
     *            might be {@code null} - this results in the default
     *            constructor.
     * @throws PodioApiWrapperException
     * @return
     */
    public static <T extends AppWrapper> T newAppWrapper(Class<T> type, Item item) throws PodioApiWrapperException {
	T result = null;
	try {
	    if (item != null) {
		result = type.getConstructor(Item.class).newInstance(item);
	    } else {
		result = type.newInstance();
	    }
	} catch (Exception e) {
	    throw new PodioApiWrapperException("Could not instanciate type=" + type + " with item.id=" + item.getId(),
		    e);
	}
	return result;
    }

}
