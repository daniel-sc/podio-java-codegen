package com.java_podio.code_gen.static_classes;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import com.podio.item.FieldValuesUpdate;

public class PodioCurrency implements PodioField {

	private Currency currency;
	private Double value;
	public static NumberFormat currencyValueFormatter = DecimalFormat.getInstance(Locale.US);

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

}
