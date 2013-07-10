package com.java_podio.code_gen.static_classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.podio.item.FieldValuesUpdate;

public class PodioDate {

	protected final static SimpleDateFormat PODIO_DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected final static SimpleDateFormat PODIO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	protected final static SimpleDateFormat PODIO_TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

	private Date start = null;

	private boolean startTime = true;
	private boolean startDate = true;

	private Date end = null;

	private boolean endTime = true;
	private boolean endDate = true;

	public PodioDate() {
	}

	/**
	 * Parameters might be {@code null} or empty strings. If time pattern is
	 * 00:00:00 no time is assumed!
	 * 
	 * Format: 2013-07-27 00:00:00
	 * 
	 * @param start
	 * @param end
	 * @throws ParseException
	 * @see {@link #parseDate(String)}
	 */
	public PodioDate(String start, String end) throws ParseException {
		if (start != null && !start.equals("")) {
			setStartDate(true);
			setStart(PODIO_DATE_TIME_FORMATTER.parse(start));
			if (start.substring(11).equals("00:00:00")) {
				setStartTime(false);
			} else {
				setStartTime(true);
			}
		} else {
			setStartDate(false);
			setStartTime(false);
		}

		if (end != null && !end.equals("")) {
			setEndDate(true);
			setEnd(PODIO_DATE_TIME_FORMATTER.parse(end));
			if (end.substring(11).equals("00:00:00")) {
				setEndTime(false);
			} else {
				setEndTime(true);
			}
		} else {
			setEndDate(false);
			setEndTime(false);
		}

	}

	public PodioDate(Date start, Date end) {
		setStart(start);
		setEnd(end);
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public void setStart(Date start) {
		this.start = start;
		if (start == null) {
			setStartDate(false);
			setStartTime(false);
		} else {
			setStartDate(true);
		}
	}

	public void setEnd(Date end) {
		this.end = end;
		if (end == null) {
			setEndDate(false);
			setEndTime(false);
		} else {
			setEndDate(true);
		}
	}

	public boolean isStartTime() {
		return startTime;
	}

	public boolean isStartDate() {
		return startDate;
	}

	public boolean isEndTime() {
		return endTime;
	}

	public boolean isEndDate() {
		return endDate;
	}

	public void setStartTime(boolean startTime) {
		this.startTime = startTime;
	}

	public void setStartDate(boolean startDate) {
		this.startDate = startDate;
	}

	public void setEndTime(boolean endTime) {
		this.endTime = endTime;
	}

	public void setEndDate(boolean endDate) {
		this.endDate = endDate;
	}

	/**
	 * @param externalId
	 * @return
	 * @throws IllegalStateException
	 *             if no start date, but an end date is provided.
	 */
	public FieldValuesUpdate getFieldValuesUpdate(String externalId) {
		if (!isStartDate() && isEndDate()) {
			throw new IllegalStateException("Must have a start date, when having an end date!");
		}
		HashMap<String, String> dateHashMap = new HashMap<String, String>();
		if (isStartDate()) {
			dateHashMap.put("start",
					isStartTime() ? PODIO_DATE_TIME_FORMATTER.format(start)
							: (PODIO_DATE_FORMATTER.format(start) + " 00:00:00"));
			dateHashMap.put("start_date", PODIO_DATE_FORMATTER.format(start));
			dateHashMap.put("start_time", isStartTime() ? PODIO_TIME_FORMATTER.format(start) : null);
		}
		if (isEndDate()) {
			dateHashMap.put("end",
					isEndTime() ? PODIO_DATE_TIME_FORMATTER.format(end)
							: (PODIO_DATE_FORMATTER.format(end) + " 00:00:00"));
			dateHashMap.put("end_date", PODIO_DATE_FORMATTER.format(end));
			dateHashMap.put("end_time", isEndTime() ? PODIO_TIME_FORMATTER.format(end) : null);
		}
		return new FieldValuesUpdate(externalId, dateHashMap);
	}

	public static Date parseDate(String dateOrDateTime) throws ParseException {
		if (dateOrDateTime.length() <= 10) {
			return PODIO_DATE_FORMATTER.parse(dateOrDateTime);
		} else {
			return PODIO_DATE_TIME_FORMATTER.parse(dateOrDateTime);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + (endDate ? 1231 : 1237);
		result = prime * result + (endTime ? 1231 : 1237);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + (startDate ? 1231 : 1237);
		result = prime * result + (startTime ? 1231 : 1237);
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
		PodioDate other = (PodioDate) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (endDate != other.endDate)
			return false;
		if (endTime != other.endTime)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (startDate != other.startDate)
			return false;
		if (startTime != other.startTime)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PodioDate [start=" + start + ", startTime=" + startTime + ", startDate=" + startDate + ", end=" + end
				+ ", endTime=" + endTime + ", endDate=" + endDate + "]";
	}

}