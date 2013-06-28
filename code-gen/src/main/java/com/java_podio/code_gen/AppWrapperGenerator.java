package com.java_podio.code_gen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.podio.item.FieldValuesUpdate;
import com.podio.item.Item;
import com.podio.item.ItemCreate;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

/**
 * Generates a (single) AppWrapper abstract class - the super class of all app
 * wrapper classes.
 * 
 * @see AppWrapper
 */
public class AppWrapperGenerator {

	protected JCodeModel jc;

	protected JPackage jp;

	protected JMember originalItem;

	protected JMember podioId;

	protected JMember podioRevision;

	protected JMethod setValues;

	private JDefinedClass appWrapper;

	private JFieldVar podioDateTimeFormatter;

	private JMethod getAppExternalId;

	private JMethod getAppId;

	private JMethod getFieldValuesUpdateFromDate;

	private JFieldVar podioDateFormatter;

	private JMethod formatDate;

	public AppWrapperGenerator(JCodeModel jCodeModel, JPackage jp) {
		this.jc = jCodeModel;
		this.jp = jp;
	}

	/**
	 * Generates a/the AppWrapper class.<br>
	 * On subsequent calls on the same instance, the same object is returned!
	 * 
	 * @return
	 * @throws JClassAlreadyExistsException
	 * @see {@link AppWrapper}
	 */
	public JDefinedClass getAppWrapperClass() throws JClassAlreadyExistsException {
		if (appWrapper != null) {
			return appWrapper;
		}
		appWrapper = jp != null ? jp._class(JMod.ABSTRACT | JMod.PUBLIC, "AppWrapper") : jc._class("AppWrapper");

		podioDateTimeFormatter = appWrapper.field(JMod.PROTECTED | JMod.STATIC | JMod.FINAL, SimpleDateFormat.class,
				"PODIO_DATE_TIME_FORMATTER", JExpr.direct("new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\")"));
		podioDateFormatter = appWrapper.field(JMod.PROTECTED | JMod.STATIC | JMod.FINAL, SimpleDateFormat.class,
				"PODIO_DATE_FORMATTER", JExpr.direct("new SimpleDateFormat(\"yyyy-MM-dd\")"));

		formatDate = appWrapper.method(JMod.PUBLIC | JMod.STATIC, jc.ref(Date.class), "formatDate")._throws(
				jc.ref(ParseException.class));
		JVar formatDateParam = formatDate.param(jc.ref(String.class), "dateOrDateTime");
		JConditional cond = formatDate.body()._if(formatDateParam.invoke("length").lte(JExpr.lit(10)));
		cond._then()._return(podioDateFormatter.invoke("parse").arg(formatDateParam));
		cond._else()._return(podioDateTimeFormatter.invoke("parse").arg(formatDateParam));

		originalItem = CodeGenerator.addMember(appWrapper, "OriginalItem", jc.ref(Item.class),
				"Stores the original item, as retrieved by java-podio api.", jc);
		podioId = CodeGenerator.addMember(appWrapper, "PodioId", jc.ref(Integer.class),
				"This represents the internal Podio id of the item.", jc);
		podioRevision = CodeGenerator.addMember(appWrapper, "PodioRevision", jc.ref(Integer.class),
				"This represents the internal Podio revision of the item.", jc);

		getAppId = appWrapper.method(JMod.ABSTRACT | JMod.PUBLIC, Integer.class, "getAppId");
		getAppExternalId = appWrapper.method(JMod.ABSTRACT | JMod.PUBLIC, String.class, "getAppExternalId");
		appWrapper
				.method(JMod.ABSTRACT | JMod.PUBLIC, ItemCreate.class, "getItemCreate")
				.javadoc()
				.add("As {@link ItemCreate} inherits from {@link ItemUpdate} this method can be used to generate updates!");

		setValues = appWrapper.method(JMod.PUBLIC, jc.VOID, "setValue")._throws(ParseException.class);
		setValues.javadoc()
				.add("Fills this objects values from {@code item}.<br>Subclasses should extend this method!");
		JVar item = setValues.param(Item.class, "item");
		setValues.javadoc().addParam(item);
		setValues.javadoc().addThrows(ParseException.class);
		setValues.body().invoke(originalItem.getSetter()).arg(item);

		// public FieldValuesUpdate getFieldValuesUpdate(Date date) {
		// HashMap<String, String> dateHashMap = new HashMap<String, String>(2);
		// dateHashMap.put("start", podioDateFormat.format(date));
		// dateHashMap.put("end", podioDateFormat.format(date));
		// return new FieldValuesUpdate("datum", dateHashMap);
		// }

		getFieldValuesUpdateFromDate = appWrapper.method(JMod.PUBLIC | JMod.STATIC, FieldValuesUpdate.class,
				"getFieldValuesUpdate");
		JVar date = getFieldValuesUpdateFromDate.param(Date.class, "date");
		JVar dateHashMap = getFieldValuesUpdateFromDate.body().decl(
				jc.ref(HashMap.class).narrow(String.class, String.class), "dateHashMap",
				JExpr._new(jc.ref(HashMap.class).narrow(String.class, String.class)));
		getFieldValuesUpdateFromDate.body().add(
				dateHashMap.invoke("put").arg("start").arg(podioDateTimeFormatter.invoke("format").arg(date)));
		getFieldValuesUpdateFromDate.body().add(
				dateHashMap.invoke("put").arg("end").arg(podioDateTimeFormatter.invoke("format").arg(date)));
		getFieldValuesUpdateFromDate.body()._return(
				JExpr._new(jc.ref(FieldValuesUpdate.class)).arg("datum").arg(dateHashMap));

		CodeGenerator.addToString(appWrapper, jc);

		return appWrapper;
	}

	public JMember getOriginalItem() throws JClassAlreadyExistsException {
		if (originalItem == null) {
			getAppWrapperClass();
		}
		return originalItem;
	}

	public JMember getPodioId() throws JClassAlreadyExistsException {
		if (podioId == null) {
			getAppWrapperClass();
		}
		return podioId;
	}

	public JMember getPodioRevision() throws JClassAlreadyExistsException {
		if (podioRevision == null) {
			getAppWrapperClass();
		}
		return podioRevision;
	}

	public JMethod getSetValues() throws JClassAlreadyExistsException {
		if (setValues == null) {
			getAppWrapperClass();
		}
		return setValues;
	}

	public JFieldVar getPodioDateTimeFormatter() throws JClassAlreadyExistsException {
		if (podioDateTimeFormatter == null) {
			getAppWrapperClass();
		}
		return podioDateTimeFormatter;
	}

	public JFieldVar getPodioDateFormatter() throws JClassAlreadyExistsException {
		if (podioDateFormatter == null) {
			getAppWrapperClass();
		}
		return podioDateFormatter;
	}

	public JMethod getAppExternalId() throws JClassAlreadyExistsException {
		if (getAppExternalId == null) {
			getAppWrapperClass();
		}
		return getAppExternalId;
	}

	public JMethod getAppId() throws JClassAlreadyExistsException {
		if (getAppId == null) {
			getAppWrapperClass();
		}
		return getAppId;
	}

	public JMethod getFieldValuesUpdateFromDate() throws JClassAlreadyExistsException {
		if (getFieldValuesUpdateFromDate == null) {
			getAppWrapperClass();
		}
		return getFieldValuesUpdateFromDate;
	}

	public JMethod getFormatDate() throws JClassAlreadyExistsException {
		if (formatDate == null) {
			getAppWrapperClass();
		}
		return formatDate;
	}

}
