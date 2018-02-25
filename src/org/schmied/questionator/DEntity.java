package org.schmied.questionator;

import java.sql.*;
import java.util.*;

public class DEntity {

	public static final int MAX_BATCH_COUNT = 100;
	public static final int MAX_STRING_LENGTH = 100;

	@SuppressWarnings("all")
	public static final String validString(String s) {
		if (s == null)
			return null;
		s = s.trim();
		if (s.isEmpty())
			return null;
		if (s.length() > MAX_STRING_LENGTH) {
			if (s.length() > 2 * MAX_STRING_LENGTH)
				return null;
			s = s.substring(0, MAX_STRING_LENGTH);
		}
		return s;
	}

	public static int[] validIds(final Connection cn, final String table) {
		final List<Integer> idList = new ArrayList<>();
		try (final Statement st = cn.createStatement(); final ResultSet rs = st.executeQuery("SELECT " + table + "_id FROM " + table)) {
			while (rs.next())
				idList.add(Integer.valueOf(rs.getInt(1)));
//			rs.close();
//			st.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		final int[] validIds = Questionator.intArray(idList);
		return validIds;
	}

	public static boolean psClose(final PreparedStatement ps) {
		if (ps == null)
			return true;
		try {
			if (ps.isClosed())
				return true;
			ps.executeBatch();
			ps.close();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static final List<Integer> sqlBucket(final List<Integer> list, int idx) {
		if (idx >= list.size())
			return null;
		final List<Integer> bucket = new ArrayList<>();
		for (int i = 0; i < 500 && idx < list.size(); i++, idx++)
			bucket.add(list.get(idx));
		return bucket;
	}

	public static final List<Integer> sqlBucket(final int[] array, int idx) {
		if (idx >= array.length)
			return null;
		final List<Integer> bucket = new ArrayList<>();
		for (int i = 0; i < 500 && idx < array.length; i++, idx++)
			bucket.add(Integer.valueOf(array[idx]));
		return bucket;
	}

	public static final ResultSet sqlBucketResultSet(final List<Integer> bucket, final Statement st, final String selectColumns, final String table,
			final String whereColumn, final String appendix) {
		final String in = bucket.toString().replace("[", "").replace("]", "");
		try {
			return st
					.executeQuery("SELECT " + selectColumns + " FROM " + table + " WHERE " + whereColumn + " IN (" + in + ")" + (appendix != null ? " " + appendix : ""));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
