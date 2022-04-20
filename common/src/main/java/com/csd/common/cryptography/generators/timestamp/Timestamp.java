package com.csd.common.cryptography.generators.timestamp;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Timestamp implements Serializable {

	private static final long serialVersionUID = 4797942026816246761L;
	
	long timeStamp;
	String zoneId;

	public static Timestamp now() {
		return new Timestamp(ZoneId.systemDefault().getId());
	}

	public static Timestamp zero() {
		return new Timestamp(0,ZoneId.systemDefault().getId());
	}

	public Timestamp(String zoneId) {
		this.timeStamp = System.currentTimeMillis();
		this.zoneId = zoneId;
	}

	public Timestamp(long timeStamp, String zoneId) {
		this.timeStamp = timeStamp;
		this.zoneId = zoneId;
	}
	
	public Timestamp(Timestamp other, int plusMinutes) {
		this.timeStamp = other.timeStamp + TimeUnit.MINUTES.toMillis(plusMinutes);
		this.zoneId = other.zoneId;
	}
	
	public ZonedDateTime toDateTime() {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.of(zoneId));
	}
	
	Timestamp() {}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Timestamp timestamp = (Timestamp) o;
		return timeStamp == timestamp.timeStamp && zoneId.equals(timestamp.zoneId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(timeStamp, zoneId);
	}

	@Override
	public String toString() {
		return toDateTime().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
	}

	public static Timestamp fromString(String s) {
		ZonedDateTime dt = ZonedDateTime.parse(s,DateTimeFormatter.ISO_ZONED_DATE_TIME);
		return new Timestamp(dt.toInstant().toEpochMilli(),dt.getZone().getId());
	}
}
