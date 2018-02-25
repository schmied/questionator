package org.schmied.questionator;

import java.sql.*;
import java.util.*;

public class Database {

	private static final int IN_CLAUSE_MAX_COUNT = 400;

	private final Connection connection;

	public Database(final Connection connection) {
		this.connection = connection;
	}

	public Connection connection() {
		return connection;
	}

	private static int[] interval(final int ids[], final int begin) {
		int end = begin + IN_CLAUSE_MAX_COUNT;
		if (end > ids.length)
			end = ids.length;
		return Arrays.copyOfRange(ids, begin, end);
	}

	public static String commaSeparate(final int ids[]) {
		final StringBuilder sb = new StringBuilder();
		sb.append(ids[0]);
		for (int i = 1; i < ids.length; i++)
			sb.append(", " + ids[i]);
		return sb.toString();
	}

	public int[] ids(final String table, final String column, final String where) {
		try (final Statement st = connection.createStatement();
				final ResultSet rs = st.executeQuery("SELECT " + column + " FROM " + table + (where == null ? "" : " WHERE " + where))) {
			final List<Number> ids = new ArrayList<>();
			while (rs.next())
				ids.add((Number) rs.getObject(1));
			return Questionator.intArray(ids);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int[] filter(final int ids[], final String table, final String column, final String where) {
		final List<Number> filteredIds = new ArrayList<>();
		for (int intervalBegin = 0; intervalBegin < ids.length; intervalBegin += IN_CLAUSE_MAX_COUNT) {
			final int[] interval = interval(ids, intervalBegin);
			final String sql = "SELECT " + column + " FROM " + table + " WHERE " + where + " AND " + column + " IN(" + commaSeparate(interval) + ")";
			//System.out.println("---> " + sql);
			try (final Statement st = connection.createStatement(); final ResultSet rs = st.executeQuery(sql)) {
				while (rs.next())
					filteredIds.add((Number) rs.getObject(1));
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return Questionator.intArray(filteredIds);
	}

	public int delete(final String table, final String column, final int ids[]) {
		int deleteCount = 0;
		for (int intervalBegin = 0; intervalBegin < ids.length; intervalBegin += IN_CLAUSE_MAX_COUNT) {
			final int[] interval = interval(ids, intervalBegin);
			final String sql = "DELETE FROM " + table + " WHERE " + column + " IN(" + commaSeparate(interval) + ")";
			//System.out.println("---> " + sql);
			try (final PreparedStatement ps = connection.prepareStatement(sql)) {
				deleteCount += ps.executeUpdate();
			} catch (final Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
		return deleteCount;
	}

/*
	public boolean bla(final int[] ids) {
		if (ids == null)
			return true;
		try {
			for (final int id : ids) {
			}
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
*/
}
