package com.java_podio.code_gen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

	protected JMember _originalItem;

	protected JMember _podioId;

	protected JMember _podioRevision;

	protected JMember _podioTitle;

	protected JMethod _setValue;

	private JDefinedClass appWrapper;

	private JFieldVar podioDateTimeFormatter;

	private JMethod getAppExternalId;

	private JMethod getAppId;

	private JMethod getFieldValuesUpdateFromDate;

	private JFieldVar podioDateFormatter;

	private JMethod formatDate;

	private JMethod _getItemCreate;

	private JMember _podioTags;

	public AppWrapperGenerator(JCodeModel jCodeModel, JPackage jp) throws JClassAlreadyExistsException {
		this.jc = jCodeModel;
		this.jp = jp;
		appWrapper = jp != null ? jp._class(JMod.ABSTRACT | JMod.PUBLIC, "AppWrapper") : jc._class("AppWrapper");
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

		getAppId = appWrapper.method(JMod.PUBLIC, Integer.class, "getAppId");
		getAppExternalId = appWrapper.method(JMod.ABSTRACT | JMod.PUBLIC, String.class, "getAppExternalId");

		// static getFieldValuesUpdate:
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

	public JMethod _setValue() {
		if (_setValue == null) {
			_setValue = appWrapper.method(JMod.PUBLIC, jc.VOID, "setValue")._throws(ParseException.class);
			_setValue.javadoc().add(
					"Fills this objects values from {@code item}.<br>Subclasses should extend this method!");
			JVar item = _setValue.param(Item.class, "item");
			_setValue.javadoc().addParam(item);
			_setValue.javadoc().addThrows(ParseException.class);
			_setValue.body().invoke(_originalItem().getSetter()).arg(item);
			_setValue.body().assign(_podioId().getField(), item.invoke("getId"));
			_setValue.body().assign(_podioRevision().getField(),
					item.invoke("getCurrentRevision").invoke("getRevision"));
			_setValue.body().assign(_podioTitle().getField(), item.invoke("getTitle"));
			_setValue.body().assign(_podioTags().getField(), item.invoke("getTags"));
		}
		return _setValue;
	}

	public JMember _originalItem() {
		if (_originalItem == null) {
			_originalItem = CodeGenerator.addMember(appWrapper, "OriginalItem", jc.ref(Item.class),
					"Stores the original item, as retrieved by java-podio api.", jc);
		}
		return _originalItem;
	}

	public JMember _podioId() {
		if (_podioId == null) {
			_podioId = CodeGenerator.addMember(appWrapper, "PodioId", jc.ref(Integer.class),
					"This represents the internal Podio id of the item.", jc);
		}
		return _podioId;
	}

	public JMember _podioRevision() {
		if (_podioRevision == null) {
			_podioRevision = CodeGenerator.addMember(appWrapper, "PodioRevision", jc.ref(Integer.class),
					"This represents the internal Podio revision of the item.", jc);
		}
		return _podioRevision;
	}

	public JMember _podioTitle() {
		if (_podioTitle == null) {
			_podioTitle = CodeGenerator.addMember(appWrapper, "PodioTitle", jc.ref(String.class),
					"This represents the Podio title of the item.", jc);
		}
		return _podioTitle;
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

	public JMember _podioTags() {
		if (_podioTags == null) {
			_podioTags = CodeGenerator.addMember(appWrapper, "PodioTags", jc.ref(List.class).narrow(String.class),
					"This represents the Podio tags of the item.", jc);
		}
		return _podioTags;
	}

	public JMethod _getItemCreate() {
		if (_getItemCreate == null) {
			_getItemCreate = appWrapper.method(JMod.PUBLIC, ItemCreate.class, "getItemCreate");
			_getItemCreate
					.javadoc()
					.add("As {@link ItemCreate} inherits from {@link ItemUpdate} this method can be used to generate updates!");
			JVar _itemCreateResult = _getItemCreate.body().decl(jc.ref(ItemCreate.class), "result",
					JExpr._new(jc.ref(ItemCreate.class)));
			_getItemCreate.body().add(_itemCreateResult.invoke("setExternalId").arg(JExpr.invoke(getAppExternalId)));
			_getItemCreate.body().add(
					_itemCreateResult.invoke("setRevision").arg(JExpr.invoke(_podioRevision().getGetter())));
			_getItemCreate.body()
					.add(_itemCreateResult.invoke("setTitle").arg(JExpr.invoke(_podioTitle().getGetter())));
			_getItemCreate.body().add(_itemCreateResult.invoke("setTags").arg(JExpr.invoke(_podioTags().getGetter())));
			JVar fieldValuesList = _getItemCreate.body().decl(jc.ref(List.class).narrow(FieldValuesUpdate.class),
					"fieldValuesList", JExpr._new(jc.ref(ArrayList.class).narrow(FieldValuesUpdate.class)));
			_getItemCreate.body().add(_itemCreateResult.invoke("setFields").arg(fieldValuesList));
			_getItemCreate.body()._return(_itemCreateResult);
		}
		return _getItemCreate;
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
