package com.java_podio.code_gen.static_interface;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;

import com.java_podio.code_gen.static_classes.AppWrapper;
import com.podio.item.ItemAPI;

/**
 * This is needed for {@link GenericPodioInterfaceProxy}.
 */
public interface GenericPodioInterface {

    /**
     * @param file
     * @param appId
     * @param filename
     * @return fileId
     */
    public int uploadFile(File file, int appId, String filename);

    /**
     * @param item
     *            podioId will be set for this object
     * @return podioId of the created item
     */
    public int addItem(AppWrapper item);

    public <T extends AppWrapper> T getItemById(Class<T> type, Integer podioId);

    /**
     * TODO: speed up by doing only one remote call!
     * 
     * @param type
     * @param podioIds
     * @return
     */
    public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds);

    /**
     * TODO: speed up by doing only one remote call!
     * 
     * @param type
     * @param podioIds
     * @return
     */
    public <T extends AppWrapper> List<T> getItemsById(Class<T> type, List<Integer> podioIds, JProgressBar progressBar);

    /**
     * Updates item with all fields. Assumes {@link com.java_podio.code_gen.static_classes.AppWrapper#getPodioId()}
     * is set.
     * 
     * @param item
     * @return the updated item, this contains the new revision
     * @throws PodioConflictException
     */
    public <T extends AppWrapper> T updateItem(T item) throws PodioConflictException;

    /**
     * All items are assumed to be of the same type!
     * 
     * @see #updateItem(AppWrapper)
     * @param items
     * @return
     * @throws ParseException
     * @throws PodioConflictException
     */
    public <T extends AppWrapper> List<T> updateItems(List<T> items) throws ParseException, PodioConflictException;

    /**
     * All items are assumed to be of the same type!
     * 
     * @see #updateItem(AppWrapper)
     * @param items
     * @param progressBar
     * @return
     * @throws ParseException
     * @throws PodioConflictException
     */
    public <T extends AppWrapper> List<T> updateItems(Collection<T> items, JProgressBar progressBar) throws ParseException,
	    PodioConflictException;

    /**
     * Fetches all entries for an app from Podio. This is done parallel in
     * chunks of 500 (which is the maximum).
     *
     * Note: this uses some undocumented URI ( ItemAPI#getItems() ) - the resulting items are missing e.g. the files attribute.
     * 
     * @param app
     *            generated class of app
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public <T extends AppWrapper> List<T> getAllItems(Class<T> app) throws InterruptedException, ExecutionException;

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
    public <T extends AppWrapper> List<T> getAssociatedItems(Class<T> type, String fieldId, Integer podioId)
	    throws PodioApiWrapperException;

}