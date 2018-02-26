package org.schmied.questionator._legacy;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.schmied.questionator.Questionator;
import org.schmied.questionator.importer.entity.ImportEntity;

@SuppressWarnings("all")
@Deprecated
public class DClass extends ImportEntity implements Comparable<Object> {

	public static final int DEBUG_ID = -1;

	private static Map<Integer, DClass> allCardinalClasses;

	public final int classId;
	public final String labelEn, labelDe;

	private SortedSet<DClass> parents;
	private SortedSet<DClass> children;
	private SortedSet<DClass> mergedChildren;

	private int cardinality;

	public DClass(final int classId, final String labelEn, final String labelDe) {
		this.classId = classId;
		this.labelEn = labelEn;
		this.labelDe = labelDe;
		this.cardinality = 0;
	}

	public SortedSet<DClass> getParents() {
		return parents;
	}

	public SortedSet<DClass> getChildren() {
		return children;
	}

	public SortedSet<DClass> getMergedChildren() {
		return mergedChildren;
	}

	public int getCardinality() {
		return cardinality;
	}

	@Override
	public String toString() {
		return classId + ":" + labelEn;
	}

	public String description() {
		final String parentInfo = parents == null ? "P[]" : "P" + parents.stream().map(p -> p.labelEn).sorted().collect(Collectors.toList()).toString();
		final String childInfo = children == null ? "C[]" : "C" + children.stream().map(p -> p.labelEn).sorted().collect(Collectors.toList()).toString();
		final String mergeInfo = mergedChildren == null ? "M[]"
				: "M" + mergedChildren.stream().limit(100).map(m -> m.labelEn).sorted().collect(Collectors.toList()).toString();
		return String.format("%8d %1d %2d %3d %6d %-24s %-24s %s   %s   %s", Integer.valueOf(classId),
				parents == null ? Integer.valueOf(0) : Integer.valueOf(parents.size()), children == null ? Integer.valueOf(0) : Integer.valueOf(children.size()),
				mergedChildren == null ? Integer.valueOf(0) : Integer.valueOf(mergedChildren.size()), Integer.valueOf(cardinality), labelEn, labelDe, parentInfo,
				childInfo, mergeInfo);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		return classId == ((DClass) o).classId;
	}

	@Override
	public int hashCode() {
		return classId;
	}

	@Override
	public int compareTo(final Object o) {
		if (o == null)
			return -1;
		return classId - ((DClass) o).classId;
	}

	private static Boolean traverse(final SortedSet<DClass> visited, final DClass current, final Function<DClass, Boolean> function) {
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
		for (final DClass child : current.children) {
			if (!traverse(visited, child, function).booleanValue())
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public Boolean traverseInclusive(final Function<DClass, Boolean> function) {
		final SortedSet<DClass> visited = new TreeSet<>();
		return traverse(visited, this, function);
	}

	public Boolean traverseExclusive(final Function<DClass, Boolean> function) {
		if (children == null)
			return Boolean.TRUE;
		final SortedSet<DClass> visited = new TreeSet<>();
		for (final DClass child : children) {
			if (!traverse(visited, child, function).booleanValue())
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public SortedSet<DClass> subclassesInclusive() {
		final SortedSet<DClass> subclasses = new TreeSet<>();
		traverseInclusive(c -> {
			subclasses.add(c);
			return Boolean.TRUE;
		});
		return subclasses;
	}

	public SortedSet<DClass> subclassesExclusive() {
		final SortedSet<DClass> subclasses = new TreeSet<>();
		traverseExclusive(c -> {
			subclasses.add(c);
			return Boolean.TRUE;
		});
		return subclasses;
	}

	public static DClass cardinalClass(final Integer id) {
		return allCardinalClasses.get(id);
	}

//	public boolean add(final Collection<DClass> childrenToAdd) {
	public boolean add(final DClass childToAdd) {
//		for (final DClass childToAdd : childrenToAdd) {
		if (classId == DEBUG_ID || childToAdd.classId == DEBUG_ID)
			System.out.println(">>> add 1 " + DEBUG_ID + ": this " + description() + "   to add " + childToAdd.description());
		if (children == null)
			children = new TreeSet<>();
		children.add(childToAdd);
		if (childToAdd.parents == null)
			childToAdd.parents = new TreeSet<>();
		childToAdd.parents.add(this);
//		}
		if (classId == DEBUG_ID || childToAdd.classId == DEBUG_ID)
			System.out.println(">>> add 2 " + DEBUG_ID + ": this " + description() + "   to add " + childToAdd.description());
		return true;
	}

	// remove class from the tree, split possible
	public boolean remove() {
		if (parents != null) {
			for (final DClass p : parents) {
				if (p.children != null) {
					p.children.remove(this);
					if (p.children.isEmpty())
						p.children = null;
				}
			}
		}
		if (children != null) {
			for (final DClass c : children) {
				if (c.parents != null) {
					c.parents.remove(this);
					if (c.parents.isEmpty())
						c.parents = null;
				}
			}
		}
		if (mergedChildren != null) {
			mergedChildren.clear();
			mergedChildren = null;
		}

		if (classId == DEBUG_ID)
			System.out.println(">>> remove " + DEBUG_ID);

		/*
		for (final DClass c : allChildrenInclusive()) {
			if (c.parents != null) {
				c.parents.clear();
				c.parents = null;
			}
			if (c.children != null) {
				c.children.clear();
				c.children = null;
			}
			if (c.mergedChildren != null) {
				c.mergedChildren.clear();
				c.mergedChildren = null;
			}
		}
		*/
		return true;
	}

	public DClass cloneCardinalClassSubtree() {
		// basic clone without references
		final SortedMap<Integer, DClass> clones = new TreeMap<>();
		for (final DClass subclass : subclassesInclusive()) {
			final DClass clone = new DClass(subclass.classId, subclass.labelEn, subclass.labelDe);
			clone.cardinality = subclass.cardinality;
			clones.put(Integer.valueOf(clone.classId), clone);
		}
		// set clone references to cloned objects
		for (final Integer id : clones.keySet()) {
			final DClass original = allCardinalClasses.get(id);
			final DClass clone = clones.get(id);
			// clone of root of subtree has no parents
			if (id.intValue() != classId && original.parents != null) {
				clone.parents = new TreeSet<>();
				for (final DClass parent : original.parents) {
					final DClass parentClone = clones.get(Integer.valueOf(parent.classId));
					if (parentClone == null)
						continue;
					clone.parents.add(parentClone);
				}
				if (clone.parents.isEmpty())
					clone.parents = null;
			}
			if (original.children != null) {
				clone.children = new TreeSet<>();
				for (final DClass child : original.children) {
					final DClass childClone = clones.get(Integer.valueOf(child.classId));
					if (childClone == null) {
						System.out.println("No child clone for " + child.toString());
						continue;
					}
					clone.children.add(childClone);
				}
				if (clone.children.isEmpty())
					clone.children = null;
			}
			if (original.mergedChildren != null) {
				clone.mergedChildren = new TreeSet<>();
				for (final DClass mc : original.mergedChildren) {
					clone.mergedChildren.add(new DClass(mc.classId, mc.labelEn, mc.labelDe));
				}
				if (clone.mergedChildren.isEmpty())
					clone.mergedChildren = null;
			}
		}

		final DClass clone = clones.get(Integer.valueOf(classId));
		if (clone.parents != null) {
			System.out.println("error: clone of root of subtree must not have any parents");
			clone.parents.clear();
			clone.parents = null;
		}

		//clone.removeInvalidReferences();

		return clone;
	}

	public boolean removeInvalidReferences() {
		final SortedSet<DClass> subclasses = new TreeSet<>(subclassesInclusive());
		int refCnt = 0;
		int removeCnt = 0;
		for (final DClass subclass : subclasses) {
			if (subclass.classId == DEBUG_ID)
				System.out.println(">>> remove invalid references 1 " + DEBUG_ID + " from " + toString() + ": " + subclass.description());
			if (subclass.parents != null) {
				refCnt += subclass.parents.size();
				final Set<DClass> toRemove = subclass.parents.stream().filter(p -> !subclasses.contains(p)).collect(Collectors.toSet());
				if (!toRemove.isEmpty()) {
					System.out.println("invalid parents remove: " + toString() + "/" + subclass.toString() + " " + toRemove.toString());
					subclass.parents.removeAll(toRemove);
					if (subclass.parents.isEmpty())
						subclass.parents = null;
					removeCnt += toRemove.size();
				}
			}
			if (subclass.children != null) {
				refCnt += subclass.children.size();
				final Set<DClass> toRemove = subclass.children.stream().filter(c -> !subclasses.contains(c)).collect(Collectors.toSet());
				if (!toRemove.isEmpty()) {
					System.out.println("invalid children remove: " + toString() + "/" + subclass.toString() + " " + toRemove.toString());
					subclass.children.removeAll(toRemove);
					if (subclass.children.isEmpty())
						subclass.children = null;
					removeCnt += toRemove.size();
				}
			}
			if (subclass.classId == DEBUG_ID)
				System.out.println(">>> remove invalid references 2 " + DEBUG_ID + " from " + toString() + ": " + subclass.description());
		}
		if (removeCnt > 0)
			System.out.println("invalid references removed: " + toString() + " " + removeCnt + "/" + refCnt + "/" + subclasses.size());
		return true;
	}

	public boolean merge() {

		if (parents == null)
			return false;

		final int cardinalityPerParent = Math.max(1, Math.round((float) cardinality / parents.size()));
		for (final DClass parent : parents) {
			if (parent.children == null) {
				System.out.println("parent " + parent.toString() + " of merge class " + toString() + " has no children");
				parent.children = new TreeSet<>();
			}
			// add cardinality to parent
			parent.cardinality += cardinalityPerParent;
			// move children to parent
			if (children != null)
				parent.children.addAll(children);
			// remove from parent children
			if (!parent.children.remove(this))
				System.out.println("parent " + parent.toString() + " children do not include " + toString());
			// move merged classes to parent
			if (parent.mergedChildren == null)
				parent.mergedChildren = new TreeSet<>();
			parent.mergedChildren.add(this);
			if (mergedChildren != null)
				parent.mergedChildren.addAll(mergedChildren);
		}

		// modify parents of children
		if (children != null) {
			for (final DClass child : children) {
				if (child.parents == null) {
					System.out.println("create parents for child " + child.toString() + " of " + toString());
					child.parents = new TreeSet<>();
				}
				if (!child.parents.remove(this))
					System.out.println("child " + child.toString() + " parents do not include " + toString());
				child.parents.addAll(parents);
			}
		}

		// remove references
		if (parents != null) {
			parents.clear();
			parents = null;
		}
		if (children != null) {
			children.clear();
			children = null;
		}
		if (mergedChildren != null) {
			mergedChildren.clear();
			mergedChildren = null;
		}
		cardinality = 0;

		return true;
	}

	// ---------------------------------------------------------------------------------------------------------------- import

//	private static final int MAX_SUBCLASS_COUNT = 1000;

/*
	private static SortedSet<Integer> allClassIds(final Connection cn) {
		final SortedSet<Integer> allClassIds = new TreeSet<>();
		try {
			final Statement st = cn.createStatement();
			final ResultSet rs = st.executeQuery("SELECT item_id, value FROM claim_item WHERE property_id = 279");
			while (rs.next()) {
				allClassIds.add(Integer.valueOf(rs.getInt(1)));
				allClassIds.add(Integer.valueOf(rs.getInt(2)));
			}
			rs.close();
			st.close();
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("all class ids: " + allClassIds.size());
		if (DEBUG_ID > 0)
			System.out.println(">>> all class ids contains " + DEBUG_ID + ": " + allClassIds.contains(Integer.valueOf(DEBUG_ID)));
		return allClassIds;
	}
*/

/*
	private static SortedSet<Integer> allLeafClassIds(final Connection cn, final Collection<Integer> allClassIds) {
		final SortedSet<Integer> allLeafClassIds = allClassIds == null ? new TreeSet<>(allClassIds(cn)) : new TreeSet<>(allClassIds);
		try {
			final Statement st = cn.createStatement();
			final ResultSet rs = st.executeQuery("SELECT value FROM claim_item WHERE property_id = 279");
			while (rs.next())
				allLeafClassIds.remove(Integer.valueOf(rs.getInt(1)));
			rs.close();
			st.close();
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("all leaf class ids: " + allLeafClassIds.size());
		if (DEBUG_ID > 0)
			System.out.println(">>> all leaf class ids contains " + DEBUG_ID + ": " + allLeafClassIds.contains(Integer.valueOf(DEBUG_ID)));
		return allLeafClassIds;
	}
*/

/*
	private static SortedSet<Integer> allNonCardinalClassIds(final Connection cn, final Collection<Integer> allClassIds) {
		try {
			final List<Integer> allLeafClassIds = new ArrayList<>(allLeafClassIds(cn, allClassIds == null ? allClassIds(cn) : allClassIds));
			final SortedSet<Integer> allNonCardinalClassIds = new TreeSet<>();
			int idx = 0;
			while (idx < allLeafClassIds.size()) {
				final List<Integer> bucket = sqlBucket(allLeafClassIds, idx);
				idx += bucket.size();
				final Statement st = cn.createStatement();
//				final ResultSet rs = sqlBucketResultSet(bucket, st, "value, count(*)", "claim_item", "property_id = 31 AND value", "GROUP BY value");
				final ResultSet rs = sqlBucketResultSet(bucket, st, "value, count(*)", "claim_item", "value", "GROUP BY value");
				while (rs.next())
					bucket.remove(Integer.valueOf(rs.getInt(1)));
				rs.close();
				st.close();
				for (final Integer id : bucket)
					allNonCardinalClassIds.add(id);
			}
			System.out.println("all non cardinal class ids: " + allNonCardinalClassIds.size());
			if (DEBUG_ID > 0)
				System.out.println(">>> all non cardinal class ids contains " + DEBUG_ID + ": " + allNonCardinalClassIds.contains(Integer.valueOf(DEBUG_ID)));
			return allNonCardinalClassIds;

		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}
*/

/*
	private static SortedSet<Integer> allCardinalClassIds(final Connection cn, Collection<Integer> allClassIds) {
		try {
			if (allClassIds == null)
				allClassIds = allClassIds(cn);
			final SortedSet<Integer> allCardinalClassIds = new TreeSet<>(allClassIds);
			final SortedSet<Integer> allNonCardinalClassIds = allNonCardinalClassIds(cn, allClassIds);
			for (final Integer nonCardinalClassId : allNonCardinalClassIds) {
				if (!allCardinalClassIds.remove(nonCardinalClassId))
					System.out.println("all class ids do not contain no-instance-leaf-class-id " + nonCardinalClassId);
			}
			System.out.println("all cardinal class ids: " + allCardinalClassIds.size());
			if (DEBUG_ID > 0)
				System.out.println(">>> all cardinal class ids contains " + DEBUG_ID + ": " + allCardinalClassIds.contains(Integer.valueOf(DEBUG_ID)));

			// remove unwanted classes
//			allClassIds.remove(Integer.valueOf(35120));		// entity
//			allClassIds.remove(Integer.valueOf(488383));	// object
//			allClassIds.remove(Integer.valueOf(7184903));	// abstract object
//			allClassIds.remove(Integer.valueOf(17553950));	// object

			return allCardinalClassIds;

		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}
*/

	private static SortedMap<Integer, DClass> allCardinalClasses(final Connection cn, final Collection<Integer> allCardinalClassIds) {
		try {
			final int[] classIds = Questionator.intArray(allCardinalClassIds);
			final SortedMap<Integer, DClass> classes = new TreeMap<>();
			int idx = 0;
			while (idx < classIds.length) {
				final List<Integer> bucket = sqlBucket(classIds, idx);
				idx += bucket.size();
				final Statement st = cn.createStatement();
				final ResultSet rs = sqlBucketResultSet(bucket, st, "item_id, label_en, label_de", "item", "item_id", null);
				while (rs.next()) {
					final Integer classId = Integer.valueOf(rs.getInt(1));
					classes.put(classId, new DClass(classId.intValue(), rs.getString(2), rs.getString(3)));
				}
				rs.close();
				st.close();
			}
			System.out.println("all cardinal classes: " + classes.size());

			final SortedMap<Integer, List<Integer>> parentIdsByClassId = new TreeMap<>();
			final SortedMap<Integer, List<Integer>> childIdsByClassId = new TreeMap<>();
			idx = 0;
			while (idx < classIds.length) {
				final List<Integer> bucket = sqlBucket(classIds, idx);
				idx += bucket.size();
				final Statement st = cn.createStatement();
				final ResultSet rs = sqlBucketResultSet(bucket, st, "item_id, value", "claim_item", "property_id = 279 AND item_id", null);
				while (rs.next()) {
					final Integer classId = Integer.valueOf(rs.getInt(1));
					if (Arrays.binarySearch(classIds, classId.intValue()) < 0) {
						System.out.println("classId does not exist:" + classId);
						continue;
					}
					final Integer parentId = Integer.valueOf(rs.getInt(2));
					if (Arrays.binarySearch(classIds, parentId.intValue()) < 0) {
						System.out.println("parentId does not exist:" + classId);
						continue;
					}

					List<Integer> lc = childIdsByClassId.get(parentId);
					if (lc == null) {
						lc = new ArrayList<>();
						childIdsByClassId.put(parentId, lc);
					}
					if (!lc.contains(classId))
						lc.add(classId);

					List<Integer> lp = parentIdsByClassId.get(classId);
					if (lp == null) {
						lp = new ArrayList<>();
						parentIdsByClassId.put(classId, lp);
					}
					if (!lp.contains(parentId))
						lp.add(parentId);
				}
				rs.close();
				st.close();
			}
			for (final int cId : classIds) {
				final Integer classId = Integer.valueOf(cId);
				final DClass c = classes.get(classId);
				final List<Integer> parentIds = parentIdsByClassId.get(classId);
				if (parentIds != null && !parentIds.isEmpty()) {
					c.parents = new TreeSet<>();
					parentIds.forEach(id -> c.parents.add(classes.get(id)));
				}
				final List<Integer> childIds = childIdsByClassId.get(classId);
				if (childIds != null && !childIds.isEmpty()) {
					c.children = new TreeSet<>();
					childIds.forEach(id -> c.children.add(classes.get(id)));
				}
			}

			// determine cardinality
//			idx = 0;
//			while (idx < classIds.length) {
//				final List<Integer> bucket = sqlBucket(classIds, idx);
//				idx += bucket.size();
//				final Statement st = cn.createStatement();
//				final ResultSet rs = sqlBucketResultSet(bucket, st, "value, count(*)", "claim_item", "property_id = 31 AND value", "GROUP BY value");
//				while (rs.next()) {
//					final DClass c = classes.get(Integer.valueOf(rs.getInt(1)));
//					c.cardinality += rs.getInt(2);
//				}
//				rs.close();
//				st.close();
//			}
//			idx = 0;
//			while (idx < classIds.length) {
//				final List<Integer> bucket = sqlBucket(classIds, idx);
//				idx += bucket.size();
//				final Statement st = cn.createStatement();
//				final ResultSet rs = sqlBucketResultSet(bucket, st, "value, count(*)", "claim_item", "property_id = 279 AND value", "GROUP BY value");
//				while (rs.next()) {
//					final DClass c = classes.get(Integer.valueOf(rs.getInt(1)));
//					c.cardinality += rs.getInt(2);
//					if (c.children != null) // cardinal children do not count to cardinality
//						c.cardinality -= c.children.size();
//				}
//				rs.close();
//				st.close();
//			}
			idx = 0;
			while (idx < classIds.length) {
				final List<Integer> bucket = sqlBucket(classIds, idx);
				idx += bucket.size();
				final Statement st = cn.createStatement();
				final ResultSet rs = sqlBucketResultSet(bucket, st, "value, count(*)", "claim_item", "value", "GROUP BY value");
				while (rs.next()) {
					final DClass c = classes.get(Integer.valueOf(rs.getInt(1)));
					c.cardinality += rs.getInt(2);
				}
				rs.close();
				st.close();
			}
			idx = 0;
			while (idx < classIds.length) {
				final List<Integer> bucket = sqlBucket(classIds, idx);
				idx += bucket.size();
				final Statement st = cn.createStatement();
				final ResultSet rs = sqlBucketResultSet(bucket, st, "item_id, count(*)", "claim_item", "item_id", "GROUP BY item_id");
				while (rs.next()) {
					final DClass c = classes.get(Integer.valueOf(rs.getInt(1)));
					c.cardinality += rs.getInt(2);
					if (c.children != null) // cardinal children do not count to cardinality
						c.cardinality -= c.children.size();
				}
				rs.close();
				st.close();
			}

			// merge unwanted classes
			for (final int id : UNWANTED_CARDINAL_CLASSES) {
				final Integer uwRootId = Integer.valueOf(id);
				final DClass uwRootClass = classes.get(uwRootId);
				if (uwRootClass == null) {
					System.out.println("No unwanted class with id " + uwRootId);
					continue;
				}
				for (final DClass uwClass : uwRootClass.subclassesInclusive()) {
					if (uwClass.merge())
						classes.remove(Integer.valueOf(uwClass.classId));
					else
						System.out.println("error merging unwanted: " + uwClass.toString());
				}
			}
			System.out.println("all cardinal classes without unwanted: " + classes.size());
			if (DEBUG_ID > 0) {
				final DClass c = classes.get(Integer.valueOf(DEBUG_ID));
				System.out.println(">>> all cardinal classes contains " + DEBUG_ID + ": " + (c == null ? "false" : c.description()));
			}

			return classes;

		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

/*
	public static boolean initClasses(final Connection cn) {

//		if (!DClass.recreateTable(cn, true))
//			return false;
//		allClasses(cn);

		allCardinalClasses = allCardinalClasses(cn, allCardinalClassIds(cn, null));

//		final SortedSet<DClass> rootClasses = new TreeSet<>(allCardinalClasses.values().stream().filter(c -> c.parents == null).collect(Collectors.toSet()));
//		for (final DClass rootClass : rootClasses) {
//			System.out.println();
//			System.out.println(rootClass.toString());
//			rootClass.traverseExclusive(c -> {
//				final String parentInfo = c.parents.stream().map(p -> p.labelEn).collect(Collectors.toList()).toString();
//				System.out.println(c.toString() + parentInfo);
//				return Boolean.TRUE;
//			});
//		}

		return true;
	}
*/

	// ---------------------------------------------------------------------------------------------------------------- sql

//	public static final String SQL_INSERT_SUB = "INSERT INTO class (class_id, parent_id, label_en, label_de) VALUES (?, ?, ?, ?)";
//	public static final String SQL_QUERY_SUB = "SELECT COUNT(*) FROM class WHERE class_id = ? AND parent_id = ?";

/*
	private static boolean recreateTable(final Connection cn, final boolean dropIfExists) {
		try {
			final Statement st = cn.createStatement();
			if (dropIfExists) {
				st.execute("DROP TABLE IF EXISTS class");
				st.execute("DROP TABLE IF EXISTS class_parent");
				st.execute("DROP TABLE IF EXISTS class_child");
			}
//			st.execute("CREATE TABLE IF NOT EXISTS class (class_id INT4, label_en CHARACTER VARYING(" + MAX_STRING_LENGTH + "), label_de CHARACTER VARYING("
//					+ MAX_STRING_LENGTH + "))");
			st.execute("CREATE TABLE IF NOT EXISTS class (class_id INT4, label_en CHARACTER VARYING(" + MAX_STRING_LENGTH + "), label_de CHARACTER VARYING("
					+ MAX_STRING_LENGTH + "))");
			st.execute("CREATE TABLE IF NOT EXISTS class_parent (class_id INT4, parent_id INT4)");
			st.execute("CREATE TABLE IF NOT EXISTS class_child (class_id INT4, child_id INT4)");
			st.close();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
*/

/*
	public static boolean clean(final Connection cn) {
		try {
			int toRemoveCnt;
			do {
				final List<Integer> allNonCardinalClassIds = new ArrayList<>(allNonCardinalClassIds(cn, null));
				//System.out.println("all non cardinal class ids: " + allNonCardinalClassIds.size());
				final SortedSet<Integer> allLessThanMinClassIds = new TreeSet<>();
				int idx = 0;
				while (idx < allNonCardinalClassIds.size()) {
					final List<Integer> bucket = sqlBucket(allNonCardinalClassIds, idx);
					idx += bucket.size();
					final Statement st = cn.createStatement();
					final ResultSet rs = sqlBucketResultSet(bucket, st, "item_id", "item", "popularity < " + DClaim.MIN_POPULARITY_CNT + " AND item_id", null);
					while (rs.next())
						allLessThanMinClassIds.add(Integer.valueOf(rs.getInt(1)));
					rs.close();
					st.close();
				}
				System.out.println("all less than min class ids: " + allLessThanMinClassIds.size());

				final PreparedStatement pstClaimGeo = cn.prepareStatement("DELETE FROM claim_geo WHERE item_id = ?");
				final PreparedStatement pstClaimItem = cn.prepareStatement("DELETE FROM claim_item WHERE item_id = ?");
				final PreparedStatement pstClaimItemValue = cn.prepareStatement("DELETE FROM claim_item WHERE value = ?");
				final PreparedStatement pstClaimQuantity = cn.prepareStatement("DELETE FROM claim_quantity WHERE item_id = ?");
				final PreparedStatement pstClaimTime = cn.prepareStatement("DELETE FROM claim_time WHERE item_id = ?");
				final PreparedStatement pstItem = cn.prepareStatement("DELETE FROM item WHERE item_id = ?");
				idx = 0;
				for (final Integer lessThanMinClassId : allLessThanMinClassIds) {
					final int id = lessThanMinClassId.intValue();
					pstClaimGeo.setInt(1, id);
					pstClaimGeo.addBatch();
					pstClaimItem.setInt(1, id);
					pstClaimItem.addBatch();
					pstClaimItemValue.setInt(1, id);
					pstClaimItemValue.addBatch();
					pstClaimQuantity.setInt(1, id);
					pstClaimQuantity.addBatch();
					pstClaimTime.setInt(1, id);
					pstClaimTime.addBatch();
					pstItem.setInt(1, id);
					pstItem.addBatch();
					if (idx > MAX_INSERT_BATCH_COUNT) {
						pstClaimGeo.executeBatch();
						pstClaimItem.executeBatch();
						pstClaimItemValue.executeBatch();
						pstClaimQuantity.executeBatch();
						pstClaimTime.executeBatch();
						pstItem.executeBatch();
						idx = 0;
					} else {
						idx++;
					}
				}
				pstClaimGeo.executeBatch();
				pstClaimItem.executeBatch();
				pstClaimItemValue.executeBatch();
				pstClaimQuantity.executeBatch();
				pstClaimTime.executeBatch();
				pstItem.executeBatch();
				pstClaimGeo.close();
				pstClaimItem.close();
				pstClaimItemValue.close();
				pstClaimQuantity.close();
				pstClaimTime.close();

				toRemoveCnt = allLessThanMinClassIds.size();
			} while (toRemoveCnt > 0);

			final Statement st = cn.createStatement();
			st.execute("VACUUM FULL ANALYZE");
			st.close();

		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
*/

	// ---------------------------------------------------------------------------------------------------------------- class definition

	private static final int[] UNWANTED_CARDINAL_CLASSES = { //
			562061, // marktgemeinde
			//			928830, // metro station
			1065118, // district of china
			12813115, // urban area of sweden
			1799794, // administrative territorial entity of a specific level
			15916867, // administrative territorial entity of a single country
	};

	public static final int[] IMPORT_SKIP_CLASS_IDS = { //

//			21674689, // admin-territorial entity of Ukraine

			7187, // gene
			8054, // protein
			11053, // RNA
			21199, // natural number
			139677, // Operon
			201448, // transfer RNA
			277338, // pseudogene
			284416, // small nucleolar RNA
			417841, // protein family
			420927, // protein complex
			427087, // non-coding RNA
			898273, // protein domain
			//			3270632, // national championship
			4167410, // wikimedia disambiguation page
			4167836, // wikimedia category
			7644128, // Supersecondary structure
			11266439, // wikimedia template
			//			13219666, // tennis tournament
			//			13357858, // badminton tournament
			13366104, // even number
			13366129, // odd number
			13406463, // wikimedia list article
			13442814, // scientific article
			17633526, // wikinews article
			14204246, // Wikimedia project page
			//			15061650, // golf tournament
			15184295, // Wikimedia module
			19842659, // Wikimedia user language template
			20010800, // Wikimedia user language category
			20747295, // protein-coding gene
			//			21167512, // chemical hazard
			//			23636313, // Greek minister
			//			24702381, // mayor of Vaud
			24719571, // Alcohol dehydrogenase superfamily, zinc-type
			24726117, // SDR
			24726420, // ABC transporter, permease
			24771218, // Transcription regulator HTH, AraC- type
			24774756, // Olfactory receptor
			24781630, // Amino acid/polyamine transporter I
			24781392, // MFS
			24787504, // Bordetella uptake gene
	};

	static {
		Arrays.sort(UNWANTED_CARDINAL_CLASSES);
		Arrays.sort(IMPORT_SKIP_CLASS_IDS);
	}
}
