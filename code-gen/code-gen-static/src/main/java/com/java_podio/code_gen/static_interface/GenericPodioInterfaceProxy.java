package com.java_podio.code_gen.static_interface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JProgressBar;

import com.java_podio.code_gen.static_classes.AppWrapper;
import com.podio.item.filter.ItemFilter;

/**
 * This is a proxy/cache for podio objects. It immediately delegates all calls -
 * except unnecessary updates are suppressed.<br>
 * 
 * <b>Note:</b> any methods of subclasses of {@link GenericPodioImpl} that
 * directly use {@link GenericPodioImpl#getAPI(Integer, Class)} bypass the
 * caching!
 */
public abstract class GenericPodioInterfaceProxy {

    private static final Logger LOGGER = Logger.getLogger(GenericPodioInterfaceProxy.class.getName());

    /**
     * AppType -> (PodioId -> AppTypeObject)
     */
    private static Map<Class<? extends AppWrapper>, Map<Integer, AppWrapper>> cache = new HashMap<Class<? extends AppWrapper>, Map<Integer, AppWrapper>>();

    static {
	cache = Collections.synchronizedMap(cache);
    }

    /**
     * @param type
     * @param podioId
     * @return cached object or {@code null}, if no object is found or
     *         {@code podioId==null}
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AppWrapper> T getCachedItem(Class<T> type, Integer podioId) {
	if (!cache.containsKey(type))
	    cache.put(type, Collections.synchronizedMap(new HashMap<Integer, AppWrapper>()));
	if (podioId == null)
	    return null;
	return (T) cache.get(type).get(podioId);
    }

    /**
     * Returns the cached item that is equal with respect to
     * {@code item.getPodioId()}, or {@code null}.
     * 
     * @param item
     * @return
     */
    @SuppressWarnings("unchecked")
    protected static <T extends AppWrapper> T getCachedItem(T item) {
	return (T) getCachedItem(item.getClass(), item.getPodioId());
    }

    /**
     * @param item
     * @return {@code true} if the cache contains an item that is equal to the
     *         parameter.
     */
    protected static <T extends AppWrapper> boolean isCached(T item) {
	return item.equals(getCachedItem(item));
    }

    /**
     * @param type
     * @param item
     * @throws IllegalArgumentException
     *             if {@code item.getPodioId()==null}
     */
    protected static <T extends AppWrapper> void cacheItem(Class<T> type, AppWrapper item) {
	if (!cache.containsKey(type))
	    cache.put(type, Collections.synchronizedMap(new HashMap<Integer, AppWrapper>()));
	if (item.getPodioId() == null)
	    throw new IllegalArgumentException("Cannot cache item without podio id!");

	try {
	    cache.get(type).put(item.getPodioId(), copyItem(item));
	} catch (ClassNotFoundException e) {
	    LOGGER.log(Level.SEVERE, "could not (de-)serialize item!", e);
	} catch (IOException e) {
	    LOGGER.log(Level.SEVERE, "could not (de-)serialize item!", e);
	}
    }

    protected static <T extends AppWrapper> void cacheItems(Collection<T> items) {
	for (T item : items) {
	    cacheItem(item.getClass(), item);
	}
    }

    @SuppressWarnings("unchecked")
    protected static <T extends AppWrapper> T copyItem(T item) throws IOException, ClassNotFoundException {
	ByteArrayOutputStream baOs = new ByteArrayOutputStream();
	ObjectOutputStream oOs = new ObjectOutputStream(baOs);
	oOs.writeObject(item);
	ByteArrayInputStream bais = new ByteArrayInputStream(baOs.toByteArray());
	ObjectInputStream ois = new ObjectInputStream(bais);
	return (T) ois.readObject();
    }

    public static void printCache() {
	System.out.println("Cache: " + cache);
    }

    /**
     * @param original
     * @param interfaceType
     *            must be interface!
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends GenericPodioInterface> T proxify(T original, Class<T> interfaceType) {
	LOGGER.info("Using GenericPodioInterfaceProxy on " + original.getClass().getCanonicalName());
	/*
	 * The proxy is necessary, since otherwise all functionality added by
	 * subclasses of GenericPodioImpl would be hidden!
	 */
	return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[] { interfaceType },
		new GenericPodioInterfaceInvokationHandler(original, interfaceType));
    }

    /**
     * @see #proxify(GenericPodioInterface, Class)
     * @param original
     * @return
     */
    public static GenericPodioInterface proxify(GenericPodioInterface original) {
	return proxify(original, GenericPodioInterface.class);
    }

    public static void clearCache() {
	cache.clear();
    }

    private static class GenericPodioInterfaceInvokationHandler<T extends GenericPodioInterface> implements
	    InvocationHandler {

	private final T original;

	private final GenericPodioInterface podioProxy;

	public GenericPodioInterfaceInvokationHandler(T original, Class<T> interfaceType) {
	    this.original = original;
	    podioProxy = new PodioProxy(original);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	    Method proxyMethod = existsMethod(podioProxy, method);
	    if (proxyMethod != null) {
		return proxyMethod.invoke(podioProxy, args);
	    } else {
		return method.invoke(original, args);
	    }

	}

	/**
	 * @param object
	 * @param method
	 * @return {@code null}, if method does not exist for object. Otherwise
	 *         the corresponding method is returned.
	 */
	private Method existsMethod(Object object, Method method) {
	    try {
		return object.getClass().getMethod(method.getName(), method.getParameterTypes());
	    } catch (NoSuchMethodException e) {
		return null;
	    }
	}
    }

    /**
     * Performs proxy operations and delegates then to original instance.
     */
    private static class PodioProxy implements GenericPodioInterface {

	private GenericPodioInterface original;

	public PodioProxy(GenericPodioInterface original) {
	    this.original = original;
	}

	public int uploadFile(File file, int appId, String filename) {
	    return original.uploadFile(file, appId, filename);
	}

	public int addItem(AppWrapper item) {
	    int podioId = original.addItem(item);
	    cacheItem(item.getClass(), item);
	    return podioId;
	}

	public <T extends AppWrapper> T getItemById(Class<T> type, Integer podioId) {
	    T result = original.getItemById(type, podioId);
	    cacheItem(type, result);
	    return result;
	}

	public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds) {
	    List<T> result = original.getItemsById(type, podioIds);
	    cacheItems(result);
	    return result;
	}

	public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds,
		JProgressBar progressBar) {
	    List<T> result = original.getItemsById(type, podioIds);
	    cacheItems(result);
	    return result;
	}

	public <T extends AppWrapper> List<T> updateItems(List<T> items) throws ParseException, PodioConflictException {
	    List<T> unchangedItems = new ArrayList<T>();
	    List<T> changedItems = new ArrayList<T>();
	    for (T item : items) {
		if (!isCached(item))
		    changedItems.add(item);
		else
		    unchangedItems.add(item);
	    }
	    LOGGER.info("Cached items: " + unchangedItems);
	    printCache();
	    changedItems = original.updateItems(changedItems);
	    cacheItems(changedItems);
	    changedItems.addAll(unchangedItems);
	    LOGGER.info("UnCached items: " + changedItems);
	    LOGGER.info("saved update calls for PodioIds: " + unchangedItems.size());
	    return changedItems;
	}

	public <T extends AppWrapper> T updateItem(T item) throws PodioConflictException {
	    if (isCached(item)) {
		LOGGER.info("saved update call for PodioId: " + item.getPodioId());
		return (T) item;
	    }
	    T updatedItem = original.updateItem(item);
	    cacheItem(updatedItem.getClass(), updatedItem);
	    return updatedItem;
	}

	public <T extends AppWrapper> List<T> updateItems(Collection<T> items, JProgressBar progressBar)
		throws ParseException, PodioConflictException {
	    List<T> unchangedItems = new ArrayList<T>();
	    List<T> changedItems = new ArrayList<T>();
	    for (T item : items) {
		if (!isCached(item))
		    changedItems.add(item);
		else
		    unchangedItems.add(item);
	    }
	    LOGGER.info("Cached items: " + unchangedItems);
	    LOGGER.info("UnCached items: " + changedItems);
	    printCache();
	    changedItems = original.updateItems(changedItems, progressBar);
	    cacheItems(changedItems);
	    changedItems.addAll(unchangedItems);
	    LOGGER.info("saved update calls for PodioIds: " + unchangedItems.size());
	    return changedItems;
	}

        @Override
        public <T extends AppWrapper> List<T> filterAllItems(Class<T> app, ItemFilter filter) throws InterruptedException, ExecutionException {
                LOGGER.info("Cacheing..");
                List<T> result = original.filterAllItems(app, filter);
                cacheItems(result);
                return result;
        }

        public <T extends AppWrapper> List<T> getAllItems(Class<T> app) throws InterruptedException, ExecutionException {
	    LOGGER.info("Cacheing..");
	    List<T> result = original.getAllItems(app);
	    cacheItems(result);
	    return result;
	}

	public <T extends AppWrapper> List<T> getAssociatedItems(Class<T> type, String fieldId, Integer podioId)
		throws PodioApiWrapperException {
	    List<T> result = original.getAssociatedItems(type, fieldId, podioId);
	    cacheItems(result);
	    return result;
	}

    }
}
