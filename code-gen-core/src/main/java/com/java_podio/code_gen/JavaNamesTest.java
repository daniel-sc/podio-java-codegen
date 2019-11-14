package com.java_podio.code_gen;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class JavaNamesTest {

    private static final String CONTEXT = "my-context";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testCreateValidJavaTypeName() {
	String result1 = JavaNames.createValidJavaTypeName("Ja", CONTEXT);
	String result2 = JavaNames.createValidJavaTypeName("Ja", CONTEXT);
	System.out.println(result1);
	System.out.println(result2);
	Assert.assertFalse("same input should yield differen names!", result1.equals(result2));
    }

}
