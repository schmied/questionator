package org.schmied.questionator;

import java.sql.*;
import java.util.Arrays;

public class DProperty extends DEntity {

	public final int propertyId;
	public final String labelEn, labelDe;

	public DProperty(final int propertyId, final String labelEn, final String labelDe) {
		this.propertyId = propertyId;
		this.labelEn = labelEn;
		this.labelDe = labelDe;
	}

	@Override
	public String toString() {
		return "p" + propertyId + ":" + labelEn + ":" + labelDe;
	}

	// ---------------------------------------------------------------------------------------------------------------- import

	public static boolean importProperties(final Connection cn) {

		if (!DProperty.recreateTable(cn))
			return false;

		for (final DProperty property : VALID_PROPERTIES)
			property.sqlInsert(cn);

		return true;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static int propertyId(final String propertyKey) {
		final int propertyId = Integer.valueOf(propertyKey.substring(1)).intValue();
		final boolean isValidPropertyId = Arrays.binarySearch(VALID_PROPERTY_IDS, propertyId) >= 0;
		if (!isValidPropertyId)
			return -1;
		return propertyId;
	}

	// ---------------------------------------------------------------------------------------------------------------- sql

	private static final String SQL_INSERT = "INSERT INTO property (property_id, label_en, label_de) VALUES (?, ?, ?)";

	private static boolean recreateTable(final Connection cn) {
		try (final Statement st = cn.createStatement()) {
			st.execute("DROP TABLE IF EXISTS property");
			st.execute("CREATE TABLE IF NOT EXISTS property (property_id INT4 PRIMARY KEY, label_en character varying(" + MAX_STRING_LENGTH
					+ "), label_de character varying(" + MAX_STRING_LENGTH + "))");
//			st.execute("CREATE INDEX idx_property_label_en ON property USING btree (label_en)");
//			st.execute("CREATE INDEX idx_property_label_de ON property USING btree (label_de)");

//			st.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean sqlInsert(final Connection cn) {
		try (final PreparedStatement ps = cn.prepareStatement(SQL_INSERT)) {
			ps.setInt(1, propertyId);
			ps.setString(2, labelEn);
			ps.setString(3, labelDe);
			ps.execute();
//			ps.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// ---------------------------------------------------------------------------------------------------------------- property definition

	private static final DProperty[] VALID_PROPERTIES = { //

			new DProperty(6, "head of government", "Leiter der Regierung oder Verwaltung"), //
			new DProperty(17, "country", "Staat"), //
			new DProperty(18, "image", "Bild"), //
			new DProperty(19, "place of birth", "Geburtsort"), // 
			new DProperty(20, "place of death", "Sterbeort"), //
			new DProperty(21, "sex or gender", "Geschlecht"), //
			new DProperty(22, "father", "Vater"), //
			new DProperty(25, "mother", "Mutter"), //
			new DProperty(26, "spouse", "Ehepartner"), //
			//new DProperty(27, "country of citizenship", "Land der Staatsangehörigkeit"), //
			new DProperty(30, "continent", "Kontinent"), //
			new DProperty(31, "instance of", "ist ein(e)"), //
			new DProperty(35, "head of state", "Staatsoberhaupt"), //
			new DProperty(36, "capital", "Hauptstadt"), //
			new DProperty(37, "official language", "Amtssprache"), //
			new DProperty(38, "currency", "Währung"), //
			new DProperty(39, "position held", "öffentliches Amt oder Stellung"), //
			new DProperty(40, "child", "Kinder"), //
			new DProperty(41, "flag image", "Flagge"), //
			new DProperty(47, "shares border with", "gemeinsame Grenze mit"), //
			new DProperty(50, "author", "Autor"), //
			new DProperty(53, "family", "Familie"), //
			new DProperty(54, "member of sports team", "Mitglied von Sportteam oder -verein"), //
			new DProperty(57, "director", "Regisseur"), //
			new DProperty(58, "screenwriter", "Drehbuchautor"), //
			new DProperty(61, "discoverer or inventor", "Entdecker oder Erfinder"), //
			new DProperty(66, "ancestral home", "Herkunftsort"), //
			new DProperty(78, "top-level internet domain", "Top-Level-Domain"), //
			new DProperty(84, "architect", "Architekt"), //
			new DProperty(85, "anthem", "Hymne"), //
			new DProperty(86, "composer", "Komponist"), //
			new DProperty(87, "librettist", "Librettist"), //
			new DProperty(88, "commissioned by", "Auftraggeber"), //
			new DProperty(91, "sexual orientation", "sexuelle Orientierung"), //
			new DProperty(94, "coat of arms image", "Wappen"), //
			new DProperty(97, "noble title", "Adelstitel"), //
			new DProperty(98, "editor", "Herausgeber"), //
			new DProperty(101, "field of work", "Arbeitsgebiet"), //
			new DProperty(102, "member of political party", "Parteizugehörigkeit"), //
			new DProperty(103, "native language", "Muttersprache"), //
			new DProperty(105, "taxon rank", "taxonomischer Rang"), //
			new DProperty(106, "occupation", "Tätigkeit"), //
			new DProperty(108, "employer", "beschäftigt bei"), //
			new DProperty(110, "illustrator", "Illustrator"), //
			new DProperty(111, "measured physical quantity", "gemessene physikalische Größe"), //
			new DProperty(112, "founded by", "Gründer"), //
			new DProperty(114, "airline alliance", "Luftfahrtallianz"), //
			new DProperty(115, "home venue", "Stadion"), //
			new DProperty(119, "place of interment", "Begräbnisort"), //
			new DProperty(122, "basic form of government", "Regierungsform"), //
			new DProperty(123, "publisher", "Verlag"), //
			new DProperty(126, "maintained by", "Betreuer"), //
			new DProperty(127, "owned by", "Eigentümer"), //
			new DProperty(131, "located in the administrative territorial entity", "liegt in der Verwaltungseinheit"), //
			new DProperty(135, "movement", "Bewegung"), //
			new DProperty(136, "genre", "Genre"), //
			new DProperty(137, "operator", "Betreiber"), //
			new DProperty(138, "named after", "benannt nach"), //
			new DProperty(140, "religion", "Religionszugehörigkeit"), //
			new DProperty(144, "based on", "Vorlage"), //
			new DProperty(149, "architectural style", "Architekturstil"), //
			new DProperty(154, "logo image", "Logo"), //
			new DProperty(155, "follows", "Vorgänger"), //
			new DProperty(156, "followed by", "Nachfolger"), //
			new DProperty(157, "killed by", "getötet durch"), //
			new DProperty(158, "seal image", "Siegel"), //
			new DProperty(159, "headquarters location", "Hauptverwaltung"), //
			new DProperty(161, "cast member", "Darsteller"), //
			new DProperty(162, "producer", "Produzent"), //
			new DProperty(166, "award received", "Auszeichnung"), //
			new DProperty(169, "chief executive officer", "Geschäftsführer"), //
			new DProperty(170, "creator", "Urheber"), //
			new DProperty(171, "parent taxon", "übergeordnetes Taxon"), //
			new DProperty(175, "performer", "Interpret"), //
			new DProperty(176, "manufacturer", "Hersteller"), //
			new DProperty(178, "developer", "Entwickler"), //
			new DProperty(179, "series", "Serie"), //
			new DProperty(180, "depicts", "Motiv"), //
			new DProperty(181, "taxon range map image", "Verbreitungskarte des Taxons"), //
			new DProperty(183, "endemic to", "endemisch in"), //
			new DProperty(186, "material used", "Material"), //
			//new DProperty(190, "sister city", "Partnerkommune"), //
			new DProperty(194, "legislative body", "gesetzgebende Körperschaft"), //
			new DProperty(195, "collection", "Sammlung"), //
			new DProperty(200, "lake inflows", "Zufluss"), //
			new DProperty(201, "lake outflow", "Abfluss"), //
			new DProperty(205, "basin country", "Anrainerstaat"), //
			new DProperty(206, "located next to body of water", "liegt am Gewässer"), //
			new DProperty(225, "taxon name", "wissenschaftlicher Name"), //
			new DProperty(241, "military branch", "Teilstreitkraft"), //
			new DProperty(246, "element symbol", "Elementsymbol"), //
			new DProperty(249, "ticker symbol", "Tickersymbol"), //
			new DProperty(263, "official residence", "Amtssitz"), //
			new DProperty(264, "record label", "Musiklabel"), //
			new DProperty(272, "production company", "Produktionsgesellschaft"), //
			new DProperty(274, "chemical formula", "chemische Formel"), //
			new DProperty(276, "location", "Ort"), //
			new DProperty(277, "programming language", "Programmiersprache"), //
			new DProperty(279, "subclass of", "Unterklasse von"), //
			new DProperty(286, "head coach", "Cheftrainer"), //
			new DProperty(287, "designed by", "Designer"), //
			new DProperty(344, "director of photography", "Kameramann"), //
			new DProperty(361, "part of", "ist Teil von"), //
			new DProperty(371, "presenter", "Moderator"), //
			new DProperty(375, "space launch vehicle", "Trägerrakete"), //
			new DProperty(397, "parent astronomical body", "übergeordneter astronomischer Körper"), //
			new DProperty(400, "platform", "Plattform"), //
			new DProperty(403, "mouth of the watercourse", "mündet in"), //
			new DProperty(408, "software engine", "Engine"), //
			new DProperty(410, "military rank", "militärischer Dienstgrad"), //
			new DProperty(413, "position played on team / speciality", "Spielerposition / Spezialität"), //
			new DProperty(417, "patron saint", "Schutzpatron"), //
			new DProperty(449, "original network", "Sender der Erstausstrahlung"), //
			new DProperty(451, "partner", "Lebenspartner"), //
			new DProperty(452, "industry", "Branche"), //
			new DProperty(453, "character role", "Rolle"), //
			new DProperty(457, "foundational text", "Gründungsvertrag"), //
			new DProperty(460, "said to be the same as", "eventuell gleichwertig"), //
			new DProperty(462, "color", "Farbe"), //
			new DProperty(463, "member of", "Mitglied von"), //
			new DProperty(466, "occupant", "Bewohner / Nutzer"), //
			new DProperty(474, "country calling code", "internationale Telefonvorwahl"), //
			new DProperty(488, "leader", "Vorsitzender"), //
			new DProperty(495, "country of origin", "Ursprungsland"), //
			new DProperty(505, "general manager", "Generaldirektor"), //
			new DProperty(509, "cause of death", "Todesursache"), //
			new DProperty(516, "powerplant", "Antrieb"), //
			new DProperty(520, "armament", "Bewaffnung"), //
			new DProperty(523, "temporal range start", "zeitliches Auftreten – Anfang"), //
			new DProperty(524, "temporal range end", "zeitliches Auftreten – Ende"), //
			new DProperty(527, "has part", "besteht aus"), // ////////////////?????????????????
			new DProperty(534, "streak color", "Strichfarbe"), // 
			new DProperty(569, "date of birth", "Geburtsdatum"), //
			new DProperty(570, "date of death", "Sterbedatum"), //
			new DProperty(571, "inception", "Gründungs-/Erstellungsdatum"), //
			new DProperty(575, "time of discovery", "Entdeckungsdatum"), //
			new DProperty(576, "dissolved or abolished", "Auflösungsdatum"), //
			new DProperty(577, "publication date", "Veröffentlichungsdatum"), //
			new DProperty(580, "start time", "Startzeitpunkt"), //
			new DProperty(582, "end time", "Endzeitpunkt"), //
			new DProperty(585, "point in time", "Zeitpunkt"), //
			new DProperty(598, "commander of", "befohlene Einheiten"), //
			new DProperty(606, "first flight", "Zeitpunkt des ersten Flugs"), //
			new DProperty(607, "conflict", "Kriegseinsatz"), //
			new DProperty(610, "highest point", "höchster Punkt"), //
			new DProperty(611, "religious order", "Ordensgemeinschaft"), //
			new DProperty(619, "time of spacecraft launch", "Start"), //
			new DProperty(625, "coordinate location", "geographische Koordinaten"), //
			//new DProperty(628, "E number", "E-Nummer"), // is external ID
			new DProperty(641, "sport", "Sportart"), //
			new DProperty(647, "drafted by", "gedraftet durch"), //
			new DProperty(706, "located on terrain feature", "liegt geografisch in Gebiet oder Gewässer"), //
			new DProperty(708, "diocese", "Diözese"), //
			new DProperty(710, "participant", "Teilnehmer"), //
			new DProperty(725, "voice actor", "Sprecher"), //
			new DProperty(729, "service entry", "Zeitpunkt der Inbetriebnahme"), //
			new DProperty(730, "service retirement", "Zeitpunkt der Außerbetriebnahme"), //
			new DProperty(737, "influenced by", "beeinflusst von"), //
			new DProperty(739, "ammunition", "Patrone"), //
			new DProperty(740, "location of formation", "Gründungsort"), //
			//new DProperty(742, "pseudonym", "Pseudonym"), //
			new DProperty(749, "parent organization", "Dachgesellschaft"), //
			new DProperty(750, "distributor", "Distributor"), //
			new DProperty(770, "cause of destruction", "Grund der Zerstörung"), //
			new DProperty(780, "symptoms", "Symptome"), //
			new DProperty(793, "significant event", "Schlüsselereignis"), //
			new DProperty(800, "notable work", "Werke"), //
			new DProperty(825, "dedicated to", "Widmung an"), //
			new DProperty(840, "narrative location", "Handlungsort"), //
			new DProperty(841, "feast day", "Gedenktag"), //
			new DProperty(859, "sponsor", "Sponsor"), //
			new DProperty(885, "origin of the watercourse", "Quelle des Wasserlaufs"), //
			new DProperty(915, "filming location", "Drehort"), //
			new DProperty(921, "main subject", "Schlagwort"), //
			new DProperty(927, "anatomical location", "anatomische Lage"), //
			new DProperty(937, "work location", "Wirkungsort"), //
			new DProperty(941, "inspired by", "inspiriert von"), //
			new DProperty(945, "allegiance", "Treuepflicht"), //
			new DProperty(974, "tributary", "Nebenfluss"), //
			new DProperty(1028, "donated by", "gestiftet von"), //
			new DProperty(1037, "manager / director", "Leiter"), //
			new DProperty(1040, "film editor", "Filmeditor"), //
			new DProperty(1050, "medical condition", "Erkrankung"), //
			new DProperty(1056, "product or material produced", "Produkt"), //
			new DProperty(1064, "track gauge", "Spurweite"), //
			new DProperty(1066, "student of", "Schüler von"), //
			new DProperty(1071, "location of final assembly", "Herstellungsort"), //
			new DProperty(1080, "from fictional universe", "aus fiktivem Universum"), //
			new DProperty(1082, "population", "Einwohnerzahl"), //
			new DProperty(1086, "atomic number", "Ordnungszahl"), //
			new DProperty(1092, "total produced", "Produktionsmenge"), //
			new DProperty(1098, "number of speakers", "Sprecher (Anzahl)"), //
			new DProperty(1101, "floors above ground", "Stockwerke"), //
			new DProperty(1110, "attendance", "Besucherzahl"), //
			new DProperty(1113, "number of episodes", "Anzahl der Episoden"), //
			new DProperty(1114, "quantity", "Anzahl"), //
			new DProperty(1120, "number of deaths", "Anzahl der Todesfälle"), //
			new DProperty(1142, "political ideology", "politische Ideologie"), //
			new DProperty(1181, "numeric value", "numerischer Wert"), //
			new DProperty(1196, "manner of death", "Todesart"), //
			new DProperty(1268, "represents", "repräsentiert"), //
			new DProperty(1290, "godparent", "Pate"), //
			new DProperty(1303, "instrument", "Instrument"), //
			new DProperty(1308, "officeholder", "Amtsinhaber"), //
			new DProperty(1340, "eye color", "Augenfarbe"), //
			new DProperty(1344, "participant of", "Teilnehmer an"), //
			new DProperty(1346, "winner", "Sieger"), //
			new DProperty(1365, "replaces", "ersetzt"), //
			new DProperty(1366, "replaced by", "ersetzt durch"), //
			new DProperty(1376, "capital of", "Hauptstadt von"), //
			new DProperty(1387, "political alignment", "politische Ausrichtung"), //
			new DProperty(1399, "convicted of", "verurteilt wegen"), //
			new DProperty(1412, "languages spoken, written or signed", "gesprochene oder publizierte Sprachen"), //
			new DProperty(1416, "affiliation", "Zugehörigkeit zu"), //
			new DProperty(1427, "start point", "geografischer Startpunkt"), //
			new DProperty(1431, "executive producer", "Produktionsleiter"), //
			new DProperty(1441, "present in work", "kommt vor in"), //
			new DProperty(1444, "destination point", "geografischer Endpunkt"), //
			new DProperty(1449, "nickname", "Spitzname"), //
			new DProperty(1532, "country for sport", "Land (Sport)"), //
			new DProperty(1640, "curator", "Kurator"), //
			new DProperty(1830, "owner of", "Inhaber von"), //
			new DProperty(1875, "represented by", "repräsentiert von"), //
			new DProperty(1877, "after a work by", "nach einem Werk von"), //
			new DProperty(1962, "patron", "Mäzen"), //
			new DProperty(1995, "health specialty", "medizinisches Fachgebiet"), //
			new DProperty(2012, "cuisine", "Küche"), //
			new DProperty(2043, "length", "Länge"), //
			new DProperty(2044, "elevation above sea level", "Höhe über dem Meeresspiegel"), //
			new DProperty(2046, "area", "Fläche"), //
			new DProperty(2047, "duration", "Dauer"), //
			new DProperty(2048, "height", "Höhe"), //
			new DProperty(2049, "width", "Breite"), //
			new DProperty(2052, "speed", "Geschwindigkeit"), //
			new DProperty(2067, "mass", "Masse"), //
			new DProperty(2101, "melting point", "Schmelzpunkt"), //
			new DProperty(2102, "boiling point", "Siedepunkt"), //
			new DProperty(2120, "radius", "Radius"), //
			new DProperty(2121, "prize money", "Preisgeld"), //
			new DProperty(2124, "member count", "Mitgliederzahl"), //
			new DProperty(2175, "medical condition treated", "zur Behandlung von benutzt"), //
			new DProperty(2176, "drug used for treatment", "behandelt mit"), //
			new DProperty(2218, "net worth estimate", "Vermögen (Schätzung)"), //
			new DProperty(2234, "volume as quantity", "Volumen"), //
			new DProperty(2239, "first aid measures", "Erste-Hilfe-Maßnahmen"), //
			new DProperty(2257, "frequency of event", "Austragungsperiodendauer einer Veranstaltung"), //
			new DProperty(2386, "diameter", "Durchmesser"), //
			new DProperty(2416, "sports discipline competed in", "Sportdisziplin"), //
			new DProperty(2554, "production designer", "Szenenbildner"), //
			new DProperty(2596, "culture", "Kultur"), //
			new DProperty(2632, "place of detention", "Haftort"), //
			new DProperty(2894, "day of week", "Wochentag"), //
			new DProperty(2922, "month of the year", "Monat des Jahres"), //
			new DProperty(3075, "official religion", "offizielle Religion"), //
			new DProperty(3342, "significant person", "relevante Person"), //
			new DProperty(3373, "sibling", "Geschwister"), //
			new DProperty(3716, "social classification", "gesetzlicher sozialer Status"), //
			new DProperty(3780, "active ingredient in", "Wirkstoff in"), //
			new DProperty(3781, "has active ingredient", "has active ingredient"), //
			new DProperty(4000, "has fruit type", "Fruchttyp"), //
			new DProperty(4552, "mountain range", "Gebirgszug"), //
			new DProperty(4614, "watershed", "Flusssystem"), //
			new DProperty(4647, "location of first performance", "Ort der Uraufführung oder Erstausstrahlung"), //
	};

	public static final int[] VALID_PROPERTY_IDS;
	static {

		VALID_PROPERTY_IDS = new int[VALID_PROPERTIES.length];
		for (int i = 0; i < VALID_PROPERTIES.length; i++)
			VALID_PROPERTY_IDS[i] = VALID_PROPERTIES[i].propertyId;
		Arrays.sort(VALID_PROPERTY_IDS);
	}
}
