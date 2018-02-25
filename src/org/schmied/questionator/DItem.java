package org.schmied.questionator;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalTime;
import java.util.*;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.json.JSONObject;
import org.schmied.questionator.graph.Graphs;

public class DItem extends DEntity {

	private int itemId;
	private short popularity;
	private String labelEn, labelDe;
	private List<DClaim> claims;

	public DItem(final int itemId, final List<DClaim> claims, final short popularity, final String labelEn, final String labelDe) {
		this.itemId = itemId;
		this.claims = claims;
		this.popularity = popularity;
		this.labelEn = labelEn;
		this.labelDe = labelDe;
	}

	public final int itemId() {
		return itemId;
	}

	public final List<DClaim> claims() {
		return claims;
	}

	public final short popularity() {
		return popularity;
	}

	public final String labelEn() {
		return labelEn;
	}

	public final String labelDe() {
		return labelDe;
	}

	@Override
	public String toString() {
		return "i" + itemId + ":" + labelEn + ":" + labelDe;
	}

	// ---------------------------------------------------------------------------------------------------------------- import

	private static int importItem(final JSONObject json, final Connection cn) {
		final DItem item = DItem.item(json);
		if (item == null)
			return 0;
		return item.insert(cn);
	}

	private static final JSONObject readLine(final BufferedReader br) {
		try {
			final String line = br.readLine();
			if (line == null)
				return null;
			try {
				//System.out.println(line);
				return new JSONObject(line);
			} catch (final Exception e1) {
				System.out.println(line);
				e1.printStackTrace();
				return null;
			}
		} catch (final Exception e2) {
			e2.printStackTrace();
			return null;
		}
	}

	private static final int MAX_ITEMS = 51000000;

	public static boolean importItems(final Path file, final Connection cn) {

		if (!DClaim.recreateTables(cn))
			return false;
		if (!DItem.recreateTable(cn))
			return false;

//		try (final InputStream is = Files.newInputStream(dfFile);
		try (final FileInputStream fis = new FileInputStream(file.toFile());
				final BZip2CompressorInputStream bz = new BZip2CompressorInputStream(fis);
				final InputStreamReader isr = new InputStreamReader(bz, "US-ASCII");
				final BufferedReader br = new BufferedReader(isr, 1024 * 1024)) {

			final long ticks = System.currentTimeMillis();
			int countImported = 0;

			br.readLine(); // first line cannot be importet

			for (int countRead = 0; countRead < MAX_ITEMS; countRead++) {
				try {
					final JSONObject json = readLine(br);
					if (json == null) {
						System.out.println(countRead + " lines read.");
						break;
					}
					if (importItem(json, cn) > 0) // if (importItem(json, cn, validClassIds) > 0)
						countImported++;
				} catch (final Exception e) {
					System.out.println(e.getMessage());
					break;
				}
				if (countRead % 1000 == 0 && countRead > 0) {
					final long secondsElapsed = (System.currentTimeMillis() - ticks) / 1000;
					long secondsEta = Math.round((double) secondsElapsed * MAX_ITEMS / countRead - secondsElapsed);
					if (secondsEta < 0)
						secondsEta = 0;
					System.out.println(countImported + " / " + countRead + " " + (Math.round(100.0 * countImported / countRead)) + "% in "
							+ LocalTime.ofSecondOfDay(secondsElapsed).toString() + ", ETA "
							+ (secondsEta > 86000 ? " > 1d (" + Math.round(secondsEta / 60.0f / 60.0f) + "h)" : LocalTime.ofSecondOfDay(secondsEta).toString()));
				}
			}

			DItem.insertClose();

		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}

		// build indexes
		final long ticksIndex = System.currentTimeMillis();
		try (final Statement st = cn.createStatement()) {
			st.execute("ALTER TABLE item ADD CONSTRAINT pk_item_item_id PRIMARY KEY (item_id)");
			st.execute("CREATE INDEX idx_item_label_en ON item USING btree (label_en)");
			st.execute("CREATE INDEX idx_item_label_de ON item USING btree (label_de)");
			st.execute("CREATE INDEX idx_claim_geo_item_id          ON claim_geo      USING btree (item_id)");
			st.execute("CREATE INDEX idx_claim_geo_property_id      ON claim_geo      USING btree (property_id)");
			st.execute("CREATE INDEX idx_claim_item_item_id         ON claim_item     USING btree (item_id)");
			st.execute("CREATE INDEX idx_claim_item_property_id     ON claim_item     USING btree (property_id)");
			st.execute("CREATE INDEX idx_claim_item_value           ON claim_item     USING btree (value)");
			st.execute("CREATE INDEX idx_claim_quantity_item_id     ON claim_quantity USING btree (item_id)");
			st.execute("CREATE INDEX idx_claim_quantity_property_id ON claim_quantity USING btree (property_id)");
			st.execute("CREATE INDEX idx_claim_string_item_id       ON claim_string   USING btree (item_id)");
			st.execute("CREATE INDEX idx_claim_string_property_id   ON claim_string   USING btree (property_id)");
			st.execute("CREATE INDEX idx_claim_time_item_id         ON claim_time     USING btree (item_id)");
			st.execute("CREATE INDEX idx_claim_time_property_id     ON claim_time     USING btree (property_id)");
			st.execute("ANALYZE");
			//st.execute("REINDEX DATABASE questionator");
			//st.execute("ANALYZE");
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("index analzye reindex analyze [" + (System.currentTimeMillis() - ticksIndex) + "ms]");

		DClaimItem.deleteInvalid(cn);

		final long ticksConstraint = System.currentTimeMillis();
		try (final Statement st = cn.createStatement()) {
			st.execute("ALTER TABLE claim_geo      ADD CONSTRAINT fk_claim_geo_item_id      FOREIGN KEY (item_id) REFERENCES item (item_id)");
			st.execute("ALTER TABLE claim_item     ADD CONSTRAINT fk_claim_item_item_id     FOREIGN KEY (item_id) REFERENCES item (item_id)");
			st.execute("ALTER TABLE claim_item     ADD CONSTRAINT fk_claim_item_value       FOREIGN KEY (value)   REFERENCES item (item_id)");
			st.execute("ALTER TABLE claim_quantity ADD CONSTRAINT fk_claim_quantity_item_id FOREIGN KEY (item_id) REFERENCES item (item_id)");
			st.execute("ALTER TABLE claim_string   ADD CONSTRAINT fk_claim_string_item_id   FOREIGN KEY (item_id) REFERENCES item (item_id)");
			st.execute("ALTER TABLE claim_time     ADD CONSTRAINT fk_claim_time_item_id     FOREIGN KEY (item_id) REFERENCES item (item_id)");
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("add foreign key constraints [" + (System.currentTimeMillis() - ticksConstraint) + "ms]");

		return true;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	private static final String label(final JSONObject labels, final String lang) {
		final JSONObject o = labels.optJSONObject(lang);
		if (o == null)
			return null;
		return validString(o.optString("value"));
	}

	public static DItem item(final JSONObject json) {
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

		final List<DClaim> claims = DClaim.claims(json.optJSONObject("claims"), itemId, popularity);
		if (claims == null)
			return null;

		return new DItem(itemId, claims, popularity, labelEn, labelDe);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	public static final String SQL_INSERT = "INSERT INTO item (item_id, popularity, label_en, label_de) VALUES (?, ?, ?, ?)";

	private static PreparedStatement psInsert;
	private static int psInsertIdx;

	public static boolean recreateTable(final Connection cn) {
		try (final Statement st = cn.createStatement()) {
//			st.execute("DROP TABLE IF EXISTS claim_item");
			st.execute("DROP TABLE IF EXISTS item");
			st.execute("CREATE TABLE IF NOT EXISTS item (item_id INT4 NOT NULL, popularity SMALLINT, label_en CHARACTER VARYING(" + MAX_STRING_LENGTH
					+ "), label_de CHARACTER VARYING(" + MAX_STRING_LENGTH + "))");
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int insert(final Connection cn) {
		try {
			if (psInsert == null || psInsert.isClosed()) {
				psInsert = cn.prepareStatement(SQL_INSERT);
				psInsertIdx = 0;
			}
			psInsert.setInt(1, itemId);
			psInsert.setInt(2, popularity);
			psInsert.setString(3, labelEn);
			psInsert.setString(4, labelDe);
			psInsert.addBatch();
			if (psInsertIdx > MAX_BATCH_COUNT) {
				psInsert.executeBatch();
				psInsertIdx = 0;
			} else {
				psInsertIdx++;
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return 0;
		}

		int statementInsertCnt = 0;

		for (final DClaim claim : claims) {
			if (claim.insert(cn))
				statementInsertCnt++;
		}

		return statementInsertCnt;
	}

	public static boolean insertClose() {
		boolean b = psClose(psInsert);
		b &= DClaimGeo.insertClose();
		b &= DClaimItem.insertClose();
		b &= DClaimTime.insertClose();
		b &= DClaimQuantity.insertClose();
		b &= DClaimString.insertClose();
		return b;
	}

	public static boolean reduceItems(final Connection cn) {
		final Database db = new Database(cn);
		int[] unpopularItemIds = null;
		for (;;) {
			unpopularItemIds = db.ids("item", "item_id", "popularity < " + DClaim.MIN_POPULARITY_CNT);
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

/*
		// delete claims
		try (final PreparedStatement psClaimGeo = cn.prepareStatement("DELETE FROM claim_geo WHERE item_id = ?");
				final PreparedStatement psClaimItem = cn.prepareStatement("DELETE FROM claim_item WHERE item_id = ? OR value = ?");
				final PreparedStatement psClaimQuantity = cn.prepareStatement("DELETE FROM claim_quantity WHERE item_id = ?");
				final PreparedStatement psClaimString = cn.prepareStatement("DELETE FROM claim_string WHERE item_id = ?");
				final PreparedStatement psClaimTime = cn.prepareStatement("DELETE FROM claim_time WHERE item_id = ?")) {
			int idx = 0;
			for (final int itemId : itemIds) {
				psClaimGeo.setInt(1, itemId);
				psClaimGeo.addBatch();
				psClaimItem.setInt(1, itemId);
				psClaimItem.setInt(2, itemId);
				psClaimItem.addBatch();
				psClaimQuantity.setInt(1, itemId);
				psClaimQuantity.addBatch();
				psClaimString.setInt(1, itemId);
				psClaimString.addBatch();
				psClaimTime.setInt(1, itemId);
				psClaimTime.addBatch();
				if (idx > MAX_BATCH_COUNT) {
					psClaimGeo.executeBatch();
					psClaimItem.executeBatch();
					psClaimQuantity.executeBatch();
					psClaimString.executeBatch();
					psClaimTime.executeBatch();
					idx = 0;
				} else {
					idx++;
				}
			}
			psClaimGeo.executeBatch();
			psClaimItem.executeBatch();
			psClaimQuantity.executeBatch();
			psClaimString.executeBatch();
			psClaimTime.executeBatch();

		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}

		// delete items
		try (final PreparedStatement psItem = cn.prepareStatement("DELETE FROM item WHERE item_id = ?")) {
			int idx = 0;
			for (final int itemId : itemIds) {
				psItem.setInt(1, itemId);
				psItem.addBatch();
				if (idx > MAX_BATCH_COUNT) {
					psItem.executeBatch();
					idx = 0;
				} else {
					idx++;
				}
			}
			psItem.executeBatch();
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
*/
//		System.out.println("delete items: " + itemIds.length + " [" + (System.currentTimeMillis() - ticks) + "ms]");
	}
}
