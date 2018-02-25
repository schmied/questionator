package org.schmied.questionator;

import java.sql.*;

import org.json.JSONObject;

public class DClaimGeo extends DClaim {

	final float lat, lng;

	public DClaimGeo(final int itemId, final int propertyId, final float lat, final float lng) {
		super(itemId, propertyId);
		this.lat = lat;
		this.lng = lng;
	}

	public final float lat() {
		return lat;
	}

	public final float lng() {
		return lng;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static DClaimGeo claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"globecoordinate".equals(json.opt("type")))
			return null;
		final JSONObject jValue = json.optJSONObject("value");
		if (jValue == null)
			return null;
		final float lat = (float) jValue.optDouble("latitude", 1111.1);
		if (lat > 1000.0f)
			return null;
		final float lng = (float) jValue.optDouble("longitude", 1111.1);
		if (lng > 1000.0f)
			return null;
		return new DClaimGeo(itemId, propertyId, lat, lng);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	private static final String SQL_INSERT = "INSERT INTO claim_geo (item_id, property_id, lat, lng) VALUES (?, ?, ?, ?)";

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
			psInsert.setFloat(3, lat);
			psInsert.setFloat(4, lng);
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
