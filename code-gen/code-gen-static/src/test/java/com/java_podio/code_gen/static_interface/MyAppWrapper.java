package com.java_podio.code_gen.static_interface;

import com.java_podio.code_gen.static_classes.AppWrapper;

/**
 * Note: (De-)Serialization fails if this is a nested class!
 */
public class MyAppWrapper extends AppWrapper {

    private static final long serialVersionUID = 1L;

    private String text;
    
    public MyAppWrapper() {
        super();
    }

    @Override
    public String getAppExternalId() {
        return null;
    }

    @Override
    public Integer getAppId() {
        return 100;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
    	return true;
        if (!super.equals(obj))
    	return false;
        if (getClass() != obj.getClass())
    	return false;
        MyAppWrapper other = (MyAppWrapper) obj;
        if (text == null) {
    	if (other.text != null)
    	    return false;
        } else if (!text.equals(other.text))
    	return false;
        return true;
    }

}