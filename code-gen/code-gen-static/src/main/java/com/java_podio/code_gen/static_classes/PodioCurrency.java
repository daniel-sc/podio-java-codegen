package com.java_podio.code_gen.static_classes;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import com.podio.item.FieldValuesUpdate;

public class PodioCurrency implements PodioField {

    	private static final long serialVersionUID = 1L;

    	public static NumberFormat currencyValueFormatter = DecimalFormat.getInstance(Locale.US);
	
    	private Currency currency;
	private Double value;

	static {
		currencyValueFormatter.setGroupingUsed(false);
	}

	/**
	 * Creates Currency using {@link java.util.Locale.getDefault()}.
	 * 
	 */
	public PodioCurrency() {
		currency = Currency.getInstance(Locale.getDefault());
	}

	/**
	 * Creates Currency using {@link java.util.Locale.getDefault()}.
	 * 
	 */
	public PodioCurrency(Double value) {
		this.value = value;
		currency = Currency.getInstance(Locale.getDefault());
	}

	/**
	 * 
	 * @param currency
	 *            Currency code as defined by ISO 4217 (e.g. "EUR" or "USD") -
	 *            see {@link Currency#getInstance(String)}
	 */
	public PodioCurrency(Double value, String currency) {
		this.value = value;
		this.currency = Currency.getInstance(currency);
	}

	public Currency getcurrency() {
		return currency;
	}

	public void setcurrency(Currency currency) {
		this.currency = currency;
	}

	public Double getvalue() {
		return value;
	}

	public void setvalue(Double value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.java_podio.code_gen.static_classes.PodioField#getFieldValuesUpdate(java.lang.String)
	 */
	public FieldValuesUpdate getFieldValuesUpdate(String externalId) {
		HashMap<String, String> valueMap = new HashMap<String, String>();
		valueMap.put("currency", getcurrency().getCurrencyCode());
		valueMap.put("value", currencyValueFormatter.format(getvalue()));
		FieldValuesUpdate result = new FieldValuesUpdate(externalId, valueMap);
		return result;
	}

	public String toString() {
		String result = "PodioCurrency [";
		result += ("currency=" + currency);
		result += (", value=" + value);
		return (result + "]");
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((currency == null) ? 0 : currency.hashCode());
	    result = prime * result + ((value == null) ? 0 : value.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    PodioCurrency other = (PodioCurrency) obj;
	    if (currency == null) {
		if (other.currency != null)
		    return false;
	    } else if (!currency.equals(other.currency))
		return false;
	    if (value == null) {
		if (other.value != null)
		    return false;
	    } else if (!value.equals(other.value))
		return false;
	    return true;
	}
	
	

}
