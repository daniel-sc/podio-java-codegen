package com.java_podio.code_gen;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.podio.app.ApplicationField;
import com.podio.app.ApplicationFieldType;

/**
 * Eventually all {@link ApplicationFieldType} types should be represented here.<br>
 */
public enum PodioType {

	UNDEFINED(Object.class), TEXT(String.class), NUMBER(Double.class), MONEY(Double.class), CATEGORY_SINGLE(EnumGenerator.class), APP(Integer.class), DATE(Date.class);

	private final Class<? extends Object> javaType;

	/**
	 * Should contain all literals!
	 */
	private static Map<PodioType, ApplicationFieldType> map = new HashMap<PodioType, ApplicationFieldType>();
	static {
		map.put(TEXT, ApplicationFieldType.TEXT);
		map.put(NUMBER, ApplicationFieldType.NUMBER);
		map.put(MONEY, ApplicationFieldType.MONEY);
		map.put(CATEGORY_SINGLE, ApplicationFieldType.CATEGORY);
		map.put(APP, ApplicationFieldType.APP);
		map.put(DATE, ApplicationFieldType.DATE);
	}

	private PodioType(Class<? extends Object> javaType) {
		this.javaType = javaType;
	}

	/**
	 * This needs to be mapped in some cases, such as {@link #MONEY}.
	 * 
	 * @return
	 */
	public Class<? extends Object> getJavaType() {
		return javaType;
	}

	/**
	 * @return {@code null} for {@link #UNDEFINED}.
	 */
	public ApplicationFieldType getApplicationFieldType() {
		return map.get(this);
	}

	public static PodioType forApplicationField(ApplicationField f) {
		if (ApplicationFieldType.CATEGORY.equals(map.get(f.getType())) && Boolean.TRUE.equals(f.getConfiguration().getSettings().getMultiple())) {
			return UNDEFINED; // Categories with multiple values are not supportet yet.
		}
		for (PodioType podioType : map.keySet()) {
			if (map.get(podioType).equals(f.getType())) {
				return podioType;
			}
		}
		return UNDEFINED;
	}
}
