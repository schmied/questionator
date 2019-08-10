package org.schmied.questionator._legacy;

import java.util.*;
import java.util.stream.Collectors;

import org.schmied.questionator._legacy.d.DClass;

public class Topic implements Comparable<Object> {

	private static final int MIN_CARDINALITY = 1000;

	private final DClass rootClass;

	public Topic(final DClass rootClass) {
		this.rootClass = rootClass;
	}

	@Override
	public String toString() {
		return rootClass.labelEn;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		return rootClass.labelEn.equals(((Topic) o).rootClass.labelEn);
	}

	@Override
	public int hashCode() {
		return id(rootClass.labelDe);
	}

	private static final int id(final String label) {
		return -1 * (Math.abs(label.hashCode()) % 9999999);
	}

	@Override
	public int compareTo(final Object o) {
		if (o == null)
			return -1;
		return rootClass.labelEn.compareTo(((Topic) o).rootClass.labelEn);
	}

	public SortedSet<DClass> topClasses() {
		return rootClass.getChildren();
	}

	public static Set<Topic> topics() {

		final SortedSet<Topic> topics = new TreeSet<>();

		// construct topics out of ids from topic definition
		for (final String topicName : TOPIC_DEFINITIONS.keySet()) {
			final DClass rootClass = new DClass(id(topicName), topicName, topicName);
			final SortedSet<Integer> topClassIds = TOPIC_DEFINITIONS.get(topicName);
			for (final Integer topClassId : topClassIds) {
				final DClass topClass = DClass.cardinalClass(topClassId);
				if (topClass == null) {
					System.out.println("No top class id defined: " + topClassId);
					continue;
				}
				rootClass.add(topClass.cloneCardinalClassSubtree());
			}
			topics.add(new Topic(rootClass));
		}

		// remove class-sub-trees that exist as top classes in another topic
		final SortedSet<DClass> allTopClasses = new TreeSet<>(topics.stream().flatMap(t -> t.topClasses().stream()).collect(Collectors.toSet()));
		for (final Topic topic : topics) {
			final SortedSet<DClass> otherTopClasses = new TreeSet<>(allTopClasses);
			otherTopClasses.removeAll(topic.topClasses());
			for (final DClass topClass : topic.topClasses()) {
				final SortedSet<DClass> toRemoveClasses = new TreeSet<>(
						topClass.subclassesExclusive().stream().filter(c -> otherTopClasses.contains(c)).collect(Collectors.toSet()));
				if (!toRemoveClasses.isEmpty()) {
					System.out.println("top class: " + topClass + " remove classes: " + toRemoveClasses);
					for (final DClass toRemoveClass : toRemoveClasses)
						toRemoveClass.subclassesInclusive().forEach(c -> c.remove());
				}
			}
		}

		// remove top classes that already exist as non-root classes within topic
		for (final Topic topic : topics) {
			final SortedSet<DClass> allTopicClasses = new TreeSet<>(
					topic.topClasses().stream().flatMap(c -> c.subclassesExclusive().stream()).collect(Collectors.toSet()));
			final SortedSet<DClass> toRemoveClasses = new TreeSet<>(topic.topClasses().stream().filter(c -> allTopicClasses.contains(c)).collect(Collectors.toSet()));
			if (!toRemoveClasses.isEmpty()) {
				System.out.println("remove top classes: " + toRemoveClasses);
				toRemoveClasses.forEach(c -> c.remove());
				topic.topClasses().removeAll(toRemoveClasses);
			}
		}

		// collapse
		for (final Topic topic : topics) {
			int mergeCardinality = 1;
			do {
				final int mc = mergeCardinality;
				final DClass mergeClass = topic.rootClass.subclassesExclusive().stream().filter(c -> c.getParents() != null && c.getCardinality() < mc).findAny()
						.orElse(null);
				if (mergeClass != null) {
					if (!mergeClass.merge())
						System.out.println("merge error " + mergeClass.toString());
				} else {
					mergeCardinality++;
				}
			} while (mergeCardinality < MIN_CARDINALITY);

		}

		for (final Topic topic : topics)
			topic.rootClass.removeInvalidReferences();

		// print info
		for (final Topic topic : topics) {
			System.out.println();
			topic.rootClass.traverseInclusive(c -> {
				System.out.println(c.description());
				return Boolean.TRUE;
			});
		}

		return topics;
	}

	private static final SortedMap<String, SortedSet<Integer>> TOPIC_DEFINITIONS;
	static {
		TOPIC_DEFINITIONS = new TreeMap<>();

		final SortedSet<Integer> art = new TreeSet<>();
		art.add(Integer.valueOf(735)); // art
		art.add(Integer.valueOf(1004)); // comic 
		art.add(Integer.valueOf(11042)); // culture 
		art.add(Integer.valueOf(483453)); // fountain 
		art.add(Integer.valueOf(483501)); // artist
		art.add(Integer.valueOf(742421)); // theatre company
		art.add(Integer.valueOf(838948)); // work of art
		art.add(Integer.valueOf(858517)); // cultural history
		art.add(Integer.valueOf(1792644)); // art style
		art.add(Integer.valueOf(2198855)); // cultural movement
		art.add(Integer.valueOf(4502119)); // art group
		art.add(Integer.valueOf(4989906)); // monument
		art.add(Integer.valueOf(5193377)); // cultural institution
		art.add(Integer.valueOf(15090615)); // arts venue
		art.add(Integer.valueOf(15850590)); // theatrical genre 
//		art.add(Integer.valueOf(17537576)); // creative work 
		art.add(Integer.valueOf(18593264)); // item of collections or exhibitions
		TOPIC_DEFINITIONS.put("ART", art);

		final SortedSet<Integer> film = new TreeSet<>();
		film.add(Integer.valueOf(11424)); // film
		film.add(Integer.valueOf(28389)); // screenwriter 
		film.add(Integer.valueOf(33999)); // actor
		film.add(Integer.valueOf(130232)); // drama film
		film.add(Integer.valueOf(157394)); // fantasy film
		film.add(Integer.valueOf(157443)); // comdey film
		film.add(Integer.valueOf(200092)); // horror film
		film.add(Integer.valueOf(201658)); // film genre
		film.add(Integer.valueOf(319221)); // adventure film
		film.add(Integer.valueOf(369747)); // war film
		film.add(Integer.valueOf(471839)); // science fiction film
		film.add(Integer.valueOf(590870)); // cinematography
		film.add(Integer.valueOf(842256)); // musical film
		film.add(Integer.valueOf(859369)); // comedy-drama
		film.add(Integer.valueOf(959790)); // crime film
		film.add(Integer.valueOf(947873)); // television presenter
		film.add(Integer.valueOf(1054574)); // romance film
		film.add(Integer.valueOf(1366112)); // television drama
		film.add(Integer.valueOf(2526255)); // film director
		film.add(Integer.valueOf(3282637)); // film producer
		film.add(Integer.valueOf(4220920)); // filmmaking occupation
		film.add(Integer.valueOf(2431196)); // audiovisual work
		film.add(Integer.valueOf(15773317)); // television character
		film.add(Integer.valueOf(21198548)); // audiovisual production
		TOPIC_DEFINITIONS.put("FILM/TV", film);

		final SortedSet<Integer> foodDrink = new TreeSet<>();
		foodDrink.add(Integer.valueOf(2095)); // food
		foodDrink.add(Integer.valueOf(38695)); // cooking
		foodDrink.add(Integer.valueOf(40050)); // drink
//		foodDrink.add(Integer.valueOf(43164)); // kitchen
		foodDrink.add(Integer.valueOf(156839)); // cook
		foodDrink.add(Integer.valueOf(171141)); // gastronomy 
		foodDrink.add(Integer.valueOf(1252971)); // food maker 
		foodDrink.add(Integer.valueOf(1521410)); // kitchenware
		foodDrink.add(Integer.valueOf(1637030)); // food science
		foodDrink.add(Integer.valueOf(10675206)); // ingredient
//		foodDrink.add(Integer.valueOf(16920758)); // food preperation
//		foodDrink.add(Integer.valueOf(27038993)); // food establishment 
//		foodDrink.add(Integer.valueOf(27077054)); // catering company 
		TOPIC_DEFINITIONS.put("FOOD/DRINK", foodDrink);

		final SortedSet<Integer> geography = new TreeSet<>();
		geography.add(Integer.valueOf(15324)); // body of water
		geography.add(Integer.valueOf(82794)); // geographic region
//		geography.add(Integer.valueOf(271669)); // landform
		geography.add(Integer.valueOf(486972)); // human settlement
		geography.add(Integer.valueOf(2097994)); // municipal corporation
		geography.add(Integer.valueOf(19816755)); // artificial landform
//		geography.add(Integer.valueOf(20719696)); // physico-geographical object
		TOPIC_DEFINITIONS.put("GEOGRAPHY", geography);

		final SortedSet<Integer> humansAnimalsPlants = new TreeSet<>();
		humansAnimalsPlants.add(Integer.valueOf(420)); // biology
		humansAnimalsPlants.add(Integer.valueOf(8386)); // drug
		humansAnimalsPlants.add(Integer.valueOf(12136)); // disease
		humansAnimalsPlants.add(Integer.valueOf(12140)); // pharmaceutical drug
		humansAnimalsPlants.add(Integer.valueOf(16521)); // taxon
		humansAnimalsPlants.add(Integer.valueOf(502895)); // common name
		humansAnimalsPlants.add(Integer.valueOf(796194)); // medical procedure 
		humansAnimalsPlants.add(Integer.valueOf(1074814)); // surgical instrument
		humansAnimalsPlants.add(Integer.valueOf(2057971)); // health problem
		humansAnimalsPlants.add(Integer.valueOf(2996394)); // biological process 
		humansAnimalsPlants.add(Integer.valueOf(4260475)); // medical facility
		humansAnimalsPlants.add(Integer.valueOf(4936952)); // organ
		humansAnimalsPlants.add(Integer.valueOf(6657015)); // medical equipment
		humansAnimalsPlants.add(Integer.valueOf(7189713)); // physiological condition
		humansAnimalsPlants.add(Integer.valueOf(18479330)); // physical condition
		TOPIC_DEFINITIONS.put("HUMANS/ANIMALS/PLANTS", humansAnimalsPlants);

		final SortedSet<Integer> literature = new TreeSet<>();
		literature.add(Integer.valueOf(571)); // book
		literature.add(Integer.valueOf(8242)); // literature
		literature.add(Integer.valueOf(8253)); // fiction
		literature.add(Integer.valueOf(36180)); // writer
		literature.add(Integer.valueOf(352425)); // literary adaptation
		literature.add(Integer.valueOf(3658341)); // literary character
		literature.add(Integer.valueOf(7725634)); // literary work
		literature.add(Integer.valueOf(14897293)); // fictitious entity
		TOPIC_DEFINITIONS.put("LITERATURE", literature);

		final SortedSet<Integer> music = new TreeSet<>();
		music.add(Integer.valueOf(638)); // music 
		music.add(Integer.valueOf(7366)); // song 
		music.add(Integer.valueOf(34379)); // musical instrument 
		music.add(Integer.valueOf(639669)); // musician 
		music.add(Integer.valueOf(753110)); // songwriter 
		music.add(Integer.valueOf(868557)); // music festival 
		music.add(Integer.valueOf(2088357)); // musical ensemble 
		music.add(Integer.valueOf(2188189)); // musical work 
		TOPIC_DEFINITIONS.put("MUSIC", music);

		final SortedSet<Integer> religion = new TreeSet<>();
		religion.add(Integer.valueOf(190)); // god
		religion.add(Integer.valueOf(9134)); // mythology
//		religion.add(Integer.valueOf(9174)); // religion
		religion.add(Integer.valueOf(179461)); // religious text
		religion.add(Integer.valueOf(1234713)); // theologian
		religion.add(Integer.valueOf(1530022)); // religious organization
		religion.add(Integer.valueOf(5390013)); // belief system
		religion.add(Integer.valueOf(12617225)); // religious literature
//		religion.add(Integer.valueOf(19829980)); // religious studies scholar
		religion.add(Integer.valueOf(21029893)); // religious object
//		religion.add(Integer.valueOf(24262594)); // religious writer
		religion.add(Integer.valueOf(24334685)); // mythical entity
		TOPIC_DEFINITIONS.put("RELIGION/MYTHOLOGY", religion);

		final SortedSet<Integer> science = new TreeSet<>();
		science.add(Integer.valueOf(336)); // science
		science.add(Integer.valueOf(901)); // scientist
		science.add(Integer.valueOf(36534)); // chemical reaction
		science.add(Integer.valueOf(79529)); // chemical substance
		TOPIC_DEFINITIONS.put("SCIENCE", science);

		final SortedSet<Integer> society = new TreeSet<>();
		society.add(Integer.valueOf(315)); // language 
		society.add(Integer.valueOf(8192)); // writing system
		society.add(Integer.valueOf(41207)); // coin  
		society.add(Integer.valueOf(163740)); // nonprofit organization 
		society.add(Integer.valueOf(783794)); // company  
		society.add(Integer.valueOf(4830453)); // business enterprise
		society.add(Integer.valueOf(11105360)); // medium of exchange 
		society.add(Integer.valueOf(18536132)); // coin object  
		TOPIC_DEFINITIONS.put("SOCIETY", society);

		final SortedSet<Integer> sport = new TreeSet<>();
		sport.add(Integer.valueOf(349)); // sport
		sport.add(Integer.valueOf(718)); // chess
		sport.add(Integer.valueOf(476300)); // competition
		sport.add(Integer.valueOf(623109)); // sports league
		sport.add(Integer.valueOf(1076486)); // sports venue
		sport.add(Integer.valueOf(1539532)); // sport season
		sport.add(Integer.valueOf(2066131)); // sportsperson
		sport.add(Integer.valueOf(4438121)); // sports organization
		sport.add(Integer.valueOf(27020041)); // season
		TOPIC_DEFINITIONS.put("SPORT", sport);

		final SortedSet<Integer> temporalEntity = new TreeSet<>();
		temporalEntity.add(Integer.valueOf(26907166)); // temporalEntity
		TOPIC_DEFINITIONS.put("*TEMPORAL ENTITY*", temporalEntity);

		final SortedSet<Integer> work = new TreeSet<>();
		work.add(Integer.valueOf(386724)); // work
		TOPIC_DEFINITIONS.put("*WORK*", work);
	}

}

/*
	private static final SortedMap<Integer, Integer> UNWANTED_ROOTS;
	static {
		UNWANTED_ROOTS = new TreeMap<>();
		UNWANTED_ROOTS.put(Integer.valueOf(562061), Integer.valueOf(56061)); // marktgemeinde -> administrative territorial entity
		UNWANTED_ROOTS.put(Integer.valueOf(928830), Integer.valueOf(55488)); // metro station -> railway station
		UNWANTED_ROOTS.put(Integer.valueOf(1065118), Integer.valueOf(56061)); // district of china -> administrative territorial entity
		UNWANTED_ROOTS.put(Integer.valueOf(12813115), Integer.valueOf(486972)); // urban area of sweden -> human settlement
		UNWANTED_ROOTS.put(Integer.valueOf(1799794), Integer.valueOf(56061)); // administrative territorial entity of a specific level -> administrative territorial entity
		UNWANTED_ROOTS.put(Integer.valueOf(15916867), Integer.valueOf(56061)); // administrative territorial entity of a single country -> administrative territorial entity
	}

	private static SortedMap<Integer, DClass> allMainClasses(final Connection cn, final Map<Integer, DClass> allNonemptyClasses) {
		try {
			final SortedMap<Integer, DClass> allMainClasses = new TreeMap<>();

			// collect main classes from defined root class ids
			for (final SortedSet<Integer> rootClassIds : MAIN_ROOT_CLASS_IDS.values()) {
				for (final Integer rootClassId : rootClassIds) {
					allNonemptyClasses.get(rootClassId).traverseInclusive(c -> {
						allMainClasses.put(Integer.valueOf(c.classId), c);
						return Boolean.TRUE;
					});
				}
			}

			// remove references to classes outside of main classes
			for (final DClass mainClass : allMainClasses.values()) {
				if (mainClass.parents != null) {
					mainClass.parents = new TreeSet<>(
							mainClass.parents.stream().filter(p -> allMainClasses.containsKey(Integer.valueOf(p.classId))).collect(Collectors.toSet()));
					if (mainClass.parents.isEmpty())
						mainClass.parents = null;
				}
				if (mainClass.children != null) {
					mainClass.children = new TreeSet<>(
							mainClass.children.stream().filter(c -> allMainClasses.containsKey(Integer.valueOf(c.classId))).collect(Collectors.toSet()));
					if (mainClass.children.isEmpty())
						mainClass.children = null;
				}
			}

			// move unwanted
			for (final Integer uId : UNWANTED_ROOTS.keySet()) {
				final DClass unwantedRoot = allMainClasses.get(uId);
				if (unwantedRoot == null) {
					System.out.println("unwanted id " + uId + " not in all classes");
					continue;
				}
				final DClass destination = allMainClasses.get(UNWANTED_ROOTS.get(uId));
				if (destination == null) {
					System.out.println("destination id " + UNWANTED_ROOTS.get(uId) + " not in all classes");
					continue;
				}
				final SortedSet<DClass> unwanteds = unwantedRoot.allChildrenInclusive();
				for (final DClass unwanted : unwanteds) {
					if (unwanted.parents == null) {
						System.out.println("unwanted parent is null");
						continue;
					}
					// has non-unwanted parents? then do not merge
					if (unwanted.parents.stream().filter(up -> !unwanteds.contains(up)).collect(Collectors.toSet()).size() > 0)
						continue;
					destination.cardinality += unwanted.cardinality;
					if (destination.mergedChildren == null)
						destination.mergedChildren = new TreeSet<>();
					destination.mergedChildren.add(unwanted);
				}
				for (final DClass unwanted : unwanteds) {
					if (unwanted.parents != null) {
						unwanted.parents.stream().forEach(p -> {
							if (p.children != null) {
								p.children.remove(unwanted);
								if (p.children.isEmpty())
									p.children = null;
							}
						});
					}
					if (unwanted.children != null) {
						unwanted.children.stream().forEach(c -> {
							if (c.parents != null) {
								c.parents.remove(unwanted);
								if (c.parents.isEmpty())
									c.parents = null;
							}
						});
					}
					unwanted.parents = null;
					unwanted.children = null;
					unwanted.mergedChildren = null;
					unwanted.cardinality = 0;
					if (allMainClasses.remove(Integer.valueOf(unwanted.classId)) == null)
						System.out.println("unwanted id " + unwanted.classId + " not in all classes");
//					System.out.println("move unwanted " + unwanted.toString() + " to " + destination.toString());
				}
			}

			// collapse
			int mergeCardinality = 1;
			do {
				final int mc = mergeCardinality;
				final DClass mergeClass = allMainClasses.values().stream().filter(c -> c.parents != null && c.cardinality < mc).findAny().orElse(null);
				if (mergeClass != null) {
					if (mergeClass.merge()) {
						if (allMainClasses.remove(Integer.valueOf(mergeClass.classId)) == null)
							System.out.println("all main classes do not contain merge class " + mergeClass.toString());
					}
				} else {
					mergeCardinality++;
				}
			} while (mergeCardinality < MIN_CARDINALITY);

//			// merge leafes
//			int mergeLeafIterations = 0;
//			boolean isMerged;
//			do {
//				isMerged = false;
//				final List<DClass> allLeafClasses = allMainClasses.values().stream().filter(c -> c.children == null).sorted().collect(Collectors.toList());
//				for (final DClass leafClass : allLeafClasses) {
//					if (leafClass.cardinality < MIN_CARDINALITY) {
//						if (merge(leafClass)) {
//							if (allMainClasses.remove(Integer.valueOf(leafClass.classId)) == null)
//								System.out.println("all main classes do not contain leaf class " + leafClass.toString());
//							else
//								isMerged = true;
//						}
//					}
//				}
//				mergeLeafIterations++;
//			} while (isMerged);
//			System.out.println("merge leaf iterations: " + mergeLeafIterations);

			return allMainClasses;

		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean allClasses(final Connection cn) {
		try {
			final SortedMap<Integer, DClass> allMainClasses = allMainClasses(cn, allCardinalClasses(cn, allCardinalClassIds(cn, null)));
			final SortedSet<DClass> mainRootClasses = new TreeSet<>(allMainClasses.values().stream().filter(c -> c.parents == null).collect(Collectors.toSet()));

			for (final DClass rootClass : mainRootClasses) {
				System.out.println();
				System.out.println(rootClass.toString());
				rootClass.traverseExclusive(c -> {
					final String parentInfo = c.parents.stream().map(p -> p.labelEn).collect(Collectors.toList()).toString();
					System.out.println(c.toString() + parentInfo);
					return Boolean.TRUE;
				});
//				System.out.println(rootClass.toString() + " " + rootClass.cardinality);
			}

//			allClassIds = new ArrayList<>();
//			for (final DClass rootClass : allRootClasses) {
//				System.out.print(rootClass.classId + " " + rootClass.labelEn);
//				final int[] allChildIds = rootClass.allChildIds();
//				if (allChildIds == null) {
//					System.out.println(": skip no children");
//					continue;
//				}
//				System.out.print(" " + allChildIds.length + ": ");
//				idx = 0;
//				int count = 0;
//				while (idx < allChildIds.length) {
//					final List<Integer> bucket = sqlBucket(allChildIds, idx);
//					idx += bucket.size();
//					st = cn.createStatement();
//					rs = sqlBucketResultSet(bucket, st, "count(*)", "claim_item", "property_id = 31 AND value", null);
//					rs.next();
//					count += rs.getInt(1);
//					rs.close();
//					st.close();
//					if (count > MIN_ITEM_COUNT)
//						break;
//					st = cn.createStatement();
//					rs = sqlBucketResultSet(bucket, st, "count(*)", "claim_item", "property_id = 279 AND value", null);
//					rs.next();
//					count += rs.getInt(1);
//					rs.close();
//					st.close();
//					if (count > MIN_ITEM_COUNT)
//						break;
//				}
//				if (count > MIN_ITEM_COUNT) {
//					System.out.println("add" + (allChildIds.length < 100 ? " " + Arrays.toString(allChildIds) : ""));
//					for (int i = 0; i < allChildIds.length; i++) {
//						allClassIds.add(Integer.valueOf(allChildIds[i]));
//					}
//				} else {
//					System.out.println("skip (count " + count + ") " + Arrays.toString(allChildIds));
//				}
//			}
////			System.out.println("all class ids: " + allClassIds.size());
//
//			allClasses(cn, allClassIds);

		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
*/

// ---

/*
	private static boolean merge(final DClass mergeClass) {

		if (mergeClass.parents == null)
			return false;

//		final DClass parent = mergeClass.parents.iterator().next();
		// find parend with lowest cardinality
//		int lowCard = 999999;
//		DClass mergeParent = null;
//		for (final DClass p : mergeClass.parents) {
//			if (p.cardinality < lowCard) {
//				lowCard = p.cardinality;
//				mergeParent = p;
//			}
//		}
//		if (mergeParent == null)
//			return false;

		// cardinality
//		mergeParent.cardinality += mergeClass.cardinality;
//		mergeClass.cardinality = 0;

		// add to children of parent, remove from all parents, cardinality
		final int cardinality = Math.max(1, Math.round((float) mergeClass.cardinality / mergeClass.parents.size()));
//		if (mergeClass.children != null)
//			mergeParent.children.addAll(mergeClass.children);
		for (final DClass parent : mergeClass.parents) {
			if (parent.children == null) {
				System.out.println("parent " + parent.toString() + " of merge class " + mergeClass.toString() + " has no children");
				parent.children = new TreeSet<>();
			}
			// add cardinality to parent
			parent.cardinality += cardinality;
			// move children to parent
			if (mergeClass.children != null)
				parent.children.addAll(mergeClass.children);
			// remove from parent children
			if (!parent.children.remove(mergeClass))
				System.out.println("parent " + parent.toString() + " children do not include " + mergeClass.toString());
			// move merged classes to parent
			if (parent.mergedChildren == null)
				parent.mergedChildren = new TreeSet<>();
			parent.mergedChildren.add(mergeClass);
			if (mergeClass.mergedChildren != null)
				parent.mergedChildren.addAll(mergeClass.mergedChildren);
		}

		// modify parents of children
		if (mergeClass.children != null) {
			for (final DClass child : mergeClass.children) {
				if (!child.parents.remove(mergeClass))
					System.out.println("child " + child.toString() + " parents do not include " + mergeClass.toString());
				child.parents.addAll(mergeClass.parents);
			}
		}

		// remove references
		mergeClass.parents = null;
		mergeClass.children = null;
		mergeClass.mergedChildren = null;
		mergeClass.cardinality = 0;

		return true;
	}
*/