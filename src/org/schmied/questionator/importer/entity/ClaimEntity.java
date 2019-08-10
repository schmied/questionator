package org.schmied.questionator.importer.entity;

import java.util.*;

import org.json.*;

public abstract class ClaimEntity extends ImportEntity {

	public static final int MIN_POPULARITY_CNT = 10;

	public final int itemId, propertyId;

	public ClaimEntity(final int itemId, final int propertyId) {
		this.itemId = itemId;
		this.propertyId = propertyId;
	}

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

				if (Arrays.binarySearch(TRANSITIVE_PROPERTIES, propertyId) >= 0)
					isNode = true;
			}
		}
		if (claims.isEmpty())
			return null;

		if (!isNode && popularity < MIN_POPULARITY_CNT)
			return null;

		// do not allow instances or subclasses of SKIP_CLASS_IDS
		for (final Integer c : classes) {
			if (Arrays.binarySearch(SKIP_IDS, c.intValue()) >= 0)
				return null;
		}

		return claims;
	}

	// ---

	private static final int[] SKIP_IDS = { //

			7187, // gene
			8054, // protein
			11053, // RNA
			21199, // natural number
			139677, // Operon
			201448, // transfer RNA
			277338, // pseudogene
			284416, // small nucleolar RNA
			417841, // protein family
			420927, // protein complex
			427087, // non-coding RNA
			898273, // protein domain
			4167410, // wikimedia disambiguation page
			4167836, // wikimedia category
			7644128, // Supersecondary structure
			11266439, // wikimedia template
			13366104, // even number
			13366129, // odd number
			13406463, // wikimedia list article
			13442814, // scientific article
			17633526, // wikinews article
			14204246, // Wikimedia project page
			15184295, // Wikimedia module
			19842659, // Wikimedia user language template
			20010800, // Wikimedia user language category
			20747295, // protein-coding gene
			24719571, // Alcohol dehydrogenase superfamily, zinc-type
			24726117, // SDR
			24726420, // ABC transporter, permease
			24771218, // Transcription regulator HTH, AraC- type
			24774756, // Olfactory receptor
			24781630, // Amino acid/polyamine transporter I
			24781392, // MFS
			24787504, // Bordetella uptake gene

	};
	static {
		Arrays.sort(SKIP_IDS);
	}

	private static final int[] TRANSITIVE_PROPERTIES = { //

			127, // owned by
			131, // located in the administrative territorial entity
			155, // follows
			156, // followed by
			171, // parent taxon
			279, // subclass of
			355, // subsidiary
			361, // part of
			527, // has part
			749, // parent organization
			1365, // replaces
			1366, // replaced by
			1830, // owner of

	};
	static {
		Arrays.sort(TRANSITIVE_PROPERTIES);
	}
}
