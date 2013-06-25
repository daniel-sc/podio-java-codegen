package com.java_podio.code_gen;

import com.podio.app.Application;
import com.podio.app.ApplicationField;

public class CodeGenerator {

	public CodeGenerator() {
	}

	public void generateCode(Application app) {
		printApp(app);
	}

	public static void printApp(Application app) {
		System.out.println("AppId=" + app.getId());
		System.out.println("AppExternalId=" + app.getConfiguration().getExternalId());
		System.out.println("AppName=" + app.getConfiguration().getName());
		System.out.println("AppItemName=" + app.getConfiguration().getItemName());
		System.out.println("FIELDS:");
		for (ApplicationField appField : app.getFields()) {
			System.out.println("FieldId=" + appField.getId());
			System.out.println("FieldExternalId=" + appField.getExternalId());
			System.out.println("FieldDescription=" + appField.getConfiguration().getDescription());
			System.out.println("FieldLabel=" + appField.getConfiguration().getLabel());
			if (appField.getConfiguration().getSettings() != null) {
				System.out.println("FieldAllowedValues=" + appField.getConfiguration().getSettings().getAllowedValues());
				System.out.println("FieldAllowedCurrencies=" + appField.getConfiguration().getSettings().getAllowedCurrencies());
				System.out.println("FieldReferenceableTypes=" + appField.getConfiguration().getSettings().getReferenceableTypes());
				System.out.println("FieldTextFieldSize=" + appField.getConfiguration().getSettings().getSize());
			}
			System.out.println("FieldType=" + appField.getType().toString());
			System.out.println();
		}
	}

}
