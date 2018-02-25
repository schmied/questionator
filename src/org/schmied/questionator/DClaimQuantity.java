package org.schmied.questionator;

import java.sql.*;

import org.json.JSONObject;

public class DClaimQuantity extends DClaim {

	private final float value;
	private final int unit;

	public DClaimQuantity(final int itemId, final int propertyId, final float value, final int unit) {
		super(itemId, propertyId);
		this.value = value;
		this.unit = unit;
	}

	public final float value() {
		return value;
	}

	public final float unit() {
		return unit;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static DClaimQuantity claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"quantity".equals(json.opt("type")))
			return null;
		final JSONObject jValue = json.optJSONObject("value");
		if (jValue == null)
			return null;
		final float value = (float) jValue.optDouble("amount", 0.0);
		final String unitString = jValue.optString("unit");
		int unit = -1;
		if (unitString != null && !unitString.trim().isEmpty())
			unit = Integer.parseInt(unitString.replaceAll("\\D", ""));
		return new DClaimQuantity(itemId, propertyId, value, unit);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	private static final String SQL_INSERT = "INSERT INTO claim_quantity (item_id, property_id, value, unit) VALUES (?, ?, ?, ?)";

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
			psInsert.setFloat(3, value);
			psInsert.setInt(4, unit);
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
