package org.schmied.questionator;

import java.nio.file.*;
import java.sql.*;
import java.util.*;

import org.schmied.questionator.graph.*;

public class Questionator {

	private static final String DB_URL = "jdbc:postgresql://localhost/questionator_test?user=postgres&password=postgres";
//	private static final String DB_URL = "jdbc:postgresql://localhost/questionator_old?user=postgres&password=postgres";
	private static final String DUMP_FILE = "C:/Users/schmied/Downloads/wikidata-20180115-all.json.bz2";
//	private static final String DUMP_FILE = "C:/Users/schmied/Downloads/wikidata-test.json";

	private final Connection connection;

	private Questionator() throws Exception {
		try {
			connection = DriverManager.getConnection(DB_URL);
		} catch (final Exception e) {
			e.printStackTrace();
			close();
			throw e;
		}
	}

	private void close() {
		try {
			if (connection != null && !connection.isClosed())
				connection.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static Questionator instance() {
		try {
			return new Questionator();
		} catch (final Exception e) {
			return null;
		}
	}

	private static boolean importAll(final Connection cn) {

		if (!DProperty.importProperties(cn))
			return false;
		final Path file = Paths.get(DUMP_FILE);
		if (!DItem.importItems(file, cn))
			return false;
		if (!DItem.reduceItems(cn))
			return false;
		if (!DClaimItem.deleteRedundant(cn))
			return false;

////		if (!DClass.initClasses(cn))
////			return false;

////		Topic.topics();

		final Graphs graphs = new Graphs();
		for (final Graph graph : graphs.graphs().values()) {
			System.out.println("PROPERTY " + graph.propertyId);
			for (final Node rootNode : graph.rootNodes(cn)) {
				System.out.println(rootNode.description());
			}
		}

////		DItem.clean(cn, graphs);

		return true;
	}

	public static final int[] intArray(final Collection<? extends Number> c) {
		if (c == null)
			return null;
		final int[] a = new int[c.size()];
		int idx = 0;
		for (final Number n : c) {
			a[idx] = n.intValue();
			idx++;
		}
		Arrays.sort(a);
		return a;
	}

	public static void main(String[] argv) throws Exception {

		final Questionator q = Questionator.instance();
		if (q == null)
			return;

		importAll(q.connection);

		q.close();
	}
}

/*


SELECT sqrt(i.popularity::float * v.popularity::float) as pop, i.label_en, i.label_de, p.label_en, p.label_de, v.label_en, v.label_de FROM claim_item ci
  JOIN item v ON ci.value = v.item_id
  JOIN item i ON ci.item_id = i.item_id
  JOIN property p ON ci.property_id = p.property_id
  ORDER BY pop DESC, i.label_en
  LIMIT 100000



SELECT ?subclass ?subclassLabel ?instanceofSubclass ?instanceofSubclassLabel WHERE {
  ?subclass wdt:P279 wd:Q121998 .
  ?subclass wdt:P31 wd:Q13406463 .
  ?subclass wdt:P31 ?instanceofSubclass .
  FILTER (?instanceofSubclass != wd:Q13406463) .
  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
}
*/

//SELECT ?subclass ?subclassLabel WHERE {
//  ?subclass wdt:P279 wd:Q7725634 .
//  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
//}

//SELECT (COUNT(*) AS ?count) WHERE {
//  ?item wdt:P31 wd:Q5 .
//  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
//}

//SELECT ?subclass ?subclassLabel (COUNT(?item) AS ?count) WHERE {
//  ?subclass wdt:P279 wd:Q7725634 .
//  ?item wdt:P31 ?subclass .
//  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
//}
//GROUP BY ?subclass ?subclassLabel
//ORDER BY DESC(?count)
//LIMIT 100

/*
SELECT i.popularity, i.label_en, i.label_de, p.label_en, p.label_de, v.label_en, v.label_de FROM claim_item ci
  JOIN item v ON ci.value = v.item_id
  JOIN item i ON ci.item_id = i.item_id
  JOIN property p ON ci.property_id = p.property_id
  ORDER BY popularity DESC, i.label_en
  LIMIT 100000

-- property verteilung
SELECT i.popularity, count(*) from item i
  JOIN statement_item si ON si.item_id = i.item_id
  WHERE si.property_id = 19
  GROUP BY i.popularity
  ORDER BY i.popularity DESC
  LIMIT 10000

-- property auflistung
SELECT i.popularity, i.label_de from item i
  JOIN claim_item ci ON ci.item_id = i.item_id
  WHERE ci.property_id = 19
  ORDER BY i.popularity DESC
  LIMIT 10000

*/

//	private static final String[] DATATYPES = { "globe-coordinate", "quantity", "string", "time", "wikibase-item" };

/*
final CopyManager copyManager = ((PGConnection) cn).getCopyAPI();
final PushbackReader prItem = new PushbackReader(new StringReader(""), 10000);
final PushbackReader prProperty = new PushbackReader(new StringReader(""), 10000);
final StringBuilder sbItem = new StringBuilder();
final StringBuilder sbProperty = new StringBuilder();

private static final int insert(final String table, final CopyManager cm, final PushbackReader pr, final StringBuilder sb, final int idx,
		final int id, final String labelEn, final String labelDe) throws Exception {
	sb.append(id).append(",'").append(labelEn.replace("'", "")).append("','").append(labelDe.replace("'", "")).append("'\n");
	if (idx > MAX_INSERT_BATCH_COUNT) {
		System.out.println(sb.toString());
		pr.unread(sb.toString().toCharArray());
		cm.copyIn("COPY " + table + " FROM STDIN WITH CSV", pr);
		sb.delete(0, sb.length());
		return 0;
	}
	return idx + 1;
}

idxItem = insert("item", copyManager, prItem, sbItem, idxItem, id, labelEn, labelDe);
idxProperty = insert("property", copyManager, prProperty, sbProperty, idxProperty, id, labelEn, labelDe);

prItem.unread(sbItem.toString().toCharArray());
copyManager.copyIn("COPY item FROM STDIN WITH CSV", prItem);
prProperty.unread(sbProperty.toString().toCharArray());
copyManager.copyIn("COPY property FROM STDIN WITH CSV", prProperty);


if (Arrays.binarySearch(DATATYPES, datatype) < 0)
	continue;

*/
