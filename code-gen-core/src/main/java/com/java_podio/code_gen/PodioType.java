package com.java_podio.code_gen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.java_podio.code_gen.static_classes.PodioCurrency;
import com.java_podio.code_gen.static_classes.PodioDate;
import com.java_podio.code_gen.static_interface.GenericPodioInterfaceProxy;
import com.podio.app.ApplicationField;
import com.podio.app.ApplicationFieldType;

/**
 * Eventually all {@link ApplicationFieldType} types should be represented here.<br>
 * The java type is only a default and mostly used only for primitive types,
 * such as String and Double.
 */
public enum PodioType {

    UNDEFINED(Object.class),

    TEXT(String.class),

    NUMBER(Double.class),

    MONEY(PodioCurrency.class),

    CATEGORY_SINGLE(EnumGenerator.class),

    CATEGORY_MULTI(EnumGenerator.class),

    APP(List.class),

    DATE(PodioDate.class),

    DURATION(Integer.class),

    PROGRESS(Integer.class),

    CONTACT(List.class),

    /** used for links */
    EMBED(List.class);

    private static final Logger LOGGER = Logger.getLogger(PodioType.class.getName());

    private final Class<? extends Object> javaType;

    /**
     * Should contain all literals!
     */
    private static Map<PodioType, ApplicationFieldType> map = new HashMap<PodioType, ApplicationFieldType>();
    static {
	map.put(TEXT, ApplicationFieldType.TEXT);
	map.put(NUMBER, ApplicationFieldType.NUMBER);
	map.put(MONEY, ApplicationFieldType.MONEY);
	map.put(APP, ApplicationFieldType.APP);
	map.put(DATE, ApplicationFieldType.DATE);
	map.put(DURATION, ApplicationFieldType.DURATION);
	map.put(PROGRESS, ApplicationFieldType.PROGRESS);
	map.put(CONTACT, ApplicationFieldType.CONTACT);
	map.put(EMBED, ApplicationFieldType.EMBED);

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
	if (ApplicationFieldType.CATEGORY.equals(f.getType())) {
	    if (Boolean.TRUE.equals(f.getConfiguration().getSettings().getMultiple())) {
		return CATEGORY_MULTI;
	    } else {
		return CATEGORY_SINGLE;
	    }
	}
	for (PodioType podioType : map.keySet()) {
	    if (map.get(podioType).equals(f.getType())) {
		return podioType;
	    }
	}
	LOGGER.warning("cannot determine type of field: " + f.getType() + "("
		+ f.getConfiguration().getLabel() + ", " + f.getStatus() + ")");
	return UNDEFINED;
    }
}
