package com.java_podio.code_gen.static_classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.podio.item.FieldValuesUpdate;

public class PodioDate {

	protected final static SimpleDateFormat PODIO_DATE_FORMATTER = (new SimpleDateFormat("yyyy-MM-dd"));

	protected final static SimpleDateFormat PODIO_DATE_TIME_FORMATTER = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

	private Date start = null;

	private Date end = null;

	public PodioDate() {
	}

	/**
	 * Parameters might be {@code null} or empty strings.
	 * 
	 * @param start
	 * @param end
	 * @throws ParseException
	 * @see {@link #parseDate(String)}
	 */
	public PodioDate(String start, String end) throws ParseException {
		if (start != null && !start.equals("")) {
			this.start = parseDate(start);
		}
		if (end != null && !end.equals("")) {
			this.end = parseDate(end);
		}
	}

	public PodioDate(Date start, Date end) {
		this.start = start;
		this.end = end;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public FieldValuesUpdate getFieldValuesUpdate(String externalId) {
		HashMap<String, String> dateHashMap = new HashMap<String, String>();
		dateHashMap.put("start", PODIO_DATE_TIME_FORMATTER.format(start));
		dateHashMap.put("end", PODIO_DATE_TIME_FORMATTER.format(end));
		return new FieldValuesUpdate(externalId, dateHashMap);
	}

	public static Date parseDate(String dateOrDateTime) throws ParseException {
		if (dateOrDateTime.length() <= 10) {
			return PodioDate.PODIO_DATE_FORMATTER.parse(dateOrDateTime);
		} else {
			return PodioDate.PODIO_DATE_TIME_FORMATTER.parse(dateOrDateTime);
		}
	}

	@Override
	public String toString() {
		return "PodioDate [start=" + start + ", end=" + end + "]";
	}
}
