package com.java_podio.code_gen;

import com.podio.item.FieldValuesUpdate;
import com.podio.item.FieldValuesView;

/**
 * DRAFT: this might become helpful, when there'll be more PodioTypes, such as PodioCurrency..
 */
public interface PodioTypes {

	public FieldValuesUpdate getFieldValuesUpdate(String externalId);

	public void setValues(FieldValuesView field);
	

}