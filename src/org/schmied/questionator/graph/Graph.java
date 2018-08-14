package org.schmied.questionator.graph;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.schmied.questionator.*;
import org.schmied.questionator.importer.entity.*;

public class Graph {

	public static class Definition {

		public final int transitiveProperty;
		public final int[] leafProperties;

		public Definition(final int transitiveProperty, final int[] leafProperties) {
			this.transitiveProperty = transitiveProperty;
			this.leafProperties = leafProperties == null ? new int[] {} : leafProperties;
		}
	}

	//public static final int DEBUG_ID = -1;

	//public final int propertyId;
	public final Definition definition;

	private SortedMap<Integer, Node> transitives;
	private SortedSet<Node> rootTransitives, leafTransitives;

	public Graph(final Definition definition) {
		this.definition = definition;
	}

	private synchronized void invalidate() {
		transitives = null;
		rootTransitives = null;
		leafTransitives = null;
	}

	private SortedSet<Integer> transitiveIds(final Connection cn) {
		final long ticks = System.currentTimeMillis();
		final SortedSet<Integer> transitiveIds = new TreeSet<>();
		try (final Statement st = cn.createStatement();
				final ResultSet rs = st.executeQuery("SELECT item_id, value FROM claim_item WHERE property_id = " + definition.transitiveProperty)) {
			while (rs.next()) {
				transitiveIds.add(Integer.valueOf(rs.getInt(1)));
				transitiveIds.add(Integer.valueOf(rs.getInt(2)));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("transitive ids " + definition.transitiveProperty + ": " + transitiveIds.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
//		if (DEBUG_ID > 0)
//			System.out.println(">>> node ids contains " + DEBUG_ID + ": " + itemIds.contains(Integer.valueOf(DEBUG_ID)));
		return transitiveIds;
	}

	private SortedMap<Integer, Node> unconnectedTransitives(final Connection cn) {
		final int[] transitiveIds = Questionator.intArray(transitiveIds(cn));
		final long ticks = System.currentTimeMillis();
		final SortedMap<Integer, Node> unconnectedTransitives = new TreeMap<>();
		int idx = 0;
		while (idx < transitiveIds.length) {
			final List<Integer> bucket = ImportEntity.sqlBucket(transitiveIds, idx);
			idx += bucket.size();
			try (final Statement st = cn.createStatement();
					final ResultSet rs = ImportEntity.sqlBucketResultSet(bucket, st, "item_id, label_en", "item", "item_id", null)) {
				while (rs.next()) {
					final Integer itemId = Integer.valueOf(rs.getInt(1));
					unconnectedTransitives.put(itemId, new Node(itemId.intValue(), rs.getString(2)));
				}
			} catch (final SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		System.out.println(
				"unconnected transitives " + definition.transitiveProperty + ": " + unconnectedTransitives.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return unconnectedTransitives;
	}

	private synchronized SortedMap<Integer, Node> transitives(final Connection cn) {
		if (transitives != null)
			return transitives;

		transitives = unconnectedTransitives(cn);

		final long ticks = System.currentTimeMillis();
		final int[] itemIds = Questionator.intArray(transitives.keySet());
		int idx = 0;
		while (idx < itemIds.length) {
			final List<Integer> bucket = ImportEntity.sqlBucket(itemIds, idx);
			idx += bucket.size();
			try (final Statement st = cn.createStatement();
					final ResultSet rs = ImportEntity.sqlBucketResultSet(bucket, st, "item_id, value", "claim_item",
							"property_id = " + definition.transitiveProperty + " AND item_id", null)) {
				while (rs.next()) {
					final Integer childId = Integer.valueOf(rs.getInt(1));
					final Node child = transitives.get(childId);
					if (child == null) {
						System.out.println("child does not exist:" + childId);
						continue;
					}
					final Integer parentId = Integer.valueOf(rs.getInt(2));
					final Node parent = transitives.get(parentId);
					if (parent == null) {
						System.out.println("parent does not exist:" + parentId);
						continue;
					}
					Node.connect(parent, child);
				}
			} catch (final SQLException e) {
				e.printStackTrace();
				invalidate();
				return null;
			}
		}
		System.out.println("nodes " + definition.transitiveProperty + ": " + transitives.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return transitives;
	}

	public synchronized SortedSet<Node> rootTransitives(final Connection cn) {
		if (rootTransitives != null)
			return rootTransitives;
		final Collection<Node> n = transitives(cn).values();
		final long ticks = System.currentTimeMillis();
		rootTransitives = new TreeSet<>(n.stream().filter(c -> c.parents() == null).collect(Collectors.toSet()));
		System.out.println("root nodes " + definition.transitiveProperty + ": " + rootTransitives.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return rootTransitives;
	}

	public synchronized SortedSet<Node> leafTransitives(final Connection cn) {
		if (leafTransitives != null)
			return leafTransitives;
		final Collection<Node> n = transitives(cn).values();
		final long ticks = System.currentTimeMillis();
		leafTransitives = new TreeSet<>(n.stream().filter(c -> c.children() == null).collect(Collectors.toSet()));
		System.out.println("leaf nodes " + definition.transitiveProperty + ": " + leafTransitives.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return leafTransitives;
	}

	public int deleteUnpopularLeafTransitives(final Database db) throws Exception {
		final SortedSet<Node> leafs = leafTransitives(db.connection());
		final int[] ids = Questionator.intArray(leafs.stream().map(l -> Integer.valueOf(l.itemId)).collect(Collectors.toList()));
		final int[] unpopularIds = db.whereIn("item", "item_id", "popularity < " + ClaimEntity.MIN_POPULARITY_CNT, "item_id", ids);
		final int[] unreferencedIds = db.unreferenced(unpopularIds, null);
		final int cnt = ItemEntity.delete(db, unreferencedIds);
		System.out.println("delete unpopular leafes " + definition.transitiveProperty + ": leafes " + ids.length + ", unpopular " + unpopularIds.length
				+ ", unreferenced " + unreferencedIds.length);
		invalidate();
		return cnt;
	}

/*
	public boolean reduceValidateDelete(final Connection cn, final int unpopularItemId, final SortedSet<Integer> invalidItemIds) {
		final Node deleteNode = nodes(cn).get(Integer.valueOf(unpopularItemId));
		if (deleteNode == null)
			return true;
		return deleteNode.validateDelete(invalidItemIds);
	}

	public int reduceReconnect(final Connection cn, final SortedSet<Integer> deleteItemIds) {

		int reconnectCount = 0;

		try (final PreparedStatement ps = cn.prepareStatement("INSERT INTO claim_item (item_id, property_id, value) VALUES (?, " + propertyId + ", ?)")) {
			int idx = 0;
			for (final Integer deleteItemId : deleteItemIds) {
				final Node deleteNode = nodes.get(deleteItemId);
				if (deleteNode == null || deleteNode.children() == null || deleteNode.parents() == null)
					continue;
				for (final Node child : deleteNode.children()) {
					for (final Node parent : deleteNode.parents()) {
						//System.out.println(">>> " + propertyId + " " + deleteItemId + ": " + child.itemId + " -> " + parent.itemId);
						ps.setInt(1, child.itemId);
						ps.setInt(2, parent.itemId);
						ps.addBatch();
						if (idx > InsertDatabase.CAPACITY) {
							ps.executeBatch();
							idx = 0;
						} else {
							idx++;
						}
						reconnectCount++;
					}
				}
			}
			ps.executeBatch();
		} catch (final SQLException e) {
			e.printStackTrace();
			return -1;
		}

		// for instance-of items: move to parents of subclass-of 
		if (propertyId == 279) {
			final Database db = new Database(cn);
			try (final PreparedStatement ps = cn.prepareStatement("INSERT INTO claim_item (item_id, property_id, value) VALUES (?, 31, ?)")) {
				int idx = 0;
				for (final Integer deleteItemId : deleteItemIds) {
					final Node deleteNode = nodes.get(deleteItemId);
					if (deleteNode == null || deleteNode.parents() == null)
						continue;
					final int[] childIds = db.ids("claim_item", "item_id", "property_id = 31 AND value = " + deleteItemId);
					if (childIds == null)
						return -1;
					for (final int childId : childIds) {
						for (final Node parent : deleteNode.parents()) {
							//System.out.println(">>> 31 " + deleteItemId + ": " + childId + " -> " + parent.itemId);
							ps.setInt(1, childId);
							ps.setInt(2, parent.itemId);
							ps.addBatch();
							if (idx > InsertDatabase.CAPACITY) {
								ps.executeBatch();
								idx = 0;
							} else {
								idx++;
							}
							reconnectCount++;
						}
					}
				}
				ps.executeBatch();
			} catch (final SQLException e) {
				e.printStackTrace();
				return -1;
			}
		}
		invalidate();

		return reconnectCount;
	}
*/
}
