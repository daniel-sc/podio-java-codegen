package com.java_podio.code_gen.static_interface;

/**
 * Indicates a general exception with the Podio API Wrapper classes.
 */
public class PodioApiWrapperException extends Exception {

    private static final long serialVersionUID = 1L;

    public PodioApiWrapperException() {
    }

    public PodioApiWrapperException(String arg0) {
	super(arg0);
    }

    public PodioApiWrapperException(Throwable arg0) {
	super(arg0);
    }

    public PodioApiWrapperException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }

    public PodioApiWrapperException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
	super(arg0, arg1, arg2, arg3);
    }

}
