package org.schmied.questionator.importer.entity;

import java.util.Arrays;

public class PropertyEntity extends ImportEntity {

	public final int propertyId;
	public final String labelEn, labelDe;

	public PropertyEntity(final int propertyId, final String labelEn, final String labelDe) {
		this.propertyId = propertyId;
		this.labelEn = labelEn;
		this.labelDe = labelDe;
	}

	@Override
	public String toString() {
		return "p" + propertyId + ":" + labelEn + ":" + labelDe;
	}

	// ---------------------------------------------------------------------------------------------------------------- json

	public static int propertyId(final String propertyKey) {
		final int propertyId = Integer.valueOf(propertyKey.substring(1)).intValue();
		final boolean isValiPropertyEntityId = Arrays.binarySearch(VALID_PROPERTY_IDS, propertyId) >= 0;
		if (!isValiPropertyEntityId)
			return -1;
		return propertyId;
	}

	// ---------------------------------------------------------------------------------------------------------------- property definition

	public static final PropertyEntity[] VALID_PROPERTIES = { //

			new PropertyEntity(6, "head of government", "Leiter der Regierung oder Verwaltung"), //
			new PropertyEntity(17, "country", "Staat"), //
			new PropertyEntity(18, "image", "Bild"), //
			new PropertyEntity(19, "place of birth", "Geburtsort"), // 
			new PropertyEntity(20, "place of death", "Sterbeort"), //
			new PropertyEntity(21, "sex or gender", "Geschlecht"), //
			new PropertyEntity(22, "father", "Vater"), //
			new PropertyEntity(25, "mother", "Mutter"), //
			new PropertyEntity(26, "spouse", "Ehepartner"), //
			//new PropertyEntity(27, "country of citizenship", "Land der Staatsangehörigkeit"), //
			new PropertyEntity(30, "continent", "Kontinent"), //
			new PropertyEntity(31, "instance of", "ist ein(e)"), //
			new PropertyEntity(35, "head of state", "Staatsoberhaupt"), //
			new PropertyEntity(36, "capital", "Hauptstadt"), //
			new PropertyEntity(37, "official language", "Amtssprache"), //
			new PropertyEntity(38, "currency", "Währung"), //
			new PropertyEntity(39, "position held", "öffentliches Amt oder Stellung"), //
			new PropertyEntity(40, "child", "Kinder"), //
			new PropertyEntity(41, "flag image", "Flagge"), //
			new PropertyEntity(47, "shares border with", "gemeinsame Grenze mit"), //
			new PropertyEntity(50, "author", "Autor"), //
			new PropertyEntity(53, "family", "Familie"), //
			new PropertyEntity(54, "member of sports team", "Mitglied von Sportteam oder -verein"), //
			new PropertyEntity(57, "director", "Regisseur"), //
			new PropertyEntity(58, "screenwriter", "Drehbuchautor"), //
			new PropertyEntity(61, "discoverer or inventor", "Entdecker oder Erfinder"), //
			new PropertyEntity(66, "ancestral home", "Herkunftsort"), //
			new PropertyEntity(78, "top-level internet domain", "Top-Level-Domain"), //
			new PropertyEntity(84, "architect", "Architekt"), //
			new PropertyEntity(85, "anthem", "Hymne"), //
			new PropertyEntity(86, "composer", "Komponist"), //
			new PropertyEntity(87, "librettist", "Librettist"), //
			new PropertyEntity(88, "commissioned by", "Auftraggeber"), //
			new PropertyEntity(91, "sexual orientation", "sexuelle Orientierung"), //
			new PropertyEntity(94, "coat of arms image", "Wappen"), //
			new PropertyEntity(97, "noble title", "Adelstitel"), //
			new PropertyEntity(98, "editor", "Herausgeber"), //
			new PropertyEntity(101, "field of work", "Arbeitsgebiet"), //
			new PropertyEntity(102, "member of political party", "Parteizugehörigkeit"), //
			new PropertyEntity(103, "native language", "Muttersprache"), //
			new PropertyEntity(105, "taxon rank", "taxonomischer Rang"), //
			new PropertyEntity(106, "occupation", "Tätigkeit"), //
			new PropertyEntity(108, "employer", "beschäftigt bei"), //
			new PropertyEntity(110, "illustrator", "Illustrator"), //
			new PropertyEntity(111, "measured physical quantity", "gemessene physikalische Größe"), //
			new PropertyEntity(112, "founded by", "Gründer"), //
			new PropertyEntity(114, "airline alliance", "Luftfahrtallianz"), //
			new PropertyEntity(115, "home venue", "Stadion"), //
			new PropertyEntity(119, "place of interment", "Begräbnisort"), //
			new PropertyEntity(122, "basic form of government", "Regierungsform"), //
			new PropertyEntity(123, "publisher", "Verlag"), //
			new PropertyEntity(126, "maintained by", "Betreuer"), //
			new PropertyEntity(127, "owned by", "Eigentümer"), //
			new PropertyEntity(131, "located in the administrative territorial entity", "liegt in der Verwaltungseinheit"), //
			new PropertyEntity(135, "movement", "Bewegung"), //
			new PropertyEntity(136, "genre", "Genre"), //
			new PropertyEntity(137, "operator", "Betreiber"), //
			new PropertyEntity(138, "named after", "benannt nach"), //
			new PropertyEntity(140, "religion", "Religionszugehörigkeit"), //
			new PropertyEntity(144, "based on", "Vorlage"), //
			new PropertyEntity(149, "architectural style", "Architekturstil"), //
			new PropertyEntity(154, "logo image", "Logo"), //
			new PropertyEntity(155, "follows", "Vorgänger"), //
			new PropertyEntity(156, "followed by", "Nachfolger"), //
			new PropertyEntity(157, "killed by", "getötet durch"), //
			new PropertyEntity(158, "seal image", "Siegel"), //
			new PropertyEntity(159, "headquarters location", "Hauptverwaltung"), //
			new PropertyEntity(161, "cast member", "Darsteller"), //
			new PropertyEntity(162, "producer", "Produzent"), //
			new PropertyEntity(166, "award received", "Auszeichnung"), //
			new PropertyEntity(169, "chief executive officer", "Geschäftsführer"), //
			new PropertyEntity(170, "creator", "Urheber"), //
			new PropertyEntity(171, "parent taxon", "übergeordnetes Taxon"), //
			new PropertyEntity(175, "performer", "Interpret"), //
			new PropertyEntity(176, "manufacturer", "Hersteller"), //
			new PropertyEntity(178, "developer", "Entwickler"), //
			new PropertyEntity(179, "series", "Serie"), //
			new PropertyEntity(180, "depicts", "Motiv"), //
			new PropertyEntity(181, "taxon range map image", "Verbreitungskarte des Taxons"), //
			new PropertyEntity(183, "endemic to", "endemisch in"), //
			new PropertyEntity(186, "material used", "Material"), //
			//new PropertyEntity(190, "sister city", "Partnerkommune"), //
			new PropertyEntity(194, "legislative body", "gesetzgebende Körperschaft"), //
			new PropertyEntity(195, "collection", "Sammlung"), //
			new PropertyEntity(200, "lake inflows", "Zufluss"), //
			new PropertyEntity(201, "lake outflow", "Abfluss"), //
			new PropertyEntity(205, "basin country", "Anrainerstaat"), //
			new PropertyEntity(206, "located next to body of water", "liegt am Gewässer"), //
			new PropertyEntity(225, "taxon name", "wissenschaftlicher Name"), //
			new PropertyEntity(241, "military branch", "Teilstreitkraft"), //
			new PropertyEntity(246, "element symbol", "Elementsymbol"), //
			new PropertyEntity(249, "ticker symbol", "Tickersymbol"), //
			new PropertyEntity(263, "official residence", "Amtssitz"), //
			new PropertyEntity(264, "record label", "Musiklabel"), //
			new PropertyEntity(272, "production company", "Produktionsgesellschaft"), //
			new PropertyEntity(274, "chemical formula", "chemische Formel"), //
			new PropertyEntity(276, "location", "Ort"), //
			new PropertyEntity(277, "programming language", "Programmiersprache"), //
			new PropertyEntity(279, "subclass of", "Unterklasse von"), //
			new PropertyEntity(286, "head coach", "Cheftrainer"), //
			new PropertyEntity(287, "designed by", "Designer"), //
			new PropertyEntity(344, "director of photography", "Kameramann"), //
			new PropertyEntity(361, "part of", "ist Teil von"), //
			new PropertyEntity(371, "presenter", "Moderator"), //
			new PropertyEntity(375, "space launch vehicle", "Trägerrakete"), //
			new PropertyEntity(397, "parent astronomical body", "übergeordneter astronomischer Körper"), //
			new PropertyEntity(400, "platform", "Plattform"), //
			new PropertyEntity(403, "mouth of the watercourse", "mündet in"), //
			new PropertyEntity(408, "software engine", "Engine"), //
			new PropertyEntity(410, "military rank", "militärischer Dienstgrad"), //
			new PropertyEntity(413, "position played on team / speciality", "Spielerposition / Spezialität"), //
			new PropertyEntity(417, "patron saint", "Schutzpatron"), //
			new PropertyEntity(449, "original network", "Sender der Erstausstrahlung"), //
			new PropertyEntity(451, "partner", "Lebenspartner"), //
			new PropertyEntity(452, "industry", "Branche"), //
			new PropertyEntity(453, "character role", "Rolle"), //
			new PropertyEntity(457, "foundational text", "Gründungsvertrag"), //
			new PropertyEntity(460, "said to be the same as", "eventuell gleichwertig"), //
			new PropertyEntity(462, "color", "Farbe"), //
			new PropertyEntity(463, "member of", "Mitglied von"), //
			new PropertyEntity(466, "occupant", "Bewohner / Nutzer"), //
			new PropertyEntity(474, "country calling code", "internationale Telefonvorwahl"), //
			new PropertyEntity(488, "leader", "Vorsitzender"), //
			new PropertyEntity(495, "country of origin", "Ursprungsland"), //
			new PropertyEntity(505, "general manager", "Generaldirektor"), //
			new PropertyEntity(509, "cause of death", "Todesursache"), //
			new PropertyEntity(516, "powerplant", "Antrieb"), //
			new PropertyEntity(520, "armament", "Bewaffnung"), //
			new PropertyEntity(523, "temporal range start", "zeitliches Auftreten – Anfang"), //
			new PropertyEntity(524, "temporal range end", "zeitliches Auftreten – Ende"), //
			new PropertyEntity(527, "has part", "besteht aus"), // ////////////////?????????????????
			new PropertyEntity(534, "streak color", "Strichfarbe"), // 
			new PropertyEntity(569, "date of birth", "Geburtsdatum"), //
			new PropertyEntity(570, "date of death", "Sterbedatum"), //
			new PropertyEntity(571, "inception", "Gründungs-/Erstellungsdatum"), //
			new PropertyEntity(575, "time of discovery", "Entdeckungsdatum"), //
			new PropertyEntity(576, "dissolved or abolished", "Auflösungsdatum"), //
			new PropertyEntity(577, "publication date", "Veröffentlichungsdatum"), //
			new PropertyEntity(580, "start time", "Startzeitpunkt"), //
			new PropertyEntity(582, "end time", "Endzeitpunkt"), //
			new PropertyEntity(585, "point in time", "Zeitpunkt"), //
			new PropertyEntity(598, "commander of", "befohlene Einheiten"), //
			new PropertyEntity(606, "first flight", "Zeitpunkt des ersten Flugs"), //
			new PropertyEntity(607, "conflict", "Kriegseinsatz"), //
			new PropertyEntity(610, "highest point", "höchster Punkt"), //
			new PropertyEntity(611, "religious order", "Ordensgemeinschaft"), //
			new PropertyEntity(619, "time of spacecraft launch", "Start"), //
			new PropertyEntity(625, "coordinate location", "geographische Koordinaten"), //
			//new PropertyEntity(628, "E number", "E-Nummer"), // is external ID
			new PropertyEntity(641, "sport", "Sportart"), //
			new PropertyEntity(647, "drafted by", "gedraftet durch"), //
			new PropertyEntity(706, "located on terrain feature", "liegt geografisch in Gebiet oder Gewässer"), //
			new PropertyEntity(708, "diocese", "Diözese"), //
			new PropertyEntity(710, "participant", "Teilnehmer"), //
			new PropertyEntity(725, "voice actor", "Sprecher"), //
			new PropertyEntity(729, "service entry", "Zeitpunkt der Inbetriebnahme"), //
			new PropertyEntity(730, "service retirement", "Zeitpunkt der Außerbetriebnahme"), //
			new PropertyEntity(737, "influenced by", "beeinflusst von"), //
			new PropertyEntity(739, "ammunition", "Patrone"), //
			new PropertyEntity(740, "location of formation", "Gründungsort"), //
			//new PropertyEntity(742, "pseudonym", "Pseudonym"), //
			new PropertyEntity(749, "parent organization", "Dachgesellschaft"), //
			new PropertyEntity(750, "distributor", "Distributor"), //
			new PropertyEntity(770, "cause of destruction", "Grund der Zerstörung"), //
			new PropertyEntity(780, "symptoms", "Symptome"), //
			new PropertyEntity(793, "significant event", "Schlüsselereignis"), //
			new PropertyEntity(800, "notable work", "Werke"), //
			new PropertyEntity(825, "dedicated to", "Widmung an"), //
			new PropertyEntity(840, "narrative location", "Handlungsort"), //
			new PropertyEntity(841, "feast day", "Gedenktag"), //
			new PropertyEntity(859, "sponsor", "Sponsor"), //
			new PropertyEntity(885, "origin of the watercourse", "Quelle des Wasserlaufs"), //
			new PropertyEntity(915, "filming location", "Drehort"), //
			new PropertyEntity(921, "main subject", "Schlagwort"), //
			new PropertyEntity(927, "anatomical location", "anatomische Lage"), //
			new PropertyEntity(937, "work location", "Wirkungsort"), //
			new PropertyEntity(941, "inspired by", "inspiriert von"), //
			new PropertyEntity(945, "allegiance", "Treuepflicht"), //
			new PropertyEntity(974, "tributary", "Nebenfluss"), //
			new PropertyEntity(1028, "donated by", "gestiftet von"), //
			new PropertyEntity(1037, "manager / director", "Leiter"), //
			new PropertyEntity(1040, "film editor", "Filmeditor"), //
			new PropertyEntity(1050, "medical condition", "Erkrankung"), //
			new PropertyEntity(1056, "product or material produced", "Produkt"), //
			new PropertyEntity(1064, "track gauge", "Spurweite"), //
			new PropertyEntity(1066, "student of", "Schüler von"), //
			new PropertyEntity(1071, "location of final assembly", "Herstellungsort"), //
			new PropertyEntity(1080, "from fictional universe", "aus fiktivem Universum"), //
			new PropertyEntity(1082, "population", "Einwohnerzahl"), //
			new PropertyEntity(1086, "atomic number", "Ordnungszahl"), //
			new PropertyEntity(1092, "total produced", "Produktionsmenge"), //
			new PropertyEntity(1098, "number of speakers", "Sprecher (Anzahl)"), //
			new PropertyEntity(1101, "floors above ground", "Stockwerke"), //
			new PropertyEntity(1110, "attendance", "Besucherzahl"), //
			new PropertyEntity(1113, "number of episodes", "Anzahl der Episoden"), //
			new PropertyEntity(1114, "quantity", "Anzahl"), //
			new PropertyEntity(1120, "number of deaths", "Anzahl der Todesfälle"), //
			new PropertyEntity(1142, "political ideology", "politische Ideologie"), //
			new PropertyEntity(1181, "numeric value", "numerischer Wert"), //
			new PropertyEntity(1196, "manner of death", "Todesart"), //
			new PropertyEntity(1268, "represents", "repräsentiert"), //
			new PropertyEntity(1290, "godparent", "Pate"), //
			new PropertyEntity(1303, "instrument", "Instrument"), //
			new PropertyEntity(1308, "officeholder", "Amtsinhaber"), //
			new PropertyEntity(1340, "eye color", "Augenfarbe"), //
			new PropertyEntity(1344, "participant of", "Teilnehmer an"), //
			new PropertyEntity(1346, "winner", "Sieger"), //
			new PropertyEntity(1365, "replaces", "ersetzt"), //
			new PropertyEntity(1366, "replaced by", "ersetzt durch"), //
			new PropertyEntity(1376, "capital of", "Hauptstadt von"), //
			new PropertyEntity(1387, "political alignment", "politische Ausrichtung"), //
			new PropertyEntity(1399, "convicted of", "verurteilt wegen"), //
			new PropertyEntity(1412, "languages spoken, written or signed", "gesprochene oder publizierte Sprachen"), //
			new PropertyEntity(1416, "affiliation", "Zugehörigkeit zu"), //
			new PropertyEntity(1427, "start point", "geografischer Startpunkt"), //
			new PropertyEntity(1431, "executive producer", "Produktionsleiter"), //
			new PropertyEntity(1441, "present in work", "kommt vor in"), //
			new PropertyEntity(1444, "destination point", "geografischer Endpunkt"), //
			new PropertyEntity(1449, "nickname", "Spitzname"), //
			new PropertyEntity(1532, "country for sport", "Land (Sport)"), //
			new PropertyEntity(1640, "curator", "Kurator"), //
			new PropertyEntity(1830, "owner of", "Inhaber von"), //
			new PropertyEntity(1875, "represented by", "repräsentiert von"), //
			new PropertyEntity(1877, "after a work by", "nach einem Werk von"), //
			new PropertyEntity(1962, "patron", "Mäzen"), //
			new PropertyEntity(1995, "health specialty", "medizinisches Fachgebiet"), //
			new PropertyEntity(2012, "cuisine", "Küche"), //
			new PropertyEntity(2043, "length", "Länge"), //
			new PropertyEntity(2044, "elevation above sea level", "Höhe über dem Meeresspiegel"), //
			new PropertyEntity(2046, "area", "Fläche"), //
			new PropertyEntity(2047, "duration", "Dauer"), //
			new PropertyEntity(2048, "height", "Höhe"), //
			new PropertyEntity(2049, "width", "Breite"), //
			new PropertyEntity(2052, "speed", "Geschwindigkeit"), //
			new PropertyEntity(2067, "mass", "Masse"), //
			new PropertyEntity(2101, "melting point", "Schmelzpunkt"), //
			new PropertyEntity(2102, "boiling point", "Siedepunkt"), //
			new PropertyEntity(2120, "radius", "Radius"), //
			new PropertyEntity(2121, "prize money", "Preisgeld"), //
			new PropertyEntity(2124, "member count", "Mitgliederzahl"), //
			new PropertyEntity(2175, "medical condition treated", "zur Behandlung von benutzt"), //
			new PropertyEntity(2176, "drug used for treatment", "behandelt mit"), //
			new PropertyEntity(2218, "net worth estimate", "Vermögen (Schätzung)"), //
			new PropertyEntity(2234, "volume as quantity", "Volumen"), //
			new PropertyEntity(2239, "first aid measures", "Erste-Hilfe-Maßnahmen"), //
			new PropertyEntity(2257, "frequency of event", "Austragungsperiodendauer einer Veranstaltung"), //
			new PropertyEntity(2386, "diameter", "Durchmesser"), //
			new PropertyEntity(2416, "sports discipline competed in", "Sportdisziplin"), //
			new PropertyEntity(2554, "production designer", "Szenenbildner"), //
			new PropertyEntity(2596, "culture", "Kultur"), //
			new PropertyEntity(2632, "place of detention", "Haftort"), //
			new PropertyEntity(2894, "day of week", "Wochentag"), //
			new PropertyEntity(2922, "month of the year", "Monat des Jahres"), //
			new PropertyEntity(3075, "official religion", "offizielle Religion"), //
			new PropertyEntity(3342, "significant person", "relevante Person"), //
			new PropertyEntity(3373, "sibling", "Geschwister"), //
			new PropertyEntity(3716, "social classification", "gesetzlicher sozialer Status"), //
			new PropertyEntity(3780, "active ingredient in", "Wirkstoff in"), //
			new PropertyEntity(3781, "has active ingredient", "has active ingredient"), //
			new PropertyEntity(4000, "has fruit type", "Fruchttyp"), //
			new PropertyEntity(4552, "mountain range", "Gebirgszug"), //
			new PropertyEntity(4614, "watershed", "Flusssystem"), //
			new PropertyEntity(4647, "location of first performance", "Ort der Uraufführung oder Erstausstrahlung"), //
	};

	public static final int[] VALID_PROPERTY_IDS;
	static {

		VALID_PROPERTY_IDS = new int[VALID_PROPERTIES.length];
		for (int i = 0; i < VALID_PROPERTIES.length; i++)
			VALID_PROPERTY_IDS[i] = VALID_PROPERTIES[i].propertyId;
		Arrays.sort(VALID_PROPERTY_IDS);
	}
}
