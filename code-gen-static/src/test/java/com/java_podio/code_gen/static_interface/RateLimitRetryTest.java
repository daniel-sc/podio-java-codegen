package com.java_podio.code_gen.static_interface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.java_podio.code_gen.static_interface.RateLimitRetry.RetriesFailedException;
import com.podio.APIApplicationException;

import javax.ws.rs.core.Response;

public class RateLimitRetryTest {

    private static final Logger LOGGER = Logger.getLogger(RateLimitRetryTest.class.getName());

    @Mock
    GenericPodioImpl genericInterface;

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProxifyNoSuccess() throws IOException {
	Mockito.when(genericInterface.uploadFile(Mockito.any(File.class), Mockito.anyInt(), Mockito.anyString()))
		.thenThrow(
			new APIApplicationException(Response.Status.TOO_MANY_REQUESTS, "rate_limit",
				"You have hit the rate limit. Please wait 2 seconds before trying again", null));

	GenericPodioInterface rateLimitProxy = RateLimitRetry.proxify(genericInterface, GenericPodioInterface.class);
	File tempfile = Files.createTempFile("pref", null).toFile();
	try {
	    rateLimitProxy.uploadFile(tempfile, 0, "myfilename");
	    Assert.fail("attemp should fail after n retries!");
	} catch (RetriesFailedException e) {
	    // expected
	    LOGGER.log(Level.INFO, "expected exception:", e);
	}
    }

    @Test
    public void testProxifySecondTrySuccess() throws IOException {
	Mockito.when(genericInterface.uploadFile(Mockito.any(File.class), Mockito.anyInt(), Mockito.anyString()))
		.thenAnswer(new Answer<Integer>() {

		    int count = 0;

		    public Integer answer(InvocationOnMock invocation) throws Throwable {
			if (count++ == 0) {
			    throw new APIApplicationException(Response.Status.TOO_MANY_REQUESTS, "rate_limit",
				    "You have hit the rate limit. Please wait 2 seconds before trying again", null);
			} else {
			    return 6;
			}
		    }
		});

	GenericPodioInterface rateLimitProxy = RateLimitRetry.proxify(genericInterface, GenericPodioInterface.class);
	File tempfile = Files.createTempFile("pref", null).toFile();

	int result = rateLimitProxy.uploadFile(tempfile, 0, "myfilename");
	Assert.assertEquals(6, result);
    }

}
