package org.schmied.questionator.graph;

import java.util.*;

import org.schmied.questionator.graph.Graph.Definition;

public class Graphs {

/*
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
*/

	private static enum Name {
		LOCATED_IN_ADMINISTRATIVE_ENTITY, PARENT_TAXON, SUBCLASS_OF_INSTANCE_OF, SUBCLASS_OF_OCCUPATION, PART_OF, PARENT_ORGANIZATION
	}

	private static final SortedMap<Name, Definition> DEFINITIONS;
	static {
		DEFINITIONS = new TreeMap<>();
		DEFINITIONS.put(Name.LOCATED_IN_ADMINISTRATIVE_ENTITY, new Definition(131, null));
		DEFINITIONS.put(Name.PARENT_TAXON, new Definition(171, null));
		DEFINITIONS.put(Name.SUBCLASS_OF_INSTANCE_OF, new Definition(279, new int[] { 31 }));
		DEFINITIONS.put(Name.SUBCLASS_OF_OCCUPATION, new Definition(279, new int[] { 106 }));
		DEFINITIONS.put(Name.PART_OF, new Definition(361, null));
		DEFINITIONS.put(Name.PARENT_ORGANIZATION, new Definition(749, null));
	}

/*
	public static final int[] TRANSITIVE_PROPERTIES;
	static {
		final Set<Integer> props = DEFINITIONS.values().stream().map(d -> Integer.valueOf(d.transitiveProperty)).collect(Collectors.toSet());
		TRANSITIVE_PROPERTIES = Questionator.intArray(props);
	}
*/

	private final SortedMap<Name, Graph> graphs;

	public Graphs() {
		graphs = new TreeMap<>();
		for (final Name name : Name.values())
			graphs.put(name, new Graph(DEFINITIONS.get(name)));
	}

	public SortedMap<Name, Graph> graphs() {
		return graphs;
	}

/*
	public Graph graph(final int property) {
		return graphs.get(Integer.valueOf(property));
	}

	public Graph graph(final Integer property) {
		return graphs.get(property);
	}
*/

/*
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
*/
}
