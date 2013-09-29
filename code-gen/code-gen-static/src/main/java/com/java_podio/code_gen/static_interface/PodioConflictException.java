package com.java_podio.code_gen.static_interface;

/**
 * Indicates that the current change could not be performed, since between the
 * revision of the local data and the remote data there were conflicting
 * changes.
 */
public class PodioConflictException extends Exception {

    private static final long serialVersionUID = 1L;

    public PodioConflictException(String arg0) {
	super(arg0);
    }

    public PodioConflictException(Throwable arg0) {
	super(arg0);
    }

    public PodioConflictException(String arg0, Throwable arg1) {
	super(arg0, arg1);
    }
}
