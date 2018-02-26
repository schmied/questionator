package org.schmied.questionator.importer.entity;

import java.sql.Connection;
import java.util.*;

import org.json.JSONObject;
import org.schmied.questionator.*;
import org.schmied.questionator.graph.Graphs;

public class ItemEntity extends ImportEntity {

	public final int itemId;
	public final short popularity;
	public final String labelEn, labelDe;
	public final List<ClaimEntity> claims;

	public ItemEntity(final int itemId, final List<ClaimEntity> claims, final short popularity, final String labelEn, final String labelDe) {
		this.itemId = itemId;
		this.claims = claims;
		this.popularity = popularity;
		this.labelEn = labelEn;
		this.labelDe = labelDe;
	}

	@Override
	public String toString() {
		return "i" + itemId + ":" + labelEn + ":" + labelDe;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	private static final String label(final JSONObject labels, final String lang) {
		final JSONObject o = labels.optJSONObject(lang);
		if (o == null)
			return null;
		return Database.validString(o.optString("value"));
	}

	public static ItemEntity item(final JSONObject json) {
		if (json == null)
			return null;

		// labels
		final JSONObject jLabels = json.optJSONObject("labels");
		if (jLabels == null)
			return null;
		String labelEn = label(jLabels, "en");
		String labelDe = label(jLabels, "de");
		if (labelDe == null)
			labelDe = labelEn;
		if (labelEn == null)
			labelEn = labelDe;
		if (labelEn == null || labelDe == null)
			return null;

		// id
		final String idString = json.optString("id");
		if (idString == null)
			return null;
		if (idString.charAt(0) != 'Q')
			return null;
		final int itemId = Integer.valueOf(idString.substring(1)).intValue();

		// popularity
		final JSONObject jSitelinks = json.optJSONObject("sitelinks");
		final short popularity = jSitelinks == null ? 0 : (short) jSitelinks.length();

		final List<ClaimEntity> claims = ClaimEntity.claims(json.optJSONObject("claims"), itemId, popularity);
		if (claims == null)
			return null;

		return new ItemEntity(itemId, claims, popularity, labelEn, labelDe);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	public static boolean reduceItems(final Connection cn) {
		final Database db = new Database(cn);
		int[] unpopularItemIds = null;
		for (;;) {
			unpopularItemIds = db.ids("item", "item_id", "popularity < " + ClaimEntity.MIN_POPULARITY_CNT);
			if (unpopularItemIds == null)
				return false;
			if (unpopularItemIds.length == 0) {
				System.out.println("reduce items: all items reduced");
				return true;
			}

			final Graphs graphs = new Graphs();

			final int[] unpopularLeafIds131 = graphs.unpopularLeafIds(db, 131); // loc adm ter ent
			if (unpopularLeafIds131 != null && unpopularLeafIds131.length > 1000) {
				System.out.println(
						"reduce items: take " + unpopularLeafIds131.length + " locAdmTerEnt leaf nodes instead of " + unpopularItemIds.length + " random items.");
				unpopularItemIds = unpopularLeafIds131;
			} else {
				final int[] unpopularLeafIds171 = graphs.unpopularLeafIds(db, 171); // parent taxon
				if (unpopularLeafIds171 != null && unpopularLeafIds171.length > 1000) {
					System.out.println(
							"reduce items: take " + unpopularLeafIds171.length + " parentTaxon leaf nodes instead of " + unpopularItemIds.length + " random items.");
					unpopularItemIds = unpopularLeafIds171;
				}
			}

			final SortedSet<Integer> validatedItemIds = graphs.reduceValidateDelete(cn, unpopularItemIds);
			System.out.println("reduce items: " + validatedItemIds.size() + " / " + unpopularItemIds.length + " valid");
			graphs.reduceReconnect(cn, validatedItemIds);
			if (!delete(cn, Questionator.intArray(validatedItemIds)))
				return false;
		}
	}

	public static boolean delete(final Connection cn, final int[] itemIds) {

		final long ticks = System.currentTimeMillis();
		final Database db = new Database(cn);

		final int cntClaimGeo = db.delete("claim_geo", "item_id", itemIds);
		if (cntClaimGeo < 0)
			return false;
		final int cntClaimItem = db.delete("claim_item", "item_id", itemIds);
		if (cntClaimItem < 0)
			return false;
		final int cntClaimItemValue = db.delete("claim_item", "value", itemIds);
		if (cntClaimItemValue < 0)
			return false;
		final int cntClaimQuantity = db.delete("claim_quantity", "item_id", itemIds);
		if (cntClaimQuantity < 0)
			return false;
		final int cntClaimString = db.delete("claim_string", "item_id", itemIds);
		if (cntClaimString < 0)
			return false;
		final int cntClaimTime = db.delete("claim_time", "item_id", itemIds);
		if (cntClaimTime < 0)
			return false;
		final int cntItem = db.delete("item", "item_id", itemIds);
		if (cntItem < 0)
			return false;

		System.out.println("delete items: " + cntClaimGeo + " claim_geo, " + cntClaimItem + " claim_item (id), " + cntClaimItemValue + " claim_item (value), "
				+ cntClaimQuantity + " claim_quantity, " + cntClaimString + " claim_string, " + cntClaimTime + " claim_time, " + cntItem + " item / " + itemIds.length
				+ " [" + (System.currentTimeMillis() - ticks) + "ms]");

		return true;
	}
}
