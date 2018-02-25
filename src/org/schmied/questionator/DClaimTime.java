package org.schmied.questionator;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class DClaimTime extends DClaim {

	private final LocalDate value;
	private final short precision;

	public DClaimTime(final int itemId, final int propertyId, final LocalDate value, final short precision) {
		super(itemId, propertyId);
		this.value = value;
		this.precision = precision;
	}

	public final LocalDate value() {
		return value;
	}

	public final short precision() {
		return precision;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static DClaimTime claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"time".equals(json.opt("type")))
			return null;
		final JSONObject jValue = json.optJSONObject("value");
		if (jValue == null)
			return null;
		String time = jValue.optString("time");
		if (time == null)
			return null;
//		final char firstChar = time.charAt(0);
//		if (!Character.isDigit(firstChar))
//			time = time.substring(1);
//		time = time.replace("-00", "-01");
		time = time.replaceAll("^\\+", "");
		final short precision = (short) jValue.optInt("precision", 11);
		LocalDateTime value = null;
		try {
			value = LocalDateTime.parse(time, DateTimeFormatter.ISO_ZONED_DATE_TIME);
//			value = LocalDateTime.parse(time, DateTimeFormatter.ISO_INSTANT);
		} catch (final Exception e1) {
			//System.out.println("Q" + itemId + " P" + propertyId + ": Cannot parse date time " + time);
			try {
				final int year = Integer.parseInt(time.split("-\\d\\d-")[0]);
				//System.out.println("Q" + itemId + " P" + propertyId + ": extract year " + year);
				value = LocalDateTime.of(year, 1, 1, 0, 0);
			} catch (final Exception e2) {
//				e2.printStackTrace();
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
		return new DClaimTime(itemId, propertyId, value.toLocalDate(), precision);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	private static final String SQL_INSERT = "INSERT INTO claim_time (item_id, property_id, value, precision) VALUES (?, ?, to_date(?, 'YYYY-MM-DD'), ?)";

	private static PreparedStatement psInsert;
	private static int psInsertIdx;

	@Override
	public boolean insert(final Connection cn) {
		try {
			if (psInsert == null || psInsert.isClosed()) {
				psInsert = cn.prepareStatement(SQL_INSERT);
				psInsertIdx = 0;
			}
			psInsert.setInt(1, itemId());
			psInsert.setInt(2, propertyId());
			psInsert.setString(3, value.format(DateTimeFormatter.ISO_LOCAL_DATE));
			psInsert.setShort(4, precision);
			psInsert.addBatch();
			if (psInsertIdx > MAX_BATCH_COUNT) {
				psInsert.executeBatch();
				psInsertIdx = 0;
			} else {
				psInsertIdx++;
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean insertClose() {
		return psClose(psInsert);
	}
}
