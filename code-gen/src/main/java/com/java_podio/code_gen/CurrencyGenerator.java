package com.java_podio.code_gen;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import com.podio.item.FieldValuesUpdate;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

/**
 * Generates a (single) Currency class for handling currencies.
 */
public class CurrencyGenerator {

	private JCodeModel jc;
	private JPackage jp;

	public CurrencyGenerator(JCodeModel jCodeModel, JPackage jp) {
		this.jc = jCodeModel;
		this.jp = jp;
	}

	/**
	 * Generates a/the currency class.
	 * 
	 * @return
	 * @throws JClassAlreadyExistsException
	 */
	public JDefinedClass generateCurrencyClass() throws JClassAlreadyExistsException {
		JDefinedClass result = jp != null ? jp._class("PodioCurrency") : jc._class("PodioCurrency");

		JMember currency = CodeGenerator.addMember(result, "currency", jc.ref(Currency.class), null, jc);

		JMember value = CodeGenerator.addMember(result, "value", jc.ref(Double.class), null, jc);

		JFieldVar currencyValueFormatter = result.field(JMod.PUBLIC, NumberFormat.class, "currencyValueFormatter", jc
				.ref(DecimalFormat.class).staticInvoke("getCurrencyInstance"));

		// Standard constructor:
		JMethod defaultConstructor = result.constructor(JMod.PUBLIC);
		defaultConstructor.body()
				.assign(currency.getField(),
						jc.ref(Currency.class).staticInvoke("getInstance")
								.arg(jc.ref(Locale.class).staticInvoke("getDefault")));
		defaultConstructor.javadoc().add("Creates Currency using {@link java.util.Locale.getDefault()}.");

		// value constructor:
		JMethod valueConstructor = result.constructor(JMod.PUBLIC);
		JVar valueParam1 = valueConstructor.param(Double.class, "value");
		valueConstructor.body().assign(JExpr._this().ref(value.getField()), valueParam1);
		valueConstructor.body()
				.assign(currency.getField(),
						jc.ref(Currency.class).staticInvoke("getInstance")
								.arg(jc.ref(Locale.class).staticInvoke("getDefault")));
		valueConstructor.javadoc().add("Creates Currency using {@link java.util.Locale.getDefault()}.");

		// value+currency Constructor:
		JMethod valueAndCurrencyConstructor = result.constructor(JMod.PUBLIC);
		JVar valueParam = valueAndCurrencyConstructor.param(Double.class, "value");
		JVar currencyParam = valueAndCurrencyConstructor.param(String.class, "currency");
		valueAndCurrencyConstructor
				.javadoc()
				.addParam("currency")
				.add("Currency code as defined by ISO 4217 (e.g. \"EUR\" or \"USD\") - see {@link Currency#getInstance(String)}");
		valueAndCurrencyConstructor.body().assign(JExpr._this().ref(value.getField()), valueParam);
		valueAndCurrencyConstructor.body().assign(JExpr._this().ref(currency.getField()),
				jc.ref(Currency.class).staticInvoke("getInstance").arg(currencyParam));

		// getFieldValuesUpdate:
		JMethod getFieldValuesUpdate = result.method(JMod.PUBLIC, FieldValuesUpdate.class, "getFieldValuesUpdate");
		JVar externalId = getFieldValuesUpdate.param(String.class, "externalId");
		JVar valueMap = getFieldValuesUpdate.body().decl(jc.ref(HashMap.class).narrow(String.class, String.class),
				"valueMap", JExpr._new(jc.ref(HashMap.class).narrow(String.class, String.class)));
		getFieldValuesUpdate.body().add(
				valueMap.invoke("put").arg("currency")
						.arg(JExpr.invoke(currency.getGetter()).invoke("getCurrencyCode")));
		getFieldValuesUpdate.body().add(
				valueMap.invoke("put").arg("value")
						.arg(currencyValueFormatter.invoke("format").arg(JExpr.invoke(value.getGetter()))));
		JVar fieldValuesUpdate = getFieldValuesUpdate.body().decl(jc.ref(FieldValuesUpdate.class), "result",
				JExpr._new(jc.ref(FieldValuesUpdate.class)).arg(externalId).arg("value").arg(valueMap));
		getFieldValuesUpdate.body()._return(fieldValuesUpdate);

		return result;
	}
}
