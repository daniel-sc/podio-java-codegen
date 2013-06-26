package com.java_podio.code_gen;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

/**
 * Generates a (single) Currency class for handling currencies.
 */
public class CurrencyGenerator {

	private JCodeModel jCodeModel;

	public CurrencyGenerator(JCodeModel jCodeModel) {
		this.jCodeModel = jCodeModel;
	}
	
	/**
	 * Generates a/the currency class and writes it to file system, if {@code folder!=null}.
	 * @param folder
	 * @return
	 */
	public JDefinedClass generateCurrencyClass(String folder) {
		//TODO implement
		return null;
	}

}
