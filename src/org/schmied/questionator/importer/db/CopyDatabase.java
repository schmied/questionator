package org.schmied.questionator.importer.db;

import java.sql.*;
import java.util.List;

import org.schmied.questionator.importer.entity.*;

public class CopyDatabase extends ImporterDatabase {

	private static final int CAPACITY = 8192;

	public CopyDatabase(final Connection connection) throws SQLException {
		super(connection, CAPACITY);
	}

	@Override
	public boolean closeImportResources() {
		return false;
	}

	@Override
	protected boolean flushItems(final List<ItemEntity> entities) {
		return false;
	}

	@Override
	protected boolean flushClaimsGeo(final List<ClaimGeoEntity> entities) {
		return false;
	}

	@Override
	protected boolean flushClaimsItem(final List<ClaimItemEntity> entities) {
		return false;
	}

	@Override
	protected boolean flushClaimsQuantity(final List<ClaimQuantityEntity> entities) {
		return false;
	}

	@Override
	protected boolean flushClaimsString(final List<ClaimStringEntity> entities) {
		return false;
	}

	@Override
	protected boolean flushClaimsTime(final List<ClaimTimeEntity> entities) {
		return false;
	}
}
