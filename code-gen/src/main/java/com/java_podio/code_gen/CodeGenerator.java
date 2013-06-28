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

	CurrencyGenerator currencyGenerator;

	private JPackage jBasePackage;

	private AppWrapperGenerator appWrapperGenerator;

	private AppGenerator appGenerator;

	public CodeGenerator(String basePackage) {
		jCodeModel = new JCodeModel();
		jBasePackage = jCodeModel._package(basePackage);
	}

	public JCodeModel generateCode(List<Application> appInfos) throws JClassAlreadyExistsException {
		for (Application application : appInfos) {
			printApp(application);
		}

		currencyGenerator = new CurrencyGenerator(jCodeModel, jBasePackage);

		appWrapperGenerator = new AppWrapperGenerator(jCodeModel, jBasePackage);

		appGenerator = new AppGenerator(jCodeModel, jBasePackage, appWrapperGenerator, currencyGenerator);

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
	 * @see #addMember(JDefinedClass, String, JType, String, JCodeModel, boolean)
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
	 *            is added to variable, setter and getter. Might be {@code null} .
	 * @param jCodeModel
	 * @param isDeprecated
	 *            if set to {@code true}, the elements are annotated with {@link Deprecated}.
	 * @return a reference to the field and its getter and setter
	 * @see CaseFormat#UPPER_CAMEL
	 */
	public static JMember addMember(JDefinedClass jc, String name, JType type, String javadoc, JCodeModel jCodeModel, boolean isDeprecated) {
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

		if (isDeprecated) {
			var.annotate(Deprecated.class);
			getter.annotate(Deprecated.class);
			setter.annotate(Deprecated.class);
		}

		return new JMember(var, getter, setter);
	}

	/**
	 * @param name
	 *            might contain spaces and special characters.
	 * @return valid upper camel case java type name
	 */
	public static String createValidJavaTypeName(String name) {
		name = name.toLowerCase().replace(' ', '-').replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
		name = name.replaceAll("[^-a-zA-Z]", "");
		name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name);
		return name;
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
			System.out.println("FieldAllowedCurrencies=" + appField.getConfiguration().getSettings().getAllowedCurrencies());
			System.out.println("FieldMultiple=" + appField.getConfiguration().getSettings().getMultiple());
			System.out.println("FieldReferenceableTypes=" + appField.getConfiguration().getSettings().getReferenceableTypes());
			if (appField.getConfiguration().getSettings().getOptions() != null) {
				for (CategoryOption option : appField.getConfiguration().getSettings().getOptions()) {
					System.out.println("FieldOption: " + option.getId() + ", " + option.getText() + ", " + option.getStatus());
				}
			}
			System.out.println("FieldTextFieldSize=" + appField.getConfiguration().getSettings().getSize());
		}
		System.out.println("FieldType=" + appField.getType().toString());

	}

}
