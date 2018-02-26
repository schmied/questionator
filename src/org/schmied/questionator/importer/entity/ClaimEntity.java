package org.schmied.questionator.importer.entity;

import java.util.*;

import org.json.*;
import org.schmied.questionator._legacy.DClass;
import org.schmied.questionator.graph.Graphs;

public abstract class ClaimEntity extends ImportEntity {

	public static final int MIN_POPULARITY_CNT = 10;

	public final int itemId, propertyId;

	public ClaimEntity(final int itemId, final int propertyId) {
		this.itemId = itemId;
		this.propertyId = propertyId;
	}

/*
	public final int itemId() {
		return itemId;
	}

	public final int propertyId() {
		return propertyId;
	}
*/

	// ---------------------------------------------------------------------------------------------------------------- json

	private static ClaimEntity claim(final int itemId, final int propertyId, final Object o) {
		if (o == null)
			return null;
		if (!(o instanceof JSONObject))
			return null;
		final JSONObject json = (JSONObject) o;
		if (!"statement".equals(json.optString("type")))
			return null;
		final JSONObject jMainsnak = json.optJSONObject("mainsnak");
		if (jMainsnak == null)
			return null;
		if (!("P" + propertyId).equals(jMainsnak.opt("property")))
			return null;
		final String datatype = jMainsnak.optString("datatype");
		if (datatype == null)
			return null;
		if (!"value".equals(jMainsnak.opt("snaktype")))
			return null;
		final JSONObject jDatavalue = jMainsnak.optJSONObject("datavalue");
		if (jDatavalue == null)
			return null;

		if (datatype.equals("wikibase-item"))
			return ClaimItemEntity.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("globe-coordinate"))
			return ClaimGeoEntity.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("quantity"))
			return ClaimQuantityEntity.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("string"))
			return ClaimStringEntity.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("commonsMedia"))
			return ClaimStringEntity.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("time"))
			return ClaimTimeEntity.claim(itemId, propertyId, jDatavalue);

		return null;
	}

	public static List<ClaimEntity> claims(final JSONObject json, final int itemId, final short popularity) {

		final List<ClaimEntity> claims = new ArrayList<>();
		final List<Integer> classes = new ArrayList<>();
		boolean isNode = false;

		for (final String propertyKey : json.keySet()) {
			final int propertyId = PropertyEntity.propertyId(propertyKey);
			if (propertyId < 0)
				continue;

			final JSONArray jPropertyValues = json.getJSONArray(propertyKey);
			for (final Object jPropertyValue : jPropertyValues) {
				final ClaimEntity claim = claim(itemId, propertyId, jPropertyValue);
				if (claim == null)
					continue;

				claims.add(claim);

				// remember class for skip-classes 
				if (propertyId == 31 || propertyId == 279)
					classes.add(Integer.valueOf(((ClaimItemEntity) claim).value));

				if (Arrays.binarySearch(Graphs.PROPERTIES, propertyId) >= 0)
					isNode = true;
			}
		}
		if (claims.isEmpty())
			return null;

		if (!isNode && popularity < MIN_POPULARITY_CNT)
			return null;

		// do not allow instances or subclasses of SKIP_CLASS_IDS
		for (final Integer c : classes) {
			if (Arrays.binarySearch(DClass.IMPORT_SKIP_CLASS_IDS, c.intValue()) >= 0) {
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> skip item " + itemId);
				return null;
			}
		}

		return claims;
	}
}
