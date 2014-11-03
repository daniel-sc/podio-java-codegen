package com.java_podio.code_gen.static_interface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.podio.APIApplicationException;

public abstract class RateLimitRetry {

    private static final int NUMBER_OF_RETRIES = 3;

    public static class RetriesFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RetriesFailedException(String message, Throwable cause) {
	    super(message, cause);
	}

	public RetriesFailedException(String message) {
	    super(message);
	}

    }

    private static class RateLimitInvokationHandler<T extends GenericPodioInterface> implements InvocationHandler {

	private final T original;

	public RateLimitInvokationHandler(T original, Class<T> interfaceType) {
	    this.original = original;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	    for (int i = 0; i <= NUMBER_OF_RETRIES; i++) {
		try {
		    Object result = method.invoke(original, args);
		    return result;
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
			    LOGGER.warning("could not parse wait time from: " + exc.getDescription()
				    + " (throwing exception)");
			    throw e;
			}
		    } else {
			LOGGER.log(Level.INFO, "non rate limit exception occured..", e);
			throw e;
		    }
		}
	    }
	    LOGGER.severe("this should never happen!");
	    throw new RetriesFailedException("giving up after " + NUMBER_OF_RETRIES + " retries. (invalid code path!)");
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
		new RateLimitInvokationHandler(original, interfaceType));
    }

}
