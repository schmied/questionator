package org.schmied.questionator.importer.db;

import java.io.StringReader;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.schmied.questionator.importer.entity.*;

public class CopyDatabase extends ImporterDatabase {

	private static final int CAPACITY = 64 * 1024;

	private final CopyManager copyManager;

	public CopyDatabase(final Connection connection) throws SQLException {
		super(connection, CAPACITY);
		copyManager = ((PGConnection) connection()).getCopyAPI();
	}

	@Override
	public boolean closeImportResources() {
		return false;
	}

	@Override
	protected boolean flushItems(final List<ItemEntity> entities) throws Exception {
		final StringBuilder sb = new StringBuilder(4 * 16 * CAPACITY);
		for (final ItemEntity e : entities) {
			sb.append(e.itemId);
			sb.append('\t');
			sb.append(e.popularity);
			sb.append('\t');
			sb.append(e.labelEn.replaceAll("\\s+", " "));
			sb.append('\t');
			sb.append(e.labelDe.replaceAll("\\s+", " "));
			sb.append('\n');
		}
		try (final StringReader sr = new StringReader(sb.toString())) {
			copyManager.copyIn("COPY item FROM STDIN", sr);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected boolean flushClaimsGeo(final List<ClaimGeoEntity> entities) throws Exception {
		final StringBuilder sb = new StringBuilder(4 * 16 * CAPACITY);
		for (final ClaimGeoEntity e : entities) {
			sb.append(e.itemId);
			sb.append('\t');
			sb.append(e.propertyId);
			sb.append('\t');
			sb.append(e.lat);
			sb.append('\t');
			sb.append(e.lng);
			sb.append('\n');
		}
		try (final StringReader sr = new StringReader(sb.toString())) {
			copyManager.copyIn("COPY claim_geo FROM STDIN", sr);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected boolean flushClaimsItem(final List<ClaimItemEntity> entities) throws Exception {
		final StringBuilder sb = new StringBuilder(4 * 16 * CAPACITY);
		for (final ClaimItemEntity e : entities) {
			sb.append(e.itemId);
			sb.append('\t');
			sb.append(e.propertyId);
			sb.append('\t');
			sb.append(e.value);
			sb.append('\n');
		}
		try (final StringReader sr = new StringReader(sb.toString())) {
			copyManager.copyIn("COPY claim_item FROM STDIN", sr);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected boolean flushClaimsQuantity(final List<ClaimQuantityEntity> entities) throws Exception {
		final StringBuilder sb = new StringBuilder(4 * 16 * CAPACITY);
		for (final ClaimQuantityEntity e : entities) {
			sb.append(e.itemId);
			sb.append('\t');
			sb.append(e.propertyId);
			sb.append('\t');
			sb.append(e.value);
			sb.append('\t');
			sb.append(e.unit);
			sb.append('\n');
		}
		try (final StringReader sr = new StringReader(sb.toString())) {
			copyManager.copyIn("COPY claim_quantity FROM STDIN", sr);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected boolean flushClaimsString(final List<ClaimStringEntity> entities) throws Exception {
		final StringBuilder sb = new StringBuilder(4 * 16 * CAPACITY);
		for (final ClaimStringEntity e : entities) {
			sb.append(e.itemId);
			sb.append('\t');
			sb.append(e.propertyId);
			sb.append('\t');
			sb.append(e.value.replaceAll("\\s+", " "));
			sb.append('\n');
		}
		try (final StringReader sr = new StringReader(sb.toString())) {
			copyManager.copyIn("COPY claim_string FROM STDIN", sr);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected boolean flushClaimsTime(final List<ClaimTimeEntity> entities) throws Exception {
		final StringBuilder sb = new StringBuilder(4 * 16 * CAPACITY);
		for (final ClaimTimeEntity e : entities) {
			sb.append(e.itemId);
			sb.append('\t');
			sb.append(e.propertyId);
			sb.append('\t');
			String value = e.value.format(DateTimeFormatter.ISO_LOCAL_DATE);
			if (value.startsWith("-"))
				value = value.substring(1) + " BC";
			sb.append(value);
			sb.append('\t');
			sb.append(e.precision);
			sb.append('\n');
		}
		try (final StringReader sr = new StringReader(sb.toString())) {
			copyManager.copyIn("COPY claim_time FROM STDIN", sr);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
