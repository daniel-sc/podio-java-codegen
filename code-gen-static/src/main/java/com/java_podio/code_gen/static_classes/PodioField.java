package com.java_podio.code_gen.static_classes;

import java.io.Serializable;

import com.podio.item.FieldValuesUpdate;

public interface PodioField extends Serializable {

	public abstract FieldValuesUpdate getFieldValuesUpdate(String externalId);

}