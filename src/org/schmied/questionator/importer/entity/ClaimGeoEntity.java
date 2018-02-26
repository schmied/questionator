package org.schmied.questionator.importer.entity;

import org.json.JSONObject;

public class ClaimGeoEntity extends ClaimEntity {

	public final float lat, lng;

	public ClaimGeoEntity(final int itemId, final int propertyId, final float lat, final float lng) {
		super(itemId, propertyId);
		this.lat = lat;
		this.lng = lng;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static ClaimGeoEntity claim(final int itemId, final int propertyId, final JSONObject json) {
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
		return new ClaimGeoEntity(itemId, propertyId, lat, lng);
	}
}
