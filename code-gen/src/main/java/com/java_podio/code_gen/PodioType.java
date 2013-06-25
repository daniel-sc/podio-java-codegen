package com.java_podio.code_gen;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.podio.app.ApplicationFieldType;

/**
 * Eventually all {@link ApplicationFieldType} types should be represented here.
 */
public enum PodioType {

	UNDEFINED(Object.class), TEXT(String.class), NUMBER(Double.class), MONEY(Double.class), CATEGORY(Integer.class), APP(
			Integer.class), DATE(Date.class);

	private final Class<? extends Object> javaType;

	/**
	 * Should contain all literals!
	 */
	private static Map<PodioType, ApplicationFieldType> map = new HashMap<PodioType, ApplicationFieldType>();
	static {
		map.put(TEXT, ApplicationFieldType.TEXT);
		map.put(NUMBER, ApplicationFieldType.NUMBER);
		map.put(MONEY, ApplicationFieldType.MONEY);
		map.put(CATEGORY, ApplicationFieldType.CATEGORY);
		map.put(APP, ApplicationFieldType.APP);
		map.put(DATE, ApplicationFieldType.DATE);
	}

	private PodioType(Class<? extends Object> javaType) {
		this.javaType = javaType;
	}

	public Class<? extends Object> getJavaType() {
		return javaType;
	}

	/**
	 * @return {@code null} for {@link #UNDEFINED}.
	 */
	public ApplicationFieldType getApplicationFieldType() {
		return map.get(this);
	}
	
	public static PodioType forApplicationFieldType(ApplicationFieldType type) {
		map.containsValue(type);
		for (PodioType podioType : map.keySet()) {
			if(map.get(podioType).equals(type)) {
				return podioType;
			}
		}
		return UNDEFINED;
	}
}
