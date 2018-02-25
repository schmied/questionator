package org.schmied.questionator.graph;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import org.schmied.questionator.*;

public class Graphs {

	public static final int[] PROPERTIES = { //
//			127, // owned by
			131, // located in the administrative territorial entity
			171, // parent taxon
			279, // subclass of
			361, // part of
			749, // parent organization
	};
	static {
		Arrays.sort(PROPERTIES);
	}

	private final SortedMap<Integer, Graph> graphs;

	public Graphs() {
		graphs = new TreeMap<>();
		for (final int p : PROPERTIES)
			graphs.put(Integer.valueOf(p), new Graph(p));
	}

	public SortedMap<Integer, Graph> graphs() {
		return graphs;
	}

	public Graph graph(final int property) {
		return graphs.get(Integer.valueOf(property));
	}

	public Graph graph(final Integer property) {
		return graphs.get(property);
	}

	public int[] unpopularLeafIds(final Database db, final int property) {
		final Graph graph = graph(property);
		if (graph == null)
			return null;
		final SortedSet<Node> leafNodes = graph.leafNodes(db.connection());
		if (leafNodes == null)
			return null;
		final int[] ids = Questionator.intArray(leafNodes.stream().map(l -> Integer.valueOf(l.itemId)).collect(Collectors.toList()));
		if (ids == null)
			return null;
		return db.filter(ids, "item", "item_id", "popularity < " + DClaim.MIN_POPULARITY_CNT);
	}

	public SortedSet<Integer> reduceValidateDelete(final Connection cn, final int[] unpopularItemIds) {
		final SortedSet<Integer> invalidItemIds = new TreeSet<>();
		final SortedSet<Integer> validatedItemIds = new TreeSet<>();
		for (final int unpopularItemId : unpopularItemIds) {
			boolean isValid = true;
			for (final Graph graph : graphs.values())
				isValid &= graph.reduceValidateDelete(cn, unpopularItemId, invalidItemIds);
			if (isValid)
				validatedItemIds.add(Integer.valueOf(unpopularItemId));
		}
		return validatedItemIds;
	}

	public boolean reduceReconnect(final Connection cn, final SortedSet<Integer> deleteItemIds) {
		final long ticks = System.currentTimeMillis();
		int reconnectCount = 0;
		for (final Graph graph : graphs.values()) {
			final int r = graph.reduceReconnect(cn, deleteItemIds);
			if (r < 0)
				return false;
			reconnectCount += r;
		}
		System.out.println("reduce reconnect all graphs: " + reconnectCount + " new connections [" + (System.currentTimeMillis() - ticks) + "ms]");
		return true;
	}
}
