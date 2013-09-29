package com.java_podio.code_gen.static_interface;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.java_podio.code_gen.static_classes.AppWrapper;
import com.podio.APIApplicationException;
import com.podio.BaseAPI;
import com.podio.file.FileAPI;
import com.podio.filter.FilterBy;
import com.podio.filter.FilterByValue;
import com.podio.item.Item;
import com.podio.item.ItemAPI;
import com.podio.item.ItemBadge;
import com.podio.item.ItemCreate;
import com.podio.item.ItemsResponse;

/**
 * Generic interface methods for generated {@link AppWrapper} classes.
 */
public abstract class GenericPodioInterface {

    private static final Logger LOGGER = Logger.getLogger(GenericPodioInterface.class.getName());

    public static SimpleDateFormat defaultDateFormatNoTime = new SimpleDateFormat("dd.MM.yyyy");
    public static SimpleDateFormat podioDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * User or app credentials might be used here.
     * 
     * @param appId
     * @param type
     *            api type
     * @return
     */
    protected abstract <T extends BaseAPI> T getAPI(Integer appId, Class<T> type);

    /**
     * @param file
     * @param appId
     * @param filename
     * @return fileId
     */
    public int uploadFile(File file, int appId, String filename) {
	return getAPI(appId, FileAPI.class).uploadFile(filename, file);
    }

    /**
     * @param item
     *            podioId will be set for this object
     * @return podioId of the created item
     */
    public int addItem(AppWrapper item) {
	ItemCreate itemCreate = item.getItemCreate();
	// TODO set external id?
	int podioId = getAPI(item.getAppId(), ItemAPI.class).addItem(item.getAppId(), itemCreate, false);
	item.setPodioId(podioId);
	return podioId;
    }

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

    /**
     * TODO: speed up by doing only one remote call!
     * 
     * @param type
     * @param podioIds
     * @return
     */
    public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds) {
	ArrayList<T> result = new ArrayList<T>(podioIds.size());
	for (Integer id : podioIds) {
	    result.add(getItemById(type, id));
	}
	return result;
    }

    /**
     * Updates item with all fields. Assumes {@link item#getPodioCustomerID()}
     * is set.
     * 
     * @param item
     * @return the updated item, this contains the new revision
     * @throws PodioConflictException
     */
    @SuppressWarnings("unchecked")
    public <T extends AppWrapper> T updateItem(T item) throws PodioConflictException {
	try {
	    getAPI(item.getAppId(), ItemAPI.class)
		    .updateItem(item.getPodioId().intValue(), item.getItemCreate(), false);
	    return (T) getItemById(item.getClass(), item.getPodioId());
	} catch (APIApplicationException e) {
	    if (e.getError() != null && e.getError().equals("conflict")) {
		throw new PodioConflictException(e);
	    } else {
		throw e;
	    }
	}
    }

    /**
     * All items are assumed to be of the same type!
     * 
     * @see #updateItem(AppWrapper)
     * @param items
     * @return
     * @throws ParseException
     * @throws PodioConflictException
     */
    @SuppressWarnings("unchecked")
    public <T extends AppWrapper> List<T> updateItems(List<T> items) throws ParseException, PodioConflictException {
	Class<T> type = null;
	if (items.size() == 0)
	    return new ArrayList<T>();
	else
	    type = (Class<T>) items.get(0).getClass();
	List<Integer> podioIds = new ArrayList<Integer>();
	try {
	    for (T t : items) {
		LOGGER.info("Update item: " + t.toString());
		updateItem(t);
		podioIds.add(t.getPodioId());
	    }
	} catch (APIApplicationException e) {
	    if (e.getError() != null && e.getError().equals("conflict")) {
		throw new PodioConflictException(e);
	    } else {
		throw e;
	    }
	}

	return getItemsById(type, podioIds);
    }

    /**
     * Fetches all entries for an app from Podio. This is done parallel in
     * chunks of 500 (which is the maximum).
     * 
     * @param app
     *            generated class of app
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public <T extends AppWrapper> List<T> getAllItems(Class<T> app) throws InterruptedException, ExecutionException {
	List<T> result = new ArrayList<T>();

	ExecutorService executor = Executors.newCachedThreadPool();

	GetItems<T> firstResult = new GetItems<T>(0, app);
	FutureTask<List<T>> firstTask = new FutureTask<List<T>>(firstResult);
	executor.execute(firstTask);
	do {
	    Thread.sleep(10);
	} while (!firstTask.isDone());
	int total = firstResult.getTotal();
	result.addAll(firstTask.get());

	List<FutureTask<List<T>>> tasks = new ArrayList<FutureTask<List<T>>>();
	for (int offset = 500; offset < total; offset += 500) {
	    tasks.add(new FutureTask<List<T>>(new GetItems<T>(offset, app)));
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

    /**
     * Job retrieving N (default: 500) items.
     * 
     * @param <T>
     */
    protected class GetItems<T extends AppWrapper> implements Callable<List<T>> {

	private int offset;
	private int total;
	private Class<T> type;
	private int amount = 500;

	@SuppressWarnings("unused")
	public void setAmount(int amount) {
	    this.amount = amount;
	}

	public GetItems(int offset, Class<T> type) {
	    this.offset = offset;
	    this.type = type;
	}

	public List<T> call() throws Exception {
	    List<T> result = new ArrayList<T>();

	    Integer appId = newAppWrapper(type, null).getAppId();
	    if (appId == null) {
		throw new IllegalStateException("Could not get App-Id for type=" + type);
	    }
	    ItemsResponse response = getAPI(appId, ItemAPI.class).getItems(appId, amount, offset, null, null);
	    total = response.getTotal();
	    for (ItemBadge itemBadge : response.getItems()) {
		Item item = PodioMapper.toItem(itemBadge);
		result.add(newAppWrapper(type, item));
	    }
	    return result;
	}

	public int getTotal() {
	    return total;
	}

    }

    /**
     * This directly queries the given app ({@code type} and field.
     * 
     * For all references employ {@link ItemAPI#getItemReference(int)} (which
     * will probably be slower, as it needs 2 remote calls).
     * 
     * @param type
     *            app of items that are returned
     * @param fieldId
     *            id of referencing field in app {@code type}. Can be field id or field external id.
     * @param podioId
     *            referenced item (might not be in app {@code type}!)
     * @return list of items from app {@code type} that reference
     *         {@code podioId} in field {@code fieldId}
     * @throws PodioApiWrapperException
     */
    public <T extends AppWrapper> List<T> getAssociatedItems(Class<T> type, final String fieldId, Integer podioId)
            throws PodioApiWrapperException {
        int appId = newAppWrapper(type, null).getAppId();
        ItemsResponse response = getAPI(appId, ItemAPI.class).getItems(appId, 500, 0, null, null,
        	new FilterByValue<Integer>(new FilterBy<Integer>() {
    
        	    public String getKey() {
        		return fieldId; //TODO check: "kunde" ?
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
	    throw new PodioApiWrapperException("Could not instanciate type=" + type + " with item=" + item, e);
	}
	return result;
    }

}
