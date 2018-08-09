package org.schmied.questionator.importer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalTime;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.json.JSONObject;
import org.schmied.questionator.importer.db.*;
import org.schmied.questionator.importer.entity.*;

public abstract class Importer {

	private static final int MAX_ITEMS = 57000000;
	//private static final int MAX_ITEMS = 10000;

	private static final int BUFFER_SIZE_READER = 16 * 1024;
	private static final int BUFFER_SIZE_STREAM = 16 * 1024;

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

	private static void importStream(final ImporterDatabase db, final InputStream is) throws Exception {
		try (final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII); final BufferedReader br = new BufferedReader(isr, BUFFER_SIZE_READER)) {

			final long ticks = System.currentTimeMillis();
			int countImported = 0;

			br.readLine(); // first line cannot be importet

			int countRead = 0;
			for (;;) {
				try {
					final JSONObject json = readLine(br);
					if (json == null || countRead > MAX_ITEMS) {
						final long secondsElapsed = (System.currentTimeMillis() - ticks) / 1000;
						System.out.println(countImported + " / " + countRead + " " + (Math.round(100.0 * countImported / countRead)) + "% in "
								+ LocalTime.ofSecondOfDay(secondsElapsed).toString() + ", " + Math.round((double) countRead / secondsElapsed) + " items/s");
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
				countRead++;
			}

		} catch (final Exception e) {
			throw e;
		} finally {
			db.closeImport();
		}
	}

	private static void importPlainFile(final ImporterDatabase db, final Path file) throws Exception {
		try (final InputStream is = Files.newInputStream(file); final BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE_STREAM)) {
			importStream(db, bis);
		} catch (final Exception e) {
			throw e;
		}
	}

	private static void importBzipFile(final ImporterDatabase db, final Path file) throws Exception {
		try (final InputStream is = Files.newInputStream(file);
				final BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE_STREAM);
				final BZip2CompressorInputStream bzis = new BZip2CompressorInputStream(bis)) {
			importStream(db, bzis);
		} catch (final Exception e) {
			throw e;
		}
	}

	private static void importFile(final ImporterDatabase db, final Path file) throws Exception {

		if (file.getFileName().toString().toLowerCase().endsWith(".bz2")) {
			importBzipFile(db, file);
		} else if (file.getFileName().toString().toLowerCase().endsWith(".json")) {
			importPlainFile(db, file);
		} else {
			throw new Exception("unrecognized file format: " + file.getFileName().toString());
		}

		db.createIndexes();
		ClaimItemEntity.deleteInvalidReferences(db.connection());
		db.addConstraints();
	}

	private static boolean importAll(final ImporterDatabase db, final String file) {

		final Path path = Paths.get(file);
		if (!Files.isRegularFile(path)) {
			System.out.println("file does not exist.");
			return false;
		}

		try {
			db.recreateTables();
			db.insertProperties();
			importFile(db, path);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

//		if (!ItemEntity.reduceItems(db.connection()))
//			return false;
//		if (!ClaimItemEntity.deleteRedundant(db.connection()))
//			return false;

////		if (!DClass.initClasses(cn))
////			return false;

////		Topic.topics();

//		final Graphs graphs = new Graphs();
//		for (final Graph graph : graphs.graphs().values()) {
//			System.out.println("PROPERTY " + graph.propertyId);
//			for (final Node rootNode : graph.rootNodes(db.connection())) {
//				System.out.println(rootNode.description());
//			}
//		}

////		DItem.clean(cn, graphs);

		return true;
	}
}
