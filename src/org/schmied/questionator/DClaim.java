package org.schmied.questionator;

import java.sql.*;
import java.util.*;

import org.json.*;
import org.schmied.questionator._legacy.DClass;
import org.schmied.questionator.graph.Graphs;

public abstract class DClaim extends DEntity {

	public abstract boolean insert(final Connection cn);

	// ---

	public static final int MIN_POPULARITY_CNT = 10;

	private final int itemId, propertyId;

	public DClaim(final int itemId, final int propertyId) {
		this.itemId = itemId;
		this.propertyId = propertyId;
	}

	public final int itemId() {
		return itemId;
	}

	public final int propertyId() {
		return propertyId;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	private static DClaim claim(final int itemId, final int propertyId, final Object o) {
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
			return DClaimItem.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("globe-coordinate"))
			return DClaimGeo.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("quantity"))
			return DClaimQuantity.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("string"))
			return DClaimString.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("commonsMedia"))
			return DClaimString.claim(itemId, propertyId, jDatavalue);
		if (datatype.equals("time"))
			return DClaimTime.claim(itemId, propertyId, jDatavalue);

		return null;
	}

	public static List<DClaim> claims(final JSONObject json, final int itemId, final short popularity) {

		final List<DClaim> claims = new ArrayList<>();
		final List<Integer> classes = new ArrayList<>();
		boolean isNode = false;

		for (final String propertyKey : json.keySet()) {
			final int propertyId = DProperty.propertyId(propertyKey);
			if (propertyId < 0)
				continue;

			final JSONArray jPropertyValues = json.getJSONArray(propertyKey);
			for (final Object jPropertyValue : jPropertyValues) {
				final DClaim claim = claim(itemId, propertyId, jPropertyValue);
				if (claim == null)
					continue;

				claims.add(claim);

				// remember class for skip-classes 
				if (propertyId == 31 || propertyId == 279)
					classes.add(Integer.valueOf(((DClaimItem) claim).value()));

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

	// ---------------------------------------------------------------------------------------------------------------- sql

	public static boolean recreateTables(final Connection cn) {
		try (final Statement st = cn.createStatement()) {
			st.execute("DROP TABLE IF EXISTS claim_geo");
			st.execute("DROP TABLE IF EXISTS claim_item");
			st.execute("DROP TABLE IF EXISTS claim_quantity");
			st.execute("DROP TABLE IF EXISTS claim_string");
			st.execute("DROP TABLE IF EXISTS claim_time");
			st.execute("CREATE TABLE IF NOT EXISTS claim_geo      (item_id INT4, property_id INT4, lat REAL, lng REAL)");
			st.execute("CREATE TABLE IF NOT EXISTS claim_item     (item_id INT4, property_id INT4, value INT4)");
			st.execute("CREATE TABLE IF NOT EXISTS claim_quantity (item_id INT4, property_id INT4, value REAL, unit INT4)");
			st.execute("CREATE TABLE IF NOT EXISTS claim_string   (item_id INT4, property_id INT4, value CHARACTER VARYING(" + MAX_STRING_LENGTH + "))");
			st.execute("CREATE TABLE IF NOT EXISTS claim_time     (item_id INT4, property_id INT4, value DATE, precision SMALLINT)");
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
