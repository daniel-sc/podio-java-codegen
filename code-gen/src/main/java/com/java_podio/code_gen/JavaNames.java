package com.java_podio.code_gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.CaseFormat;

public class JavaNames {

	private static Map<String, Set<String>> allNames = new HashMap<String, Set<String>>();

	public JavaNames() {
	}

	/**
	 * FIXME assure names are not assigned twice in the same context!
	 * 
	 * @param name
	 *            might contain spaces and special characters.
	 * @param context
	 *            it is assured, that for subsequent calls no name is created twice for the same context.
	 * @return valid upper camel case java type name
	 */
	public static String createValidJavaTypeName(String name, String context) {
		name = name.toLowerCase().replace(' ', '-').replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
		name = name.replaceAll("[^-a-zA-Z]", "");
		name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
		
		if(!allNames.containsKey(context)) {
			allNames.put(context, new HashSet<String>());
		}
		
		//TODO
		
		return name;
	}

}
