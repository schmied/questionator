package org.schmied.questionator.graph;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.schmied.questionator.*;

public class Graph {

	public static final int DEBUG_ID = -1;

	public final int propertyId;

	private SortedMap<Integer, Node> nodes;
	private SortedSet<Node> rootNodes, leafNodes;

	public Graph(final int propertyId) {
		this.propertyId = propertyId;
	}

	private synchronized void invalidate() {
		nodes = null;
		rootNodes = null;
	}

	private SortedSet<Integer> itemIds(final Connection cn) {
		final long ticks = System.currentTimeMillis();
		final SortedSet<Integer> itemIds = new TreeSet<>();
		try (final Statement st = cn.createStatement(); final ResultSet rs = st.executeQuery("SELECT item_id, value FROM claim_item WHERE property_id = " + propertyId)) {
			while (rs.next()) {
				itemIds.add(Integer.valueOf(rs.getInt(1)));
				itemIds.add(Integer.valueOf(rs.getInt(2)));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("item ids " + propertyId + ": " + itemIds.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
//		if (DEBUG_ID > 0)
//			System.out.println(">>> node ids contains " + DEBUG_ID + ": " + itemIds.contains(Integer.valueOf(DEBUG_ID)));
		return itemIds;
	}

	private SortedMap<Integer, Node> unconnectedNodes(final Connection cn) {
		final int[] itemIds = Questionator.intArray(itemIds(cn));
		final long ticks = System.currentTimeMillis();
		final SortedMap<Integer, Node> unconnectedNodes = new TreeMap<>();
		int idx = 0;
		while (idx < itemIds.length) {
			final List<Integer> bucket = DEntity.sqlBucket(itemIds, idx);
			idx += bucket.size();
			try (final Statement st = cn.createStatement(); final ResultSet rs = DEntity.sqlBucketResultSet(bucket, st, "item_id, label_en", "item", "item_id", null)) {
				while (rs.next()) {
					final Integer itemId = Integer.valueOf(rs.getInt(1));
					unconnectedNodes.put(itemId, new Node(itemId.intValue(), rs.getString(2)));
				}
			} catch (final SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		System.out.println("unconnected nodes " + propertyId + ": " + unconnectedNodes.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return unconnectedNodes;
	}

	public synchronized SortedMap<Integer, Node> nodes(final Connection cn) {
		if (nodes != null)
			return nodes;

		nodes = unconnectedNodes(cn);

		final long ticks = System.currentTimeMillis();
		final int[] itemIds = Questionator.intArray(nodes.keySet());
		int idx = 0;
		while (idx < itemIds.length) {
			final List<Integer> bucket = DEntity.sqlBucket(itemIds, idx);
			idx += bucket.size();
			try (final Statement st = cn.createStatement();
					final ResultSet rs = DEntity.sqlBucketResultSet(bucket, st, "item_id, value", "claim_item", "property_id = " + propertyId + " AND item_id", null)) {
				while (rs.next()) {
					final Integer childId = Integer.valueOf(rs.getInt(1));
					final Node child = nodes.get(childId);
					if (child == null) {
						System.out.println("child does not exist:" + childId);
						continue;
					}
					final Integer parentId = Integer.valueOf(rs.getInt(2));
					final Node parent = nodes.get(parentId);
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
		System.out.println("nodes " + propertyId + ": " + nodes.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return nodes;
	}

	public synchronized SortedSet<Node> rootNodes(final Connection cn) {
		if (rootNodes != null)
			return rootNodes;
		final Collection<Node> n = nodes(cn).values();
		final long ticks = System.currentTimeMillis();
		rootNodes = new TreeSet<>(n.stream().filter(c -> c.parents() == null).collect(Collectors.toSet()));
		System.out.println("root nodes " + propertyId + ": " + rootNodes.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return rootNodes;
	}

	public synchronized SortedSet<Node> leafNodes(final Connection cn) {
		if (leafNodes != null)
			return leafNodes;
		final Collection<Node> n = nodes(cn).values();
		final long ticks = System.currentTimeMillis();
		leafNodes = new TreeSet<>(n.stream().filter(c -> c.children() == null).collect(Collectors.toSet()));
		System.out.println("leaf nodes " + propertyId + ": " + leafNodes.size() + " [" + (System.currentTimeMillis() - ticks) + "ms]");
		return leafNodes;
	}

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
						if (idx > DEntity.MAX_BATCH_COUNT) {
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
							if (idx > DEntity.MAX_BATCH_COUNT) {
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
}
