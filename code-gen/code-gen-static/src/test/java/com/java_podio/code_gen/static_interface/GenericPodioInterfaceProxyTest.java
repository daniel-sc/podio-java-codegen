package com.java_podio.code_gen.static_interface;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.java_podio.code_gen.static_classes.AppWrapper;
import com.podio.item.Item;

public class GenericPodioInterfaceProxyTest {

    GenericPodioInterface proxy;

    @Mock
    GenericPodioImpl genericInterface;

    MyAppWrapper appObject1 = new MyAppWrapper();
    MyAppWrapper appObject2 = new MyAppWrapper();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);


	appObject1.setPodioRevision(10);
	appObject1.setPodioId(1);
	appObject1.setOriginalItem(new Item());
	appObject1.getOriginalItem().setId(12123);
	appObject2.setPodioRevision(20);
	appObject2.setPodioId(2);

	List<MyAppWrapper> allItems = new ArrayList<MyAppWrapper>();
	allItems.add(appObject1);
	allItems.add(appObject2);
	
	when(genericInterface.getAllItems(MyAppWrapper.class)).thenReturn(allItems);
	when(genericInterface.updateItem(any(AppWrapper.class))).thenAnswer(new Answer<AppWrapper>() {
	    public AppWrapper answer(InvocationOnMock invocation) {
		Object[] args = invocation.getArguments();
//		Object mock = invocation.getMock();
		AppWrapper item = (AppWrapper) args[0];
		item.setPodioId(item.getPodioId() + 1);
		return item;
	    }
	});
	proxy = GenericPodioInterfaceProxy.proxify(genericInterface);
    }

    @Test
    public void testEquals() {
	appObject1.setPodioRevision(20);
	appObject1.setPodioId(2);
	Assert.assertEquals(appObject1, appObject2);

	appObject1.setPodioRevision(null);
	Assert.assertFalse(appObject1.equals(appObject2));
    }

    @Test
    public void testCopyItem() throws ClassNotFoundException, IOException {
	AppWrapper copy = GenericPodioInterfaceProxy.copyItem(appObject1);
	Assert.assertEquals(copy, appObject1);
	Assert.assertTrue(copy != appObject1);
	copy.setPodioTitle("TEST");
	Assert.assertFalse(copy.equals(appObject1));
    }

    @Test
    public void testCache() {
	GenericPodioInterfaceProxy.cacheItem(MyAppWrapper.class, appObject1);

	Assert.assertEquals(appObject1,
		GenericPodioInterfaceProxy.getCachedItem(MyAppWrapper.class, appObject1.getPodioId()));

	Assert.assertNull(GenericPodioInterfaceProxy.getCachedItem(AppWrapper.class, 123));
	Assert.assertNull(GenericPodioInterfaceProxy.getCachedItem(AppWrapper.class, appObject1.getPodioId()));
	Assert.assertNull(GenericPodioInterfaceProxy.getCachedItem(MyAppWrapper.class, 123));
    }

    @Test
    public void testGetAllItems() throws InterruptedException, ExecutionException {
	
	GenericPodioInterfaceProxy.printCache();

	List<MyAppWrapper> allItems = proxy.getAllItems(MyAppWrapper.class);

	GenericPodioInterfaceProxy.printCache();
	
	System.out.println("allItems: " + allItems);

	for (MyAppWrapper item : allItems) {
	    Assert.assertEquals(item, GenericPodioInterfaceProxy.getCachedItem(MyAppWrapper.class, item.getPodioId()));
	}

	appObject1.setText("asdf");

	Assert.assertFalse(isCached(MyAppWrapper.class, appObject1));
    }

    @Test
    public void testNonProxyMethod() {
	List<Integer> podioIds = new ArrayList<Integer>();
	podioIds.add(123);
	podioIds.add(435434);
	proxy.getItemsById(MyAppWrapper.class, podioIds);

	Mockito.verify(genericInterface).getItemsById(MyAppWrapper.class, podioIds);
    }

    @Test
    public void testUpdateItems() throws InterruptedException, ExecutionException, ParseException,
	    PodioConflictException {
	List<MyAppWrapper> allItems = proxy.getAllItems(MyAppWrapper.class);
	Assert.assertTrue(allItems.size() >= 2);
	MyAppWrapper newitem = new MyAppWrapper();
	newitem.setPodioId(99);
	allItems.add(newitem);
	proxy.updateItems(allItems);
	verify(genericInterface, times(0)).updateItem(any(AppWrapper.class));
    }

    @Test
    public void testUpdate() throws InterruptedException, ExecutionException, PodioConflictException {
	List<MyAppWrapper> allItems = proxy.getAllItems(MyAppWrapper.class);
	for (MyAppWrapper item : allItems) {
	    proxy.updateItem(item);
	    Mockito.verify(genericInterface, Mockito.times(0)).updateItem(item);
	}

	MyAppWrapper item = allItems.get(0);
	item.setText("ASDF");
	proxy.updateItem(item);
	Mockito.verify(genericInterface).updateItem(item);
    }

    private static boolean isCached(Class<? extends AppWrapper> type, AppWrapper item) {
	return item.equals(GenericPodioInterfaceProxy.getCachedItem(type, item.getPodioId()));
    }

}
