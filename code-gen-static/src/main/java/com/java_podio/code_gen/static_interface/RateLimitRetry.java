package com.java_podio.code_gen.static_interface;

import com.podio.APIApplicationException;

import javax.net.ssl.SSLProtocolException;
import javax.ws.rs.ProcessingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RateLimitRetry {

    private static final int NUMBER_OF_RETRIES = 3;

    public static class RetriesFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RetriesFailedException(String message, Throwable cause) {
	    super(message, cause);
	}

    }

    private static class RateLimitInvokationHandler<T extends GenericPodioInterface> implements InvocationHandler {

	private final T original;
	private final boolean retrySslError;

            public RateLimitInvokationHandler(T original, Class<T> interfaceType, boolean retrySslError) {
	    this.original = original;
	    this.retrySslError = retrySslError;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	    for (int i = 0; true; i++) {
		try {
		     return method.invoke(original, args);
		} catch (InvocationTargetException e) {
		    if (i == NUMBER_OF_RETRIES) {
			LOGGER.warning("giving up after " + NUMBER_OF_RETRIES + " retries.");
			throw new RetriesFailedException("giving up after " + NUMBER_OF_RETRIES + " retries.", e);
		    } else if (e.getCause() instanceof APIApplicationException
			    && "rate_limit".equals(((APIApplicationException) e.getCause()).getError())) {
			APIApplicationException exc = ((APIApplicationException) e.getCause());
			Pattern pattern = Pattern.compile("Please wait (\\d+) seconds", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(exc.getDescription());
			if (matcher.find()) {
			    String seconds = matcher.group(1);
			    LOGGER.info("should wait for " + seconds + " seconds..");
			    int secondsInt = Integer.parseInt(seconds);
			    for (RateLimitHitListener l : listener) {
				l.hitRateLimit(i, secondsInt);
			    }
			    Thread.sleep(secondsInt * 1000);
			    LOGGER.info("waking up, trying again..");
			} else {
			    LOGGER.warning("could not parse wait time from: " + exc.getDescription() + " (throwing exception)");
			    throw e;
			}
		    } else if (retrySslError && e.getTargetException() instanceof ProcessingException
                            && (e.getTargetException().getCause() instanceof SSLProtocolException
                            || e.getTargetException().getCause() instanceof ConnectException
                            || (e.getTargetException().getCause().getCause() instanceof SSLProtocolException)
                            || (e.getTargetException().getCause().getCause() instanceof ConnectException))) {
                            LOGGER.info("Retrying SSL error in 10ms: " + e.getMessage());
                            Thread.sleep(10);
                    } else {
			LOGGER.log(Level.INFO, "non rate limit exception occured..", e);
			throw e;
		    }
		}
	    }
	}
    }

    public static interface RateLimitHitListener {
	/**
	 * Should return fast!
	 *
	 * @param retry
	 *            0 to {@link RateLimitRetry#NUMBER_OF_RETRIES} - 1
	 * @param waitSeconds
	 */
	void hitRateLimit(int retry, int waitSeconds);
    }

    private static final Logger LOGGER = Logger.getLogger(RateLimitRetry.class.getName());

    private static List<RateLimitHitListener> listener = new LinkedList<RateLimitRetry.RateLimitHitListener>();

    public static void addRateLimitHitListener(RateLimitHitListener l) {
	listener.add(l);
    }

    public static void removeRateLimitHitListener(RateLimitHitListener l) {
	listener.remove(l);
    }

    /**
     * @param original
     * @param interfaceType
     *            must be interface!
     * @param retrySslError if <code>true</code> ssl errors will as well be retried
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends GenericPodioInterface> T proxify(T original, Class<T> interfaceType, boolean retrySslError) {
	LOGGER.info("Using GenericPodioInterfaceProxy on " + original.getClass().getCanonicalName());
	/*
	 * The proxy is necessary, since otherwise all functionality added by
	 * subclasses of GenericPodioImpl would be hidden!
	 */
	return (T) Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[] { interfaceType },
		new RateLimitInvokationHandler(original, interfaceType, retrySslError));
    }

    public static <T extends GenericPodioInterface> T proxify(T original, Class<T> interfaceType) {
	return proxify(original, interfaceType, false);
    }

}
