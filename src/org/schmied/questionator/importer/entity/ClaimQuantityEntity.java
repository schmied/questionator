package org.schmied.questionator.importer.entity;

import org.json.JSONObject;

public class ClaimQuantityEntity extends ClaimEntity {

	public final float value;
	public final int unit;

	public ClaimQuantityEntity(final int itemId, final int propertyId, final float value, final int unit) {
		super(itemId, propertyId);
		this.value = value;
		this.unit = unit;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static ClaimQuantityEntity claim(final int itemId, final int propertyId, final JSONObject json) {
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
		return new ClaimQuantityEntity(itemId, propertyId, value, unit);
	}
}
