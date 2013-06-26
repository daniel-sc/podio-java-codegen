package com.java_podio.code_gen;

import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * Represents a field variable including its getter and setter method.
 */
public class JMember {

	private JFieldVar field;

	private JMethod getter;

	private JMethod setter;

	public JMember(JFieldVar field, JMethod getter, JMethod setter) {
		super();
		this.field = field;
		this.getter = getter;
		this.setter = setter;
	}

	public JFieldVar getField() {
		return field;
	}

	public JMethod getGetter() {
		return getter;
	}

	public JMethod getSetter() {
		return setter;
	}

	public void setField(JFieldVar field) {
		this.field = field;
	}

	public void setGetter(JMethod getter) {
		this.getter = getter;
	}

	public void setSetter(JMethod setter) {
		this.setter = setter;
	}

}