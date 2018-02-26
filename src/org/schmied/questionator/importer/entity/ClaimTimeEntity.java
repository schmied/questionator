package org.schmied.questionator.importer.entity;

import java.time.*;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class ClaimTimeEntity extends ClaimEntity {

	public final LocalDate value;
	public final short precision;

	public ClaimTimeEntity(final int itemId, final int propertyId, final LocalDate value, final short precision) {
		super(itemId, propertyId);
		this.value = value;
		this.precision = precision;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static ClaimTimeEntity claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"time".equals(json.opt("type")))
			return null;
		final JSONObject jValue = json.optJSONObject("value");
		if (jValue == null)
			return null;
		String time = jValue.optString("time");
		if (time == null)
			return null;
		time = time.replaceAll("^\\+", "");
		final short precision = (short) jValue.optInt("precision", 11);
		LocalDateTime value = null;
		try {
			value = LocalDateTime.parse(time, DateTimeFormatter.ISO_ZONED_DATE_TIME);
		} catch (final Exception e1) {
			try {
				final int year = Integer.parseInt(time.split("-\\d\\d-")[0]);
				value = LocalDateTime.of(year, 1, 1, 0, 0);
			} catch (final Exception e2) {
				System.out.println(
						"Q" + itemId + " P" + propertyId + ": Cannot parse year from " + time + " (" + e2.getClass().getSimpleName() + ": " + e2.getMessage() + ")");
				return null;
			}
		}
		final int year = value.getYear();
		if (year < -4700 || year > 10000) {
			System.out.println("Q" + itemId + " P" + propertyId + ": year " + year + " out of range for database");
			return null;
		}
		return new ClaimTimeEntity(itemId, propertyId, value.toLocalDate(), precision);
	}
}
