package org.schmied.questionator.importer.entity;

import java.sql.*;
import java.util.*;

import org.schmied.questionator.Questionator;

public abstract class ImportEntity {

	public static int[] validIds(final Connection cn, final String table) {
		final List<Integer> idList = new ArrayList<>();
		try (final Statement st = cn.createStatement(); final ResultSet rs = st.executeQuery("SELECT " + table + "_id FROM " + table)) {
			while (rs.next())
				idList.add(Integer.valueOf(rs.getInt(1)));
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		final int[] validIds = Questionator.intArray(idList);
		return validIds;
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
