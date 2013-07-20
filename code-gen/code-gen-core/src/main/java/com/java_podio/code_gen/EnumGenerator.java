package com.java_podio.code_gen;

import com.google.common.base.CaseFormat;
import com.java_podio.code_gen.static_classes.PodioCategory;
import com.podio.app.ApplicationField;
import com.podio.app.CategoryOption;
import com.podio.app.CategoryOptionStatus;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

/**
 * Generates a enums for handling categories.
 */
public class EnumGenerator {

	private JCodeModel jc;
	private JPackage jp;

	public EnumGenerator(JCodeModel jCodeModel, JPackage jp) {
		this.jc = jCodeModel;
		this.jp = jp;
	}

	/**
	 * Generates an enum for a given field
	 * 
	 * @param f
	 * @param name
	 *            of enum in UpperCamelCase
	 * @return
	 * @throws JClassAlreadyExistsException
	 */
	public JDefinedClass generateEnum(ApplicationField f, String name) throws JClassAlreadyExistsException {
		JDefinedClass result = jp != null ? jp._enum(name) : jc._package("")._enum(name);
		result._implements(PodioCategory.class);

		// fields:
		JFieldVar podioId = result.field(JMod.PRIVATE, jc.INT, "podioId");
		JFieldVar value = result.field(JMod.PRIVATE, jc.ref(String.class), "value");

		// constructor:
		JMethod constructor = result.constructor(JMod.PRIVATE);
		JVar constructorPodioIdParam = constructor.param(jc.INT, "podioId");
		JVar constructorToStringParam = constructor.param(jc.ref(String.class), "value");
		constructor.body().assign(JExpr._this().ref(podioId), constructorPodioIdParam);
		constructor.body().assign(JExpr._this().ref(value), constructorToStringParam);

		// toString:
		result.method(JMod.PUBLIC, String.class, "toString").body()._return(value);

		// getId:
		result.method(JMod.PUBLIC, jc.INT, "getPodioId").body()._return(podioId);

		// static byId:
		JMethod byId = result.method(JMod.PUBLIC | JMod.STATIC, result, "byId");
		byId.javadoc().addReturn().add("{@code null}, if no element with given {@code id} exists.");
		JVar byIdParam = byId.param(jc.INT, "podioId");
		JForEach forEach = byId.body().forEach(result, "e", result.staticInvoke("values"));
		forEach.body()._if(byIdParam.eq(forEach.var().invoke("getPodioId")))._then()._return(forEach.var());
		byId.body()._return(JExpr._null());

		// literals:
		result.enumConstant("NONE").arg(JExpr.lit(0)).arg(JExpr.lit("--"));
		for (CategoryOption option : f.getConfiguration().getSettings().getOptions()) {
			JEnumConstant constant = result.enumConstant(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE,
					JavaNames.createValidJavaTypeName(option.getText(), name)));
			constant.arg(JExpr.lit(option.getId())).arg(JExpr.lit(option.getText()));
			if (option.getStatus().equals(CategoryOptionStatus.DELETED)) {
				constant.annotate(Deprecated.class);
			}
		}

		return result;
	}
}
