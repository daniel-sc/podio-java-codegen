package com.java_podio.code_gen;

import java.util.List;

import com.google.common.base.CaseFormat;
import com.podio.app.Application;
import com.podio.app.ApplicationField;
import com.podio.app.CategoryOption;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class CodeGenerator {

	final JCodeModel jCodeModel;

	JFieldVar podioDateFormatter;

	private JPackage jBasePackage;

	private AppGenerator appGenerator;

	public CodeGenerator(String basePackage) {
		jCodeModel = new JCodeModel();
		jBasePackage = jCodeModel._package(basePackage);
	}

	public JCodeModel generateCode(List<Application> appInfos) throws JClassAlreadyExistsException {
		for (Application application : appInfos) {
			printApp(application);
		}

		appGenerator = new AppGenerator(jCodeModel, jBasePackage);

		for (Application app : appInfos) {
			appGenerator.getAppClass(app);
		}

		return jCodeModel;

	}

	/**
	 * Adds field to class, including setter and getter.
	 * 
	 * @param jc
	 * @param name
	 * @param type
	 * @param javadoc
	 * @param jCodeModel
	 * @return
	 * @see #addMember(JDefinedClass, String, JType, String, JCodeModel,
	 *      boolean)
	 */
	public static JMember addMember(JDefinedClass jc, String name, JType type, String javadoc, JCodeModel jCodeModel) {
		return addMember(jc, name, type, javadoc, jCodeModel, false);
	}

	/**
	 * Adds field to class, including setter and getter.
	 * 
	 * @param jc
	 * @param name
	 *            of field in upper camel case
	 * @param type
	 * @param javadoc
	 *            is added to variable, setter and getter. Might be {@code null}
	 *            .
	 * @param jCodeModel
	 * @param isDeleted
	 *            if set to {@code true}, the elements are annotated with
	 *            {@link Deprecated} and a corresponding javadoc comment is
	 *            added.
	 * @return a reference to the field and its getter and setter
	 * @see CaseFormat#UPPER_CAMEL
	 */
	public static JMember addMember(JDefinedClass jc, String name, JType type, String javadoc, JCodeModel jCodeModel,
			boolean isDeleted) {
		String nameLowerCamelCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);

		// add member:
		JFieldVar var = jc.field(JMod.PROTECTED, type, nameLowerCamelCase);

		// add getter:
		JMethod getter = jc.method(JMod.PUBLIC, type, "get" + name);
		JBlock returnBlock = getter.body();
		returnBlock._return(var);

		// add setter:
		JMethod setter = jc.method(JMod.PUBLIC, jCodeModel.VOID, "set" + name);
		JVar param = setter.param(type, nameLowerCamelCase);
		JBlock setterBlock = setter.body();
		setterBlock.assign(JExpr._this().ref(var), param);

		if (javadoc != null) {
			var.javadoc().add(javadoc);
			getter.javadoc().add(javadoc);
			setter.javadoc().add(javadoc);
		}

		if (isDeleted) {
			var.annotate(Deprecated.class);
			var.javadoc().addDeprecated().add("This field is deleted in Podio!");
			getter.annotate(Deprecated.class);
			getter.javadoc().addDeprecated().add("This field is deleted in Podio!");
			setter.annotate(Deprecated.class);
			setter.javadoc().addDeprecated().add("This field is deleted in Podio!");
		}

		return new JMember(var, getter, setter);
	}

	public static void printApp(Application app) {
		System.out.println("AppId=" + app.getId());
		System.out.println("AppExternalId=" + app.getConfiguration().getExternalId());
		System.out.println("AppName=" + app.getConfiguration().getName());
		System.out.println("AppItemName=" + app.getConfiguration().getItemName());
		System.out.println("FIELDS:");
		for (ApplicationField appField : app.getFields()) {
			printAppField(appField);
			System.out.println();
		}
	}

	public static void printAppField(ApplicationField appField) {
		System.out.println("FieldId=" + appField.getId());
		System.out.println("FieldExternalId=" + appField.getExternalId());
		System.out.println("FieldDescription=" + appField.getConfiguration().getDescription());
		System.out.println("FieldLabel=" + appField.getConfiguration().getLabel());
		System.out.println("FieldIsRequired=" + appField.getConfiguration().isRequired());
		if (appField.getConfiguration().getSettings() != null) {
			System.out.println("FieldAllowedValues=" + appField.getConfiguration().getSettings().getAllowedValues());
			System.out.println("FieldAllowedCurrencies="
					+ appField.getConfiguration().getSettings().getAllowedCurrencies());
			System.out.println("FieldMultiple=" + appField.getConfiguration().getSettings().getMultiple());
			System.out.println("FieldReferenceableTypes="
					+ appField.getConfiguration().getSettings().getReferenceableTypes());
			if (appField.getConfiguration().getSettings().getOptions() != null) {
				for (CategoryOption option : appField.getConfiguration().getSettings().getOptions()) {
					System.out.println("FieldOption: " + option.getId() + ", " + option.getText() + ", "
							+ option.getStatus());
				}
			}
			System.out.println("FieldTextFieldSize=" + appField.getConfiguration().getSettings().getSize());
		}
		System.out.println("FieldType=" + appField.getType().toString());

	}

	/**
	 * Adds a toString method, printing out all non-static field variables.
	 * 
	 * @param jclass
	 * @param jCodeModel
	 * @param includeSuperToString
	 *            if {@code true} the output is preceeded by super.toString()
	 *            result.
	 */
	public static void addToString(JDefinedClass jclass, JCodeModel jCodeModel, boolean includeSuperToString) {
		boolean first = true;
		JMethod toString = jclass.method(JMod.PUBLIC, jCodeModel.ref(String.class), "toString");
		JVar result = toString.body().decl(jCodeModel.ref(String.class), "result", JExpr.lit(jclass.name() + " ["));
		if (includeSuperToString) {
			toString.body().assignPlus(result, JExpr._super().invoke("toString"));
			first = false;
		}
		for (JFieldVar jvar : jclass.fields().values()) {
			if ((jvar.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
				continue;
			}
			toString.body().assignPlus(result, JExpr.lit((first ? "" : ", ") + jvar.name() + "=").plus(jvar));
			first = false;
		}
		toString.body()._return(result.plus(JExpr.lit("]")));
	}

}
