package org.schmied.questionator._legacy;

@Deprecated
public class DClassLegacy {

/*
			final List<Integer> noPopularLeafClassIds = new ArrayList<>();
			idx = 0;
			while (idx < leafClassIds.size()) {
				final List<Integer> bucket = new ArrayList<Integer>();
				for (int i = 0; i < 500 && idx < leafClassIds.size(); i++, idx++)
					bucket.add(leafClassIds.get(idx));
				final String in = bucket.toString().replace("[", "").replace("]", "");
				st = cn.createStatement();
				rs = st.executeQuery("SELECT ci.value, count(*) FROM claim_item ci JOIN item i ON i.item_id = ci.value WHERE i.popularity < " + DClaim.MIN_POPULARITY_CNT
						+ " AND ci.property_id = 31 AND ci.value IN (" + in + ") GROUP BY value");
				while (rs.next()) {
					if (rs.getInt(2) < 20)
						noPopularLeafClassIds.add(Integer.valueOf(rs.getInt(1)));
				}
				rs.close();
				st.close();
				if (idx % 1000 == 0)
					System.out.println(idx + " / " + leafClassIds.size());
			}
			System.out.println("no popular leaf class ids: " + noPopularLeafClassIds.size() + " " + noPopularLeafClassIds.toString());
			for (final Integer noPopularLeafClassId : noPopularLeafClassIds) {
				allClassIds.remove(noPopularLeafClassId);
				rootClassIds.remove(noPopularLeafClassId);
				leafClassIds.remove(noPopularLeafClassId);
			}
			System.out.println("all class ids: " + allClassIds.size() + " " + allClassIds.toString());
			System.out.println("root class ids: " + rootClassIds.size() + " " + rootClassIds.toString());
			System.out.println("leaf class ids: " + leafClassIds.size() + " " + leafClassIds.toString());
*/
	/*
		final PreparedStatement psInsertClass = cn.prepareStatement("INSERT INTO class (class_id) VALUES (?)");
		final PreparedStatement psInsertParent = cn.prepareStatement("INSERT INTO class_parent (class_id, parent_id) VALUES (?, ?)");
		final PreparedStatement psInsertChild = cn.prepareStatement("INSERT INTO class_child (class_id, child_id) VALUES (?, ?)");
		Statement st = cn.createStatement();
		ResultSet rs = st.executeQuery("SELECT item_id, value FROM claim_item WHERE property_id = 279");
		int idx = 0;
		while (rs.next()) {
			final int itemId = rs.getInt(1);
			final int parentId = rs.getInt(2);
			psInsertParent.setInt(1, itemId);
			psInsertParent.setInt(2, parentId);
			psInsertParent.addBatch();
			psInsertChild.setInt(1, parentId);
			psInsertChild.setInt(2, itemId);
			psInsertChild.addBatch();
			idx++;
			if (idx % MAX_INSERT_BATCH_COUNT == 0) {
				psInsertParent.executeBatch();
				psInsertChild.executeBatch();
			}
		}
		rs.close();
		st.close();
		psInsertClass.executeBatch();
		psInsertClass.close();
		psInsertParent.executeBatch();
		psInsertParent.close();
		psInsertChild.executeBatch();
		psInsertChild.close();
	
		final Set<Integer> classIdSet = new HashSet<>();
		st = cn.createStatement();
		rs = st.executeQuery("SELECT class_id, parent_id FROM class_parent");
		while (rs.next()) {
			final int classId = rs.getInt(1);
			final int parentId = rs.getInt(2);
			classIdSet.add(Integer.valueOf(classId));
			classIdSet.add(Integer.valueOf(parentId));
		}
		rs.close();
		st.close();
	
		final int[] classIds = Questionator.intArray(classIdSet);
		final List<DClass> classes = new ArrayList<>();
	//			final PreparedStatement psQueryLabels = cn.prepareStatement("SELECT label_en, label_de FROM item WHERE item_id = ?");
	//			final PreparedStatement psQueryParents = cn.prepareStatement("SELECT parent_id FROM class_parent WHERE class_id = ?");
	//			final PreparedStatement psQueryChildren = cn.prepareStatement("SELECT child_id FROM class_child WHERE class_id = ?");
		idx = 0;
		while (idx < classIds.length) {
	//			for (final Integer classId : classIds) {
			final List<Integer> inIds = new ArrayList<Integer>();
			for (int i = 0; i < 100 && idx < classIds.length; i++, idx++)
				inIds.add(Integer.valueOf(classIds[idx]));
			final String inIdString = inIds.toString().replace("[", "").replace("]", "");
			System.out.println(inIdString);
	//				final int cid = classId.intValue();
			final Map<Integer, Set<Integer>> parentIds = new HashMap<>();
	//				psQueryParents.setInt(1, cid);
	//				rs = psQueryParents.executeQuery();
			st = cn.createStatement();
			rs = st.executeQuery("SELECT class_id, parent_id FROM class_parent WHERE class_id = IN (" + inIdString + ")");
			while (rs.next()) {
				final Integer classId = Integer.valueOf(rs.getInt(1));
				Set<Integer> s = parentIds.get(classId);
				if (s == null) {
					s = new HashSet<Integer>();
					parentIds.put(classId, s);
				}
				s.add(Integer.valueOf(rs.getInt(1)));
			}
			rs.close();
			final Set<Integer> childIds = new HashSet<>();
			psQueryChildren.setInt(1, cid);
			rs = psQueryChildren.executeQuery();
			while (rs.next())
				childIds.add(Integer.valueOf(rs.getInt(1)));
			rs.close();
			psQueryLabels.setInt(1, cid);
			rs = psQueryLabels.executeQuery();
			rs.next();
			final String labelEn = rs.getString(1);
			final String labelDe = rs.getString(2);
			rs.close();
			classes.add(new DClass(classId.intValue(), Questionator.intArray(parentIds), Questionator.intArray(childIds), labelEn, labelDe));
			if (idx % 100 == 0)
				System.out.println(">> " + idx + " / " + classIds.size());
			idx++;
		}
		*/

/*
	private static int count(final Connection cn, final String whereClause) {
		int count = -1;
		final String sql = "SELECT COUNT(*) FROM class WHERE " + whereClause;
		try {
			final Statement st = cn.createStatement();
			final ResultSet rs = st.executeQuery(sql);
			if (rs.next())
				count = rs.getInt(1);
			rs.close();
			st.close();
		} catch (final Exception e) {
			System.out.println(sql);
			e.printStackTrace();
		}
		return count;
	}

	private static boolean insert(final Connection cn, final String columns, final String values) {
		final String sql = "INSERT INTO class (" + columns + ") VALUES (" + values + ")";
		try {
			final Statement st = cn.createStatement();
			st.execute(sql);
			st.close();
		} catch (final Exception e) {
			System.out.println(sql);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean importClassQuery(final Connection cn) {

		System.out.printf("%8d   %-40s %-40s ", Integer.valueOf(classId), labelEn, labelDe);

		if (Arrays.binarySearch(SKIP_CLASS_IDS, classId) >= 0) {
			System.out.println("CLASS IS A SKIP CLASS");
			return false;
		}
		if (count(cn, "class_id = " + classId) > 0) {
			System.out.println("class already exists");
			return true;
		}

		if (insert(cn, "class_id, label_en, label_de", classId + ",'" + labelEn.replace("'", "") + "','" + labelDe.replace("'", "") + "'"))
			System.out.println("imported");
		else
			System.out.println("NOT IMPORTED");

		return true;
	}

	// ---

	private static final DClass[] QUERY_MAIN_CLASSES = { //

			new DClass(386724, "work", "Werk"), //
			new DClass(26907166, "temporal entity", "temporäre Entität"), //
			new DClass(7257, "idiology", "Ideologie"), //
			new DClass(1792644, "art style", "Kunststil"), //

			new DClass(5, "human", "Mensch"), //
			new DClass(844569, "identity", "Identifikation"), //

			new DClass(43229, "organization", "Organisation"), //

//			new DClass(12737077, "occupation", "Beschäftigung"), //

			new DClass(16521, "taxon", "Taxon"), //
			new DClass(502895, "common name", "Trivialname"), //

			new DClass(6156156, "geochronological unit", "geochronologische Einheit"), //

			new DClass(14897293, "fictitious entity", "fiktive Entität"), //

			//new DClass(82794, "geographic region", "Region"), //
			new DClass(618123, "geographical object", "geographisches Objekt"), //

			new DClass(11173, "chemical compound", "chemische Verbindung"), //
			new DClass(11344, "chemical element", "chemisches Element"), //

			new DClass(17444171, "model", "Modell"), //

			new DClass(752870, "motor vehicle", "Kraftfahrzeug"), //

			new DClass(7194062, "flying machine", "Flugmaschine"), //

			new DClass(93301, "locomotive", "Lokomotive"), //

			new DClass(11446, "ship", "Schiff"), //
			new DClass(35872, "boat", "Boot"), //
			new DClass(2811, "submarine", "U-Boot"), //

			new DClass(2095, "food", "Lebensmittel"), //
	};

	private static final DClass[] QUERY_TERMINATOR_DOWN_CLASSES = { //

			new DClass(5, "human", "Mensch"), //

			// occupation
//			new DClass(30185, "mayor", "Bürgermeister"), //
//			new DClass(83307, "minister", "minister"), //
//			new DClass(121998, "ambassador", "Botschafter"), //
//			new DClass(15726790, "United States ambassador", "Botschafter der Vereinigten Staaten"), //

			// geographical object/region
			new DClass(123705, "neighborhood", "Stadtviertel"), //
			new DClass(5663900, "alcalde", "Alkalde"), //

			// chemical compound
			new DClass(7946, "mineral", "Mineral"), //
			new DClass(11173, "chemical compound", "chemische Verbindung"), //
			new DClass(11344, "chemical element", "chemisches Element"), //

			// motor vehicle
//			new DClass(1420, "automobile", "Automobil"), //

			// flying machine
//			new DClass(11436, "aircraft", "Luftfahrzeug"), //
//			new DClass(34486, "helicopter", "Hubschrauber"), //
//			new DClass(41426, "trainer", "Schulflugzeug"), //
//			new DClass(127771, "fighter", "Jagdflugzeug"), //
//			new DClass(170877, "bomber", "Bomber"), //
//			new DClass(180173, "glider", "Segelflugzeug"), //
//			new DClass(208187, "attack aircraft", "Erdkampfflugzeug"), //
//			new DClass(210932, "airliner", "Verkehrsflugzeug"), //
//			new DClass(216916, "military aircraft", "Militärflugzeug"), //
//			new DClass(697175, "launch vehicle", "Trägerrakete"), //
//			new DClass(1445518, "military transport aircraft", "militärisches Transportflugzeug"), //
//			new DClass(3041792, "reconnaissance aircraft", "Aufklärungsflugzeug"), //
//			new DClass(15062149, "land-based aircraft", "Landgestütztes Flugzeug"), //

			// locomotive
//			new DClass(34336, "diesel locomotive", "Diesellokomotive"), //
//			new DClass(93301, "locomotive", "Lokomotive"), //
//			new DClass(171043, "steam locomotive", "Dampflokomotive"), //
//			new DClass(625151, "electric locomotive", "Elektrolokomotive"), //
//			new DClass(785745, "tank locomotive", "Tenderlokomotive"), //
//			new DClass(19842071, "diesel-electric locomotive", "dieselelektrische Lokomotive"), //
//			new DClass(20650761, "tender locomotive", "Dampflokomotive mit Schleppentender"), //

			// vehicle engines
			new DClass(174174, "diesel engine", "Dieselmotor"), //
			new DClass(335225, "rocket engine", "Raketentriebwerk"), //
			new DClass(654051, "turbofan", "Mantelstromtriebwerk"), //
			new DClass(743004, "aircraft engine", "Luftfahrtantrieb"), //

			// food
			new DClass(178, "pasta", "Pasta"), //
			new DClass(7802, "bread", "Brot"), //
			new DClass(9053, "pudding", "Pudding"), //
			new DClass(9266, "salad", "Salat"), //
			new DClass(10943, "cheese", "Käse"), //
			new DClass(13266, "cookie", "Keks"), //
			new DClass(13276, "cake", "Kuchen"), //
			new DClass(40050, "drink", "Getränk"), //
			new DClass(41415, "soup", "Suppe"), //
			new DClass(178359, "sauce", "Sauce"), //
			new DClass(477248, "pastry", "Gebäck"), //
			new DClass(630531, "French wine", "französischer Wein"), //
			new DClass(746549, "dish", "Gericht"), //
			new DClass(182940, "dessert", "Dessert"), //
			new DClass(1125341, "Italian wine", "Weinbau in Italien"), //
			new DClass(2223649, "French cheese", "französischer Käse"), //
			new DClass(2920963, "stew", "Eintopf"), //
			new DClass(5200157, "confections", "Süßware"), //
			new DClass(13360264, "pie", "Kuchen"), //
			new DClass(17315191, "Italian cheese", "Italienischer Käse"), //
			new DClass(18679149, "fish dish", "Fischgericht"), //
			new DClass(19361017, "British cheese", "britischer Käse"), //

//			new DClass(473972, "protected area", "Schutzgebiet in Natur- und Landschaftsschutz"), //
//			new DClass(1048835, "political territorial entity", "politische Unterteilung"), //
//			new DClass(10864048, "first-level administrative country subdivision", "Verwaltungseinheit 1. Ebene"), //
//			new DClass(13220204, "second-level administrative country subdivision", "Verwaltungseinheit 2. Ebene"), //
//			new DClass(13221722, "third-level administrative country subdivision", "Verwaltungseinheit 3. Ebene"), //
//			new DClass(15916867, "administrative territorial entity of a single country", "territoriale Verwaltungseinheit eines Landes"), //

//			new DClass(34379, "musical instrument", "Musikinstrument"), //
	};

	private static final DClass[] QUERY_TERMINATOR_UP_CLASSES = { //

			new DClass(386724, "work", "Werk"), //
			new DClass(26907166, "temporal entity", "temporäre Entität"), //
			new DClass(7257, "idiology", "Ideologie"), //
			new DClass(1792644, "art style", "Kunststil"), //

			new DClass(5, "human", "Mensch"), //
			new DClass(844569, "identity", "Identifikation"), //

			new DClass(43229, "organization", "Organisation"), //

			new DClass(16521, "taxon", "Taxon"), //
			new DClass(502895, "common name", "Trivialname"), //

			new DClass(6156156, "geochronological unit", "geochronologische Einheit"), //

			new DClass(14897293, "fictitious entity", "fiktive Entität"), //

			new DClass(12737077, "occupation", "Beschäftigung"), //

			new DClass(82794, "geographic region", "Region"), //
			new DClass(618123, "geographical object", "geographisches Objekt"), //
			new DClass(2221906, "geographic location", "geographische Lage"), //

			new DClass(79529, "chemical substance", "chemischer Stoff"), //

			new DClass(17444171, "model", "Modell"), //

			new DClass(752870, "motor vehicle", "Kraftfahrzeug"), //

			new DClass(7194062, "flying machine", "Flugmaschine"), //

			new DClass(93301, "locomotive", "Lokomotive"), //

			new DClass(11446, "ship", "Schiff"), //
			new DClass(35872, "boat", "Boot"), //
			new DClass(2811, "submarine", "U-Boot"), //

			new DClass(2095, "food", "Lebensmittel"), //
			new DClass(40050, "drink", "Getränk"), //

			new DClass(42889, "vehicle", "Fahrzeug"), //
			new DClass(214339, "role", "soziale Rolle"), //
			new DClass(214609, "material", "Material"), //
			new DClass(357279, "culture", "Kultivierung"), //
			new DClass(386724, "work", "Werk"), //
			new DClass(599151, "official", "leitender Beamter"), //
			new DClass(874405, "social group", "soziale Gruppe"), //
			new DClass(2424752, "product", "Produkt"), //
			new DClass(1485500, "tangible good", "Ware"), //
			new DClass(4164871, "position", "Stellung"), //
			new DClass(8205328, "artefact", "Artefakt"), //

	};

	private static final DClass[] QUERY_META_CLASSES = { //

			new DClass(12136, "disease", "Krankheit"), //

			// motor vehicle
			new DClass(3231690, "automobile model", "Automodell"), //
			new DClass(23866334, "motorcycle model", "Motorradmodell"), //

			// flying machine
//			new DClass(1875621, "aircraft class", "Luftfahrzeugklasse"), //
			new DClass(15056993, "aircraft family", "Flugzeugfamilie"), //
			new DClass(15056995, "aircraft model", "Flugzeug-Modell"), //

			new DClass(811704, "rolling stock class", "Baureihe"), // schienenfahrzeug
			new DClass(19832486, "locomotive class", "Lokomotivbaureihe"), //

			new DClass(559026, "ship class", "Schiffsklasse"), //
			new DClass(1428357, "submarine class", "U-Boot-Klasse"), //
//			new DClass(2235308, "ship type", "Schiffstyp"), //
//			new DClass(16335899, "watercraft type", "Wasserfahrzeugtyp"), //

			new DClass(15057020, "engine family", "Triebwerksfamilie"), //
			new DClass(15057021, "engine model", "Motorenmodell"), //

			new DClass(15142894, "weapon model", "Waffenmodell"), //
			new DClass(20741022, "digital camera model", "Digitalkamera-Modell"), //
			new DClass(20888659, "camera model", "Kameramodell"), //
			new DClass(22704163, "firearm model", "Feuerwaffenmodell"), //
	};

	private static final DClass[] SKIP_CLASSES = { //

			// geographic region
//			new DClass(21674689, "admin-territorial entity of Ukraine", "admin-territorial entity of Ukraine"), //

//			new DClass(7187, "gene", "Gen"), //
//			new DClass(8054, "protein", "Protein"), //
			new DClass(11053, "RNA", "Ribonukleinsäure"), //
			new DClass(21199, "natural number", "natürliche Zahl"), // 
			new DClass(139677, "Operon", "Operon"), //
			new DClass(201448, "transfer RNA", "tRNA"), //
			new DClass(277338, "pseudogene", "Pseudogen"), //
			new DClass(284416, "small nucleolar RNA", "SnoRNA"), //
			new DClass(417841, "protein family", "Proteinfamilie"), //
			new DClass(420927, "protein complex", "Proteinkomplex"), //
			new DClass(427087, "non-coding RNA", "Nichtcodierende Ribonukleinsäure"), //
			new DClass(898273, "protein domain", "Proteindomäne"), //
//			new DClass(3270632, "national championship", "nationale Meisterschaft"), //
			new DClass(4167410, "wikimedia disambiguation page", "wikimedia disambiguation page"), //
			new DClass(4167836, "wikimedia category", "wikimedia category"), //
			new DClass(7644128, "Supersecondary structure", "Supersecondary structure"), // supersecondary structure
			new DClass(11266439, "wikimedia template", "wikimedia template"), // 
//			new DClass(13219666, "tennis tournament", "Tennisturnier"), //
//			new DClass(13357858, "badminton tournament", "Badmintonturnier"), //
			new DClass(13366104, "even number", "gerade Zahl"), // 
			new DClass(13366129, "odd number", "ungerade Zahl"), // 
			new DClass(13406463, "wikimedia list article", "wikimedia list article"), // 
//			new DClass(13442814, "scientific article", "wissenschaftlicher Artikel"), //
			new DClass(17633526, "wikinews article", "wikinews article"), // 
			new DClass(14204246, "Wikimedia project page", "Seite im Projektnamensraum"), //
//			new DClass(15061650, "golf tournament", "Golfturnier"), //
			new DClass(15184295, "Wikimedia module", "Wikimedia-Modul"), //
			new DClass(19842659, "Wikimedia user language template", "Wikimedia user language template"), //
			new DClass(20010800, "Wikimedia user language category", "Wikimedia user language category"), //
			new DClass(20747295, "protein-coding gene", "proteinkodierendes Gen"), //
//			new DClass(21167512, "chemical hazard", "chemische Gefahr"), //
//			new DClass(23636313, "Greek minister", "Greek minister"), //
//			new DClass(24702381, "mayor of Vaud", "Bürgermeister von Vaud"), //
			new DClass(24719571, "Alcohol dehydrogenase superfamily, zinc-type", "Alcohol dehydrogenase superfamily, zinc-type"), //
			new DClass(24726117, "SDR", "SDR"), // Short-chain dehydrogenase/reductase SDR
			new DClass(24726420, "ABC transporter, permease", "ABC transporter, permease"), //
			new DClass(24771218, "Transcription regulator HTH, AraC- type", "Transcription regulator HTH, AraC- type"), //
			new DClass(24774756, "Olfactory receptor", "Olfactory receptor"), //
			new DClass(24781630, "Amino acid/polyamine transporter I", "Amino acid/polyamine transporter I"), //
			new DClass(24781392, "MFS", "MFS"), // Major facilitator superfamily
			new DClass(24787504, "Bordetella uptake gene", "Bordetella uptake gene"), //
	};

//			new DClass(386724, "work", "Werk"), //
//			new DClass(9135, "operating system", "Betriebssystem"), //
//			new DClass(14659, "coat of arms", "Wappen"), //
// prototype aircraft model (Q15126161)
//		importClass(-1, 26907166, "temporal entity", "temporäre Entität");
//		importClass(-1, 7257, "idiology", "Ideologie");
//		importClass(-1, 1792644, "art style", "Kunststil");

	private static final int[] QUERY_MAIN_CLASS_IDS;
	private static final int[] QUERY_META_CLASS_IDS;
	private static final int[] QUERY_TERMINATOR_DOWN_CLASS_IDS;
	private static final int[] QUERY_TERMINATOR_UP_CLASS_IDS;
	public static final int[] SKIP_CLASS_IDS;
	static {

		// main class ids
		final int cntMain = QUERY_MAIN_CLASSES.length;
		QUERY_MAIN_CLASS_IDS = new int[cntMain];
		for (int i = 0; i < cntMain; i++)
			QUERY_MAIN_CLASS_IDS[i] = QUERY_MAIN_CLASSES[i].classId;
		Arrays.sort(QUERY_MAIN_CLASS_IDS);

		// meta class ids
		final int cntMeta = QUERY_META_CLASSES.length;
		QUERY_META_CLASS_IDS = new int[cntMeta];
		for (int i = 0; i < cntMeta; i++)
			QUERY_META_CLASS_IDS[i] = QUERY_META_CLASSES[i].classId;
		Arrays.sort(QUERY_META_CLASS_IDS);

		// terminator down class ids
		final int cntTermDown = QUERY_TERMINATOR_DOWN_CLASSES.length;
		QUERY_TERMINATOR_DOWN_CLASS_IDS = new int[cntTermDown];
		for (int i = 0; i < cntTermDown; i++)
			QUERY_TERMINATOR_DOWN_CLASS_IDS[i] = QUERY_TERMINATOR_DOWN_CLASSES[i].classId;
		Arrays.sort(QUERY_TERMINATOR_DOWN_CLASS_IDS);

		// terminator up class ids
		final int cntTermUp = QUERY_TERMINATOR_UP_CLASSES.length;
		QUERY_TERMINATOR_UP_CLASS_IDS = new int[cntTermUp];
		for (int i = 0; i < cntTermUp; i++)
			QUERY_TERMINATOR_UP_CLASS_IDS[i] = QUERY_TERMINATOR_UP_CLASSES[i].classId;
		Arrays.sort(QUERY_TERMINATOR_UP_CLASS_IDS);

		// skip class ids
		final int cntSkip = SKIP_CLASSES.length;
		SKIP_CLASS_IDS = new int[cntSkip];
		for (int i = 0; i < cntSkip; i++)
			SKIP_CLASS_IDS[i] = SKIP_CLASSES[i].classId;
		Arrays.sort(SKIP_CLASS_IDS);
	}

	// ---

	private static final String URL_QUERY_BASE = "https://query.wikidata.org/sparql?query=";
	private static final String URL_QUERY_PART1 = urlEncode("SELECT ?c ?lEn ?lDe WHERE {");
	private static final String URL_QUERY_PART2 = urlEncode(". ?c rdfs:label ?lEn. ?c rdfs:label ?lDe. FILTER(LANG(?lEn) = \"en\" && LANG(?lDe) = \"de\")");
	private static final String URL_QUERY_PART3 = urlEncode("}");

	private static String queryUrl(final String condition) {

		final StringBuilder sb = new StringBuilder();
		sb.append(URL_QUERY_BASE);
		sb.append(URL_QUERY_PART1);
		sb.append(urlEncode(condition));
		sb.append(URL_QUERY_PART2);

		// filter skip + metaclasses
		final List<Integer> filterIds = new ArrayList<>();
		for (final int id : QUERY_META_CLASS_IDS)
			filterIds.add(Integer.valueOf(id));
		for (final int id : SKIP_CLASS_IDS)
			filterIds.add(Integer.valueOf(id));
		final int cntFilter = filterIds.size();
		if (cntFilter > 0) {
			final StringBuilder sbFilter = new StringBuilder();
			sbFilter.append(". OPTIONAL{?c wdt:P31 ?i}. FILTER(IF(BOUND(?i),");
			for (int i = 0; i < cntFilter; i++) {
				sbFilter.append("?i!=wd:Q" + filterIds.get(i));
				if (i < cntFilter - 1)
					sbFilter.append("&&");
			}
			sbFilter.append(",true))");
			sb.append(urlEncode(sbFilter.toString()));
		}

		sb.append(URL_QUERY_PART3);
		return sb.toString();
	}

	private static String queryUrlDown(final int classId) {
		return queryUrl("?c wdt:P279 wd:Q" + classId);
	}

	private static String queryUrlUp(final int classId) {
		return queryUrl("wd:Q" + classId + " wdt:P279 ?c");
	}

	public static final String urlEncode(final String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final String QUERY_RESULT_SUBCLASS_MARKER_ID = "<uri>http://www.wikidata.org/entity/Q";
	private static final String QUERY_RESULT_SUBCLASS_MARKER_LABEL_EN = "<literal xml:lang='en'>";
	private static final String QUERY_RESULT_SUBCLASS_MARKER_LABEL_DE = "<literal xml:lang='de'>";

	private static String httpGet(final String url) {
		try {
			final InputStream is = new URL(url).openStream();
			final InputStreamReader isr = new InputStreamReader(is, "UTF-8");//"US-ASCII");
			final BufferedReader br = new BufferedReader(isr);
			final StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line.replaceAll("\\s+", " ").trim() + '\n');
			br.close();
			is.close();
			return sb.toString();
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static DClass[] query(final int classId, final String url) {
		//System.out.println(url);
		String response = httpGet(url);
		if (response == null) {
			System.out.println("no response for class " + classId + " with url " + url);
			System.out.println("wait a few seconds...");
			try {
				Thread.sleep(5000);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			response = httpGet(url);
			if (response == null) {
				System.out.println("no response for class " + classId + " with url " + url);
				return null;
			}
		}

		final List<DClass> list = new ArrayList<>();
		final StringReader sr = new StringReader(response);
		final BufferedReader br = new BufferedReader(sr);
		String line;
		Integer subId = null;
		String subLabelEn = null;
		String subLabelDe = null;
		try {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				try {
					if (line.startsWith("</result>")) {
						if (subLabelDe == null)
							subLabelDe = subLabelEn;
						if (subLabelEn == null)
							subLabelEn = subLabelDe;
						if (subId != null && subLabelEn != null && subLabelDe != null)
							list.add(new DClass(subId.intValue(), subLabelEn, subLabelDe));
						else
							System.out.println(">>> cannot add " + subId + ":" + subLabelEn + ":" + subLabelDe);
						subId = null;
						subLabelEn = null;
						subLabelDe = null;
					} else if (line.startsWith(QUERY_RESULT_SUBCLASS_MARKER_ID)) {
						final String fixed = line.substring(QUERY_RESULT_SUBCLASS_MARKER_ID.length());
						subId = Integer.valueOf(validString(fixed.replaceAll("<.*", "")));
					} else if (line.startsWith(QUERY_RESULT_SUBCLASS_MARKER_LABEL_EN)) {
						final String fixed = line.substring(QUERY_RESULT_SUBCLASS_MARKER_LABEL_EN.length());
						subLabelEn = validString(fixed.replaceAll("<.*", ""));
					} else if (line.startsWith(QUERY_RESULT_SUBCLASS_MARKER_LABEL_DE)) {
						final String fixed = line.substring(QUERY_RESULT_SUBCLASS_MARKER_LABEL_DE.length());
						subLabelDe = validString(fixed.replaceAll("<.*", ""));
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			br.close();
			sr.close();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}

		return list.toArray(new DClass[list.size()]);
	}

	private DClass[] queryDown() {
		return query(classId, queryUrlDown(classId));
	}

	private DClass[] queryUp() {
		return query(classId, queryUrlUp(classId));
	}

	private boolean importClassesQueryDirection(final Connection cn, final boolean isDown) {

		if (!importClassQuery(cn))
			return false;

		final DClass[] classesQuery = isDown ? queryDown() : queryUp();
		if (classesQuery == null)
			return false;
		if (classesQuery.length == 0)
			return true;

		final boolean isTerminatorClass = Arrays.binarySearch(isDown ? QUERY_TERMINATOR_DOWN_CLASS_IDS : QUERY_TERMINATOR_UP_CLASS_IDS, classId) >= 0;

		final List<DClass> classesValid = new ArrayList<>();
		for (final DClass classQuery : classesQuery) {
			if (!isTerminatorClass || Arrays.binarySearch(isDown ? QUERY_TERMINATOR_DOWN_CLASS_IDS : QUERY_TERMINATOR_UP_CLASS_IDS, classQuery.classId) >= 0)
				classesValid.add(classQuery);
		}
		if (classesValid.isEmpty()) {
			System.out.printf("%8d   %-40s %-40s terminate\n", Integer.valueOf(classId), labelEn, labelDe);
			return true;
		}

		final List<DClass> classesNotExist = new ArrayList<>();
		for (final DClass classValid : classesValid) {
			if (count(cn, "class_id = " + classValid.classId) == 0)
				classesNotExist.add(classValid);
		}
		if (classesNotExist.isEmpty()) {
			System.out.printf("%8d   %-40s %-40s all classes already imported\n", Integer.valueOf(classId), labelEn, labelDe);
			return true;
		}

		boolean success = true;
		int cntImport = 0;
		for (final DClass classNotExist : classesNotExist) {
			final boolean b = classNotExist.importClassesQueryDirection(cn, isDown);
			if (b)
				cntImport++;
			success &= b;
		}
		if (cntImport > 0)
			System.out.printf("%8d   %-40s %-40s %d classes imported\n", Integer.valueOf(classId), labelEn, labelDe, Integer.valueOf(cntImport));

		return success;
	}

	public static boolean importClassesQuery(final Connection cn, final boolean reimportAll) {

		if (!DClass.recreateTable(cn, reimportAll))
			return false;

		try {

			// main classes
			for (final DClass rc : QUERY_MAIN_CLASSES) {
				rc.importClassesQueryDirection(cn, true);
				rc.importClassesQueryDirection(cn, false);
			}

			// terminator down classes
			for (final DClass tdc : QUERY_TERMINATOR_DOWN_CLASSES)
				tdc.importClassesQueryDirection(cn, false);

			// meta classes
			for (final DClass mc : QUERY_META_CLASSES)
				mc.importClassQuery(cn);

		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
*/
}
