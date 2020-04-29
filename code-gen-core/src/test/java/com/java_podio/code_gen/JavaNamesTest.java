package com.java_podio.code_gen;

import org.junit.Assert;
import org.junit.Test;

public class JavaNamesTest {

        private static final String CONTEXT = "my-context";

        @Test
        public void testCreateValidJavaTypeName() {
                String result1 = JavaNames.createValidJavaTypeName("Ja", CONTEXT);
                String result2 = JavaNames.createValidJavaTypeName("Ja", CONTEXT);
                System.out.println(result1);
                System.out.println(result2);
                Assert.assertNotEquals("same input should yield different names!", result1, result2);
        }

}
