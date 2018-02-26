package org.schmied.questionator.importer;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalTime;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.json.JSONObject;
import org.schmied.questionator.graph.*;
import org.schmied.questionator.importer.db.*;
import org.schmied.questionator.importer.entity.*;

public abstract class Importer {

	private static final int MAX_ITEMS = 51000000;

	public static boolean importInsert(final Connection cn, final String file) {
		try {
			return importAll(new InsertDatabase(cn), file);
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean importCopy(final Connection cn, final String file) {
		try {
			return importAll(new CopyDatabase(cn), file);
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
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

	private static boolean importStream(final ImporterDatabase db, final InputStream is) {
		try (final InputStreamReader isr = new InputStreamReader(is, "US-ASCII"); final BufferedReader br = new BufferedReader(isr, 16 * 1024)) {

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
					final ItemEntity item = ItemEntity.item(json);
					if (item != null) {
						if (db.addItem(item))
							countImported++;
						else
							System.out.println("ERROR item " + item.toString());
					}
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
							+ LocalTime.ofSecondOfDay(secondsElapsed).toString() + ", " + Math.round((double) countRead / secondsElapsed) + " items/s, ETA "
							+ (secondsEta > 86000 ? ">1d (" + Math.round(secondsEta / 60.0f / 60.0f) + "h)" : LocalTime.ofSecondOfDay(secondsEta).toString()));
				}
			}

			db.closeImport();

		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean importPlainFile(final ImporterDatabase db, final Path file) {
		try (final InputStream is = Files.newInputStream(file)) {
			if (!importStream(db, is))
				return false;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean importBzipFile(final ImporterDatabase db, final Path file) {
		try (final InputStream is = Files.newInputStream(file); final BZip2CompressorInputStream bz = new BZip2CompressorInputStream(is)) {
			if (!importStream(db, bz))
				return false;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean importFile(final ImporterDatabase db, final Path file) {

		if (file.getFileName().toString().toLowerCase().endsWith(".bz2")) {
			if (!importBzipFile(db, file))
				return false;
		} else if (file.getFileName().toString().toLowerCase().endsWith(".json")) {
			if (!importPlainFile(db, file))
				return false;
		} else {
			System.out.println("unrecognized file format: " + file.getFileName().toString());
			return false;
		}

		if (!db.createIndexes())
			return false;

		ClaimItemEntity.deleteInvalid(db.connection());

		if (!db.addConstraints())
			return false;

		return true;
	}

	private static boolean importAll(final ImporterDatabase db, final String file) {

		final Path path = Paths.get(file);
		if (!Files.isRegularFile(path)) {
			System.out.println("file does not exist.");
			return false;
		}

		if (!db.recreateTables())
			return false;
		if (!db.insertProperties())
			return false;
		if (!importFile(db, path))
			return false;
		if (!ItemEntity.reduceItems(db.connection()))
			return false;
		if (!ClaimItemEntity.deleteRedundant(db.connection()))
			return false;

////		if (!DClass.initClasses(cn))
////			return false;

////		Topic.topics();

		final Graphs graphs = new Graphs();
		for (final Graph graph : graphs.graphs().values()) {
			System.out.println("PROPERTY " + graph.propertyId);
			for (final Node rootNode : graph.rootNodes(db.connection())) {
				System.out.println(rootNode.description());
			}
		}

////		DItem.clean(cn, graphs);

		return true;
	}
}
