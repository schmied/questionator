package org.schmied.questionator.graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Node implements Comparable<Node> {

	public final int itemId;
	public final String label;

	private SortedSet<Node> parents;
	private SortedSet<Node> children;

	public Node(final int itemId, final String label) {
		this.itemId = itemId;
		this.label = label;
	}

	@Override
	public int compareTo(final Node n) {
		if (n == null)
			return -1;
		return itemId - n.itemId;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		return itemId == ((Node) o).itemId;
	}

	@Override
	public int hashCode() {
		return itemId;
	}

	@Override
	public String toString() {
		return itemId + ":" + label;
	}

	public String description() {
		final String parentInfo = parents == null ? "P[]" : "P" + parents.stream().map(p -> p.label).sorted().collect(Collectors.toList()).toString();
		final String childInfo = children == null ? "C[]" : "C" + children.stream().map(c -> c.label).sorted().collect(Collectors.toList()).toString();
		return String.format("%8d %1d %2d %-24s %s   %s", Integer.valueOf(itemId), Integer.valueOf(parentCount()), Integer.valueOf(childCount()), label, parentInfo,
				childInfo);
	}

	public SortedSet<Node> parents() {
		return parents;
	}

	public SortedSet<Node> children() {
		return children;
	}

	public int parentCount() {
		if (parents == null)
			return 0;
		return parents.size();
	}

	public int childCount() {
		if (children == null)
			return 0;
		return children.size();
	}

	public static boolean connect(final Node parent, final Node child) {
		if (parent == null || child == null) {
			System.err.println("Cannot connect non existing nodes.");
			return false;
		}

		if (parent.children == null)
			parent.children = new TreeSet<>();
		parent.children.add(child);

		if (child.parents == null)
			child.parents = new TreeSet<>();
		child.parents.add(parent);

		return true;
	}

	private static Boolean traverseDown(final SortedSet<Node> visited, final Node current, final Function<Node, Boolean> function) {
		if (current == null)
			return Boolean.TRUE;
		if (visited.contains(current)) {
			//System.out.println("already visited: " + current.toString());
			return Boolean.TRUE;
		}
		if (!function.apply(current).booleanValue())
			return Boolean.FALSE;
		visited.add(current);
		if (current.children == null)
			return Boolean.TRUE;
		for (final Node child : current.children) {
			if (!traverseDown(visited, child, function).booleanValue())
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public Boolean traverseDownInclusive(final Function<Node, Boolean> function) {
		final SortedSet<Node> visited = new TreeSet<>();
		return traverseDown(visited, this, function);
	}

	public Boolean traverseDownExclusive(final Function<Node, Boolean> function) {
		if (children == null)
			return Boolean.TRUE;
		final SortedSet<Node> visited = new TreeSet<>();
		for (final Node child : children) {
			if (!traverseDown(visited, child, function).booleanValue())
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public boolean validateDelete(final SortedSet<Integer> invalidItemIds) {
		if (invalidItemIds.contains(Integer.valueOf(itemId)))
			return false;
//		if (parents != null && parents.stream().filter(p -> invalidItemIds.contains(Integer.valueOf(p.itemId))).findAny().isPresent())
//			return false;
//		if (children != null && children.stream().filter(c -> invalidItemIds.contains(Integer.valueOf(c.itemId))).findAny().isPresent())
//			return false;

		if (parents != null)
			invalidItemIds.addAll(parents.stream().map(p -> Integer.valueOf(p.itemId)).collect(Collectors.toSet()));
		if (children != null)
			invalidItemIds.addAll(children.stream().map(c -> Integer.valueOf(c.itemId)).collect(Collectors.toSet()));

/*
		if (parents != null)
			parents.stream().forEach(p -> p.children.remove(this));
		if (children != null)
			children.stream().forEach(c -> c.parents.remove(this));
		if (parents != null && children != null) {
			parents.stream().forEach(p -> p.children.addAll(children));
			children.stream().forEach(c -> c.parents.addAll(parents));
		}
*/
		return true;
	}
}
