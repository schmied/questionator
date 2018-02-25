package org.schmied.questionator;

import java.sql.*;
import java.util.*;

import org.json.JSONObject;

public class DClaimItem extends DClaim {

	private final int value;

	public DClaimItem(final int itemId, final int propertyId, final int value) {
		super(itemId, propertyId);
		this.value = value;
	}

	public final int value() {
		return value;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static DClaimItem claim(final int itemId, final int propertyId, final JSONObject json) {
		if (!"wikibase-entityid".equals(json.opt("type")))
			return null;
		final JSONObject jValue = json.optJSONObject("value");
		if (jValue == null)
			return null;
		if (!"item".equals(jValue.opt("entity-type")))
			return null;
		final int value = jValue.optInt("numeric-id", -1);
		if (value < 0)
			return null;
		return new DClaimItem(itemId, propertyId, value);
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	private static final String SQL_INSERT = "INSERT INTO claim_item (item_id, property_id, value) VALUES (?, ?, ?)";

	private static PreparedStatement psInsert;
	private static int psInsertIdx;

	@Override
	public boolean insert(final Connection cn) {
		if (itemId() == value) // XXX ???
			return false;
		try {
			if (psInsert == null || psInsert.isClosed()) {
				psInsert = cn.prepareStatement(SQL_INSERT);
				psInsertIdx = 0;
			}
			psInsert.setInt(1, itemId());
			psInsert.setInt(2, propertyId());
			psInsert.setInt(3, value);
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

	public static boolean deleteInvalid(final Connection cn) {
		final long ticks = System.currentTimeMillis();

		final List<Integer> itemIdList = new ArrayList<>();
		try (final Statement st = cn.createStatement(); final ResultSet rs = st.executeQuery("SELECT item_id FROM item")) {
			while (rs.next())
				itemIdList.add(Integer.valueOf(rs.getInt(1)));
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}

		final List<Integer[]> invalidClaims = new ArrayList<>();
		try (final Statement st = cn.createStatement(); final ResultSet rs = st.executeQuery("SELECT item_id, value FROM claim_item")) {
			final int[] itemIds = Questionator.intArray(itemIdList);
			while (rs.next()) {
				final int value = rs.getInt(2);
				if (Arrays.binarySearch(itemIds, value) >= 0)
					continue;
				invalidClaims.add(new Integer[] { Integer.valueOf(rs.getInt(1)), Integer.valueOf(value) });
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}

		try (final PreparedStatement psDeleteClaim = cn.prepareStatement("DELETE FROM claim_item WHERE item_id = ? AND value = ?")) {
			int idx = 0;
			for (final Integer[] invalidClaim : invalidClaims) {
				psDeleteClaim.setInt(1, invalidClaim[0].intValue());
				psDeleteClaim.setInt(2, invalidClaim[1].intValue());
				psDeleteClaim.addBatch();
				if (idx % MAX_BATCH_COUNT == 0 && idx > 0) {
					psDeleteClaim.executeBatch();
					//System.out.println(idx + " / " + invalidClaims.size() + " " + invalidClaim[0].intValue() + " " + invalidClaim[1].intValue());
				}
				idx++;
			}
			psDeleteClaim.executeBatch();
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}

		System.out.println("delete invalid claims: " + invalidClaims.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return true;
	}

	public static boolean deleteRedundant(final Connection cn) {
		final long ticks = System.currentTimeMillis();

		final List<int[]> redundantClaims = new ArrayList<>();
		try {
			//final String sql = "SELECT item_id, property_id, value, count(*) FROM claim_item WHERE property_id IN (31, " + Database.commaSeparate(Graphs.PROPERTIES)
			//		+ ") GROUP BY item_id, property_id, value HAVING count(*) > 1 ORDER BY property_id ASC, count DESC, item_id ASC";
			final String sql = "SELECT item_id, property_id, value, count(*) FROM claim_item GROUP BY item_id, property_id, value HAVING count(*) > 1 ORDER BY property_id ASC, count DESC, item_id ASC";
			//System.out.println(sql);
			final PreparedStatement pst = cn.prepareStatement(sql);
			final ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				final int[] i = new int[4];
				i[0] = rs.getInt(1);
				i[1] = rs.getInt(2);
				i[2] = rs.getInt(3);
				i[3] = rs.getInt(4);
				redundantClaims.add(i);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		try {
			final PreparedStatement pst = cn.prepareStatement("DELETE FROM claim_item WHERE item_id = ? AND property_id = ? AND value = ?");
			for (final int[] i : redundantClaims) {
				pst.setInt(1, i[0]);
				pst.setInt(2, i[1]);
				pst.setInt(3, i[2]);
				pst.addBatch();
			}
			pst.executeBatch();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		try {
			final PreparedStatement pst = cn.prepareStatement("INSERT INTO claim_item (item_id, property_id, value) VALUES (?, ?, ?)");
			for (final int[] i : redundantClaims) {
				pst.setInt(1, i[0]);
				pst.setInt(2, i[1]);
				pst.setInt(3, i[2]);
				pst.addBatch();
			}
			pst.executeBatch();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("removed redunant claims: " + redundantClaims.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return true;
	}
}
