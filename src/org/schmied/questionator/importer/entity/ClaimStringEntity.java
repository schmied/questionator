package org.schmied.questionator.importer.entity;

import org.json.JSONObject;
import org.schmied.questionator.Database;

public class ClaimStringEntity extends ClaimEntity {

	public final String value;

	public ClaimStringEntity(final int itemId, final int propertyId, final String value) {
		super(itemId, propertyId);
		this.value = value;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static ClaimStringEntity claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"string".equals(json.opt("type")))
			return null;
		final String value = Database.validString(json.optString("value"));
		if (value == null)
			return null;
		return new ClaimStringEntity(itemId, propertyId, value);
	}
}
