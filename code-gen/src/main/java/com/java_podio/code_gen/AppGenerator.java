package com.java_podio.code_gen;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.CaseFormat;
import com.podio.app.Application;
import com.podio.app.ApplicationField;
import com.podio.item.FieldValuesUpdate;
import com.podio.item.FieldValuesView;
import com.podio.item.Item;
import com.podio.item.ItemCreate;
import com.podio.item.ItemUpdate;
import com.sun.codemodel.JCase;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JVar;

/**
 * Generates a java wrapper for a given podio app.
 */
public class AppGenerator {

	private static final String FIELD_IS_OF_UNSUPPORTET_TYPE_JAVADOC = "Field is of unsupportet type and is not parsed, hence always {@code null}!";

	protected JCodeModel jCodeModel;

	/**
	 * Constructor - constructing an object from an {@link Item}.
	 */
	JMethod constructorFromItem;

	/**
	 * Sets values from a given {@link Item}.
	 */
	JMethod setValuesFromItem;

	/**
	 * Constructs a {@link ItemCreate} from current instance. As {@link ItemCreate} inherits from {@link ItemUpdate}, the result can be used for updates as well.
	 */
	JMethod getItemCreate;

	private JVar itemCreateResult;

	private JVar itemCreateFieldValues;

	protected JDefinedClass currencyClass;

	private EnumGenerator enumGenerator;

	protected JPackage jp;

	protected AppWrapperGenerator appWrapperGenerator;

	protected CurrencyGenerator currencyGenerator;

	/**
	 * The generated app class.
	 */
	private JDefinedClass jc = null;

	public AppGenerator(JCodeModel jCodeModel, JPackage jPackage, AppWrapperGenerator appWrapperGenerator, CurrencyGenerator currencyGenerator) throws JClassAlreadyExistsException {
		this.jCodeModel = jCodeModel;
		this.jp = jPackage;
		this.appWrapperGenerator = appWrapperGenerator;
		this.currencyGenerator = currencyGenerator;
		this.currencyClass = currencyGenerator.getCurrencyClass();
	}

	/**
	 * Generates a app wrapper class.
	 * 
	 * @return
	 * @throws JClassAlreadyExistsException
	 * @param app
	 * @return
	 */
	public JDefinedClass getAppClass(Application app) throws JClassAlreadyExistsException {
		// Debug:
		CodeGenerator.printApp(app);

		String className = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, app.getConfiguration().getName().toLowerCase());
		jc = jp._class(className)._extends(appWrapperGenerator.getAppWrapperClass());

		enumGenerator = new EnumGenerator(jCodeModel, jp.subPackage(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className)));

		JDocComment jDocComment = jc.javadoc();
		jDocComment.add("Wrapper for podio app '" + app.getConfiguration().getName() + "' (id=" + app.getId() + ").\nGenerated by java-podio-code-gen.");

		jc.method(JMod.PUBLIC, Integer.class, "getAppId").body()._return(JExpr.lit(app.getId()));
		jc.method(JMod.PUBLIC, String.class, "getAppExternalId").body()._return(JExpr.lit(app.getConfiguration().getExternalId() == null ? "" : app.getConfiguration().getExternalId()));

		// TODO mark deleted elements as deprecated?!

		// TODO add podio item title?!

		// TODO add field ids (id/externalId)?

		// setValuesFromItem method (needs to be defined before
		// itemConstructor?!):
		setValuesFromItem = jc.method(JMod.PUBLIC, jCodeModel.VOID, "setValues");
		JVar setValuesFromItemParam = setValuesFromItem.param(Item.class, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, className) + "Item");
		setValuesFromItem.body().add((JExpr._super().invoke(appWrapperGenerator.getSetValues()).arg(setValuesFromItemParam)));
		JForEach setValuesFromItemForEachField = setValuesFromItem.body().forEach(jCodeModel.ref(FieldValuesView.class), "field", setValuesFromItemParam.invoke("getFields"));
		JSwitch setValuesFromItemSwitch = setValuesFromItemForEachField.body()._switch(setValuesFromItemForEachField.var().invoke("getId"));
		setValuesFromItemSwitch._default().body().directStatement("System.out.println(\"ERROR: unexpected field id=\"+field.getId() (App: \"+this.getClass().getName()+\"");
		setValuesFromItemSwitch._default().body()._break();

		// getItemCreate method:
		getItemCreate = jc.method(JMod.PUBLIC, jCodeModel._ref(ItemCreate.class), "getItemCreate");
		itemCreateResult = getItemCreate.body().decl(jCodeModel.ref(ItemCreate.class), "result", JExpr._new(jCodeModel.ref(ItemCreate.class)));
		getItemCreate.body().add(itemCreateResult.invoke("setExternalId").arg(JExpr.invoke(appWrapperGenerator.getAppExternalId())));
		itemCreateFieldValues = getItemCreate.body().decl(jCodeModel.ref(List.class).narrow(FieldValuesUpdate.class), "fieldValuesList", JExpr._new(jCodeModel.ref(ArrayList.class).narrow(FieldValuesUpdate.class)));

		// Default constructor:
		jc.constructor(JMod.PUBLIC);

		// itemConstructor:
		constructorFromItem = jc.constructor(JMod.PUBLIC);
		JVar constructorFromItemParam = constructorFromItem.param(Item.class, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, className) + "Item");
		constructorFromItem.body().invoke(setValuesFromItem).arg(constructorFromItemParam);

		// add internal podio id and revision:
		setValuesFromItem.body().assign(appWrapperGenerator.getPodioId().getField(), setValuesFromItemParam.invoke("getId"));
		setValuesFromItem.body().assign(appWrapperGenerator.getPodioRevision().getField(), setValuesFromItemParam.invoke("getCurrentRevision").invoke("getRevision"));

		for (ApplicationField f : app.getFields()) {
			String name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, f.getExternalId().toLowerCase());
			PodioType type = PodioType.forApplicationField(f);

			String javadoc = f.getConfiguration().getDescription();
			if (type.equals(PodioType.UNDEFINED)) {
				javadoc = javadoc == null ? FIELD_IS_OF_UNSUPPORTET_TYPE_JAVADOC : javadoc + "\n" + FIELD_IS_OF_UNSUPPORTET_TYPE_JAVADOC;
			}

			JClass javaType = getType(type, f);
			JMember field = CodeGenerator.addMember(jc, name, javaType, javadoc, jCodeModel, "deleted".equalsIgnoreCase(f.getStatus()));

			// add setValuesFromItem part:
			JCase jcase = setValuesFromItemSwitch._case(JExpr.lit(f.getId()));
			jcase.body().invoke("set" + name).arg(createGetFieldValue(type, setValuesFromItemForEachField.var(), javaType));
			jcase.body()._break();

			// add getItemCreate part:
			JExpression fieldValueUpdate = createFieldValuesUpdate(field.getGetter(), type, f);
			if (fieldValueUpdate != null) {
				getItemCreate.body().add(itemCreateFieldValues.invoke("add").arg(fieldValueUpdate));
			}
		}

		getItemCreate.body().add(itemCreateResult.invoke("setFields").arg(itemCreateFieldValues));
		getItemCreate.body()._return(itemCreateResult);

		return jc;
	}

	// TODO add tag handling

	// TODO Add link to element?

	// TODO add revision, to getItemCreate (needs update of podio java api!)

	private JClass getType(PodioType type, ApplicationField f) {
		JClass result;
		switch (type) {
			case MONEY:
				result = currencyClass;
				break;
			case CATEGORY_SINGLE:
				String name = JavaNames.createValidJavaTypeName(f.getConfiguration().getLabel());
				if (f.getConfiguration().getSettings().getMultiple().equals(Boolean.FALSE)) {

					try {
						result = enumGenerator.generateEnum(f, name);
					} catch (JClassAlreadyExistsException e) {
						System.out.println("ERROR: could not generate enum with name: " + name + "(might exist twice?!)");
						e.printStackTrace();
						result = jCodeModel.ref(Integer.class);
					}
				} else {
					System.out.println("ERROR: Categories with multiple values not supportet yet! (Category: " + f.getConfiguration().getLabel() + ")");
					result = jCodeModel.ref(Void.class);
				}
				break;

			default:
				result = jCodeModel.ref(type.getJavaType());
				break;
		}
		return result;
	}

	/**
	 * JavaType -> Item
	 * 
	 * @param getter
	 * @param type
	 *            (return-)type of {@code getter}
	 * @param f
	 *            corresponds to (field of) {@code getter}
	 * @return an {@link JExpression} that evaluates to a {@link FieldValuesUpdate} containing the return value of {@code getter}. If the field type is not supported, returns {@code null}.
	 */
	private JExpression createFieldValuesUpdate(JMethod getter, PodioType type, ApplicationField f) {
		switch (type) {
			case TEXT:
			case NUMBER:
				return JExpr._new(jCodeModel.ref(FieldValuesUpdate.class)).arg(f.getExternalId()).arg("value").arg(JExpr.invoke(getter));
			case MONEY:
				return JExpr.invoke(getter).invoke("getFieldValuesUpdate").arg(JExpr.lit(f.getExternalId()));
			case CATEGORY_SINGLE:
				// new FieldValuesUpdate("status", "value", customer.getPowerStatus().getId())
				return JExpr._new(jCodeModel.ref(FieldValuesUpdate.class)).arg(f.getExternalId()).arg("value").arg(JExpr.invoke(getter).invoke("getPodioId"));
			case APP:
				// fieldValuesList.add(new FieldValuesUpdate("extid", "value", Collections.singletonMap("item_id", getKunde())));
				return JExpr._new(jCodeModel.ref(FieldValuesUpdate.class)).arg(f.getExternalId()).arg("value").arg(jCodeModel.ref(Collections.class).staticInvoke("singletonMap").arg("item_id").arg(JExpr.invoke(getter)));
			default:
				return null;
		}
	}

	/**
	 * Item -> JavaType
	 * 
	 * @param type
	 * @param jVar
	 *            is expected to be of type {@link FieldValuesView}
	 * @param javaType
	 * @return an {@link JExpression} that evaluates to the value represented in {@code jVar} and is of type {@code javaType}.
	 * @throws JClassAlreadyExistsException
	 */
	private JExpression createGetFieldValue(PodioType type, JVar jVar, JClass javaType) throws JClassAlreadyExistsException {
		switch (type) {
			case TEXT:
				return createGetStringFieldValue(jVar, "value", jCodeModel);
			case NUMBER:
				return createGetDoubleFieldValue(jVar);
			case MONEY:
				return createGetCurrencyFieldValue(jVar);
			case DATE:
				return createGetDateFieldValue(jVar);
			case CATEGORY_SINGLE:
				JExpression podioId = JExpr.direct("(Integer) ((java.util.Map<String, ?>) " + jVar.name() + ".getValues().get(0).get(\"value\")).get(\"id\")");
				return javaType.staticInvoke("byId").arg(podioId);
				// return JExpr.cast(
				// Integer.class,
				// JExpr.cast(jCodeModel.ref(Map.class).narrow(String.class, Object.class),
				// jVar.invoke("getValues").invoke("get").arg(JExpr.lit(0)).invoke("get").arg("value"))
				// .invoke("id"));
			case APP:
				// ((Map<String, Map<String, Integer>>) field.getValues().get(0)).get("value").get("item_id")
				return JExpr.direct("((java.util.Map<String, java.util.Map<String, Integer>>) " + jVar.name() + ".getValues().get(0)).get(\"value\").get(\"item_id\")");
			default:
				System.out.println("WARNING: could not create getFieldValueExpression for type: " + type);
				return JExpr._null();
		}
	}

	/**
	 * @param jVar
	 *            needs to be of type {@link FieldValuesView}.
	 * @return
	 */
	private JExpression createGetCurrencyFieldValue(JVar jVar) {
		return JExpr._new(currencyClass).arg(createGetDoubleFieldValue(jVar)).arg(createGetStringFieldValue(jVar, "currency", jCodeModel));
	}

	private JExpression createGetDateFieldValue(JVar jVar) throws JClassAlreadyExistsException {
		setValuesFromItem._throws(ParseException.class);
		constructorFromItem._throws(ParseException.class);
		JExpression exp = createGetStringFieldValue(jVar, "start_date", jCodeModel);
		// 2011-12-31 11:27:10
		return appWrapperGenerator.getPodioDateFormatter().invoke("parse").arg(exp);
	}

	/**
	 * @param jVar
	 *            needs to be of type {@link FieldValuesView}.
	 * @return String cast of field with key {@code field}
	 */
	public static JExpression createGetStringFieldValue(JVar jVar, String field, JCodeModel jCodeModel) {
		return JExpr.cast(jCodeModel._ref(String.class), jVar.invoke("getValues").invoke("get").arg(JExpr.lit(0)).invoke("get").arg(field));
	}

	/**
	 * @param jVar
	 *            needs to be of type {@link FieldValuesView}.
	 * @return
	 */
	private JExpression createGetDoubleFieldValue(JVar jVar) {
		// Double.parseDouble((String) field.getValues().get(0).get("value")
		return jCodeModel.ref(Double.class).staticInvoke("parseDouble").arg(createGetStringFieldValue(jVar, "value", jCodeModel));
	}

}
