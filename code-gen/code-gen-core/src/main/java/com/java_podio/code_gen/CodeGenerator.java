package com.java_podio.code_gen;

import com.google.common.base.CaseFormat;
import com.podio.app.Application;
import com.podio.app.ApplicationField;
import com.podio.app.CategoryOption;
import com.sun.codemodel.*;

import java.util.List;
import java.util.logging.Logger;

public class CodeGenerator {

        private static final Logger LOGGER = Logger.getLogger(CodeGenerator.class.getName());

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
		LOGGER.info("AppId=" + app.getId());
		LOGGER.info("AppExternalId=" + app.getConfiguration().getExternalId());
		LOGGER.info("AppName=" + app.getConfiguration().getName());
		LOGGER.info("AppItemName=" + app.getConfiguration().getItemName());
		LOGGER.info("FIELDS:");
		for (ApplicationField appField : app.getFields()) {
			printAppField(appField);
			LOGGER.info("--");
		}
	}

	public static void printAppField(ApplicationField appField) {
		LOGGER.info("FieldId=" + appField.getId());
		LOGGER.info("FieldExternalId=" + appField.getExternalId());
		LOGGER.info("FieldDescription=" + appField.getConfiguration().getDescription());
		LOGGER.info("FieldLabel=" + appField.getConfiguration().getLabel());
		LOGGER.info("FieldIsRequired=" + appField.getConfiguration().isRequired());
		if (appField.getConfiguration().getSettings() != null) {
			LOGGER.info("FieldAllowedValues=" + appField.getConfiguration().getSettings().getAllowedValues());
			LOGGER.info("FieldAllowedCurrencies="
					+ appField.getConfiguration().getSettings().getAllowedCurrencies());
			LOGGER.info("FieldMultiple=" + appField.getConfiguration().getSettings().getMultiple());
			LOGGER.info("FieldReferenceableTypes="
					+ appField.getConfiguration().getSettings().getReferenceableTypes());
			if (appField.getConfiguration().getSettings().getOptions() != null) {
				for (CategoryOption option : appField.getConfiguration().getSettings().getOptions()) {
					LOGGER.info("FieldOption: " + option.getId() + ", " + option.getText() + ", "
							+ option.getStatus());
				}
			}
			LOGGER.info("FieldTextFieldSize=" + appField.getConfiguration().getSettings().getSize());
		}
		LOGGER.info("FieldType=" + appField.getType().toString());

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
	
	/**
	 * Adds a equals method, containing all non-static field variables.
	 * 
	 * @param jclass
	 * @param jCodeModel
	 * @param includeSuperEquals
	 *            if {@code true} super.equals() is incorporated.
	 */
	public static void addEquals(JDefinedClass jclass, JCodeModel jCodeModel, boolean includeSuperEquals) {
		JMethod equals = jclass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "equals");
		JVar _obj = equals.param(jCodeModel.ref(Object.class), "obj");
		if (includeSuperEquals) {
			equals.body()._if(JExpr.FALSE.eq(JExpr._super().invoke("equals").arg(_obj)))._then()._return(JExpr.FALSE);
		}
		equals.body().directStatement("\tif (this == obj)\n\t\treturn true;\n\tif (obj == null)\n\t\treturn false;\n\tif (getClass() != obj.getClass())\treturn false;");
		JVar _other = equals.body().decl(jclass, "other", JExpr.cast(jclass, _obj));
		for (JFieldVar jvar : jclass.fields().values()) {
			if ((jvar.mods().getValue() & JMod.STATIC) == JMod.STATIC) {
				continue;
			}
			
			JConditional _outerIf = equals.body()._if(jvar.eq(JExpr._null()));
			_outerIf._then()._if(_other.ref(jvar.name()).ne(JExpr._null()))._then()._return(JExpr.FALSE);
			_outerIf._else()._if((jvar.invoke("equals").arg(_other.ref(jvar.name())).not()))._then()._return(JExpr.FALSE);
		}
		equals.body()._return(JExpr.TRUE);
	}

}
