package org.schmied.questionator;

import java.sql.*;

import org.json.JSONObject;

public class DClaimString extends DClaim {

	private final String value;

	public DClaimString(final int itemId, final int propertyId, final String value) {
		super(itemId, propertyId);
		this.value = value;
	}

	public final String value() {
		return value;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static DClaimString claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"string".equals(json.opt("type")))
			return null;
		final String value = validString(json.optString("value"));
		if (value == null)
			return null;
		return new DClaimString(itemId, propertyId, value);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	private static final String SQL_INSERT = "INSERT INTO claim_string (item_id, property_id, value) VALUES (?, ?, ?)";

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
			psInsert.setString(3, value);
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
