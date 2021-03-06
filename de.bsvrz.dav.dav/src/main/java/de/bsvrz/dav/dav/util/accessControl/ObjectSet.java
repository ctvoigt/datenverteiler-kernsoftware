/*
 * Copyright 2011 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.dav.
 * 
 * de.bsvrz.dav.dav is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.dav is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dav.dav; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.dav.util.accessControl;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.*;

import java.util.*;

/**
 * Kapselt einen Block zur Auswahl von Objekten, z.B. "Enthaltene Objekte" bzw. "Ausgeschlossene Objekte" im Zugriffsrechte-Datenmodell.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 9202 $
 */
public final class ObjectSet implements ObjectCollection {

	/** Enth�lt Objekte die die einzelnen Auswahl-Bl�cke repr�sentieren */
	private final Collection<ObjectSelectionBlock> _selectionBlocks = new ArrayList<ObjectSelectionBlock>();

	private final ObjectCollectionParent _region;

	private final boolean _isOnlyTypeSelection;

	private final ClientDavInterface _connection;


	private ObjectSet(final ObjectCollectionParent region, final ClientDavInterface connection, final boolean isOnlyTypeSelection) {
		_isOnlyTypeSelection = isOnlyTypeSelection;
		_region = region;
		_connection = connection;
	}

	/**
	 * Erstellt einen neuen Block
	 *
	 * @param region              Region bzw. Elternobjekt
	 * @param connection          Verbindung zur Konfiguration
	 * @param array               Daten-Array
	 * @param isOnlyTypeSelection Falls Mengen und einzelne Objektangaben ignoriert werden sollen
	 */
	public ObjectSet(
			final ObjectCollectionParent region, final ClientDavInterface connection, final Data.Array array, final boolean isOnlyTypeSelection) {
		this(region, connection, isOnlyTypeSelection);

		// Die Arrays EnthalteneObjekte und AusgeschlosseneObjekte sind �berfl�ssig. deshalb werden die einzelnen Auswahl-Strukturen hier zusammengef�gt.
		for(int i = 0; i < array.getLength(); i++) {
			final Data item = array.getItem(i);
			createAreaBlocks(item.getArray("Bereich"));
			createRegionBlocks(item.getArray("Region"));
			if(!_isOnlyTypeSelection) createObjectsBlocks(item.getArray("Objekte"));
		}
	}

	/**
	 * Erstellt einen neuen Block
	 *
	 * @param region              Region bzw. Elternobjekt
	 * @param connection          Verbindung zur Konfiguration
	 * @param item                Datum
	 * @param isOnlyTypeSelection Falls Mengen und einzelne Objektangaben ignoriert werden sollen
	 */
	public ObjectSet(
			final ObjectCollectionParent region, final ClientDavInterface connection, final Data item, final boolean isOnlyTypeSelection) {
		this(region, connection, isOnlyTypeSelection);

		createAreaBlocks(item.getArray("Bereich"));
		createRegionBlocks(item.getArray("Region"));
		if(!_isOnlyTypeSelection) createObjectsBlocks(item.getArray("Objekte"));
	}

	/**
	 * Erstellt einen neuen Block
	 *
	 * @param region              Region bzw. Elternobjekt
	 * @param connection          Verbindung zur Konfiguration
	 * @param data                Daten-Array mit Bl�cken
	 * @param isOnlyTypeSelection Falls Mengen und einzelne Objektangaben ignoriert werden sollen
	 */
	public ObjectSet(
			final ObjectCollectionParent region, final ClientDavInterface connection, final Iterable<Data> data, final boolean isOnlyTypeSelection) {
		this(region, connection, isOnlyTypeSelection);

		for(final Data item : data) {
			createAreaBlocks(item.getArray("Bereich"));
			createRegionBlocks(item.getArray("Region"));
			if(!_isOnlyTypeSelection) createObjectsBlocks(item.getArray("Objekte"));
		}
	}

	/**
	 * Verarbeitet die einzelnen AuswahlBereich-Bl�cke und kapselt diese in Klassen
	 *
	 * @param array Datenarray "AuswahlBereich"
	 */
	private void createAreaBlocks(final Data.Array array) {
		for(int i = 0; i < array.getLength(); i++) {
			final Data item = array.getItem(i);
			final String objectSet = _isOnlyTypeSelection ? null : item.getTextValue("Mengenbezeichnung").getText();
			if(Region.isStringNullOrBlank(objectSet)) {
				if(item.getArray("Konfigurationsverantwortlicher").getLength() == 0
				   && item.getArray("Konfigurationsbereich").getLength() == 0) {
					// Filtern nur nach Typ
					_selectionBlocks.add(new ObjectSelectionBlockTypeSimple(item, true));
				}
				else {
					// Filtern nach Konfigurationsbereich/Verantwortlicher und evtl. nach Typ
					_selectionBlocks.add(new ObjectSelectionBlockAreaSimple(item));
				}
			}
			else {
				if(item.getArray("Konfigurationsverantwortlicher").getLength() == 0
				   && item.getArray("Konfigurationsbereich").getLength() == 0) {
					_selectionBlocks.add(new ObjectSelectionBlockObjectSet(objectSet, new ObjectSelectionBlockTypeSimple(item, true)));
				}
				else {
					_selectionBlocks.add(new ObjectSelectionBlockObjectSet(objectSet, new ObjectSelectionBlockAreaSimple(item)));
				}
			}
		}
	}

	/**
	 * Verarbeitet die einzelnen AuswahlRegion-Bl�cke und kapselt diese in Klassen
	 *
	 * @param array Datenarray "AuswahlRegion"
	 */
	private void createRegionBlocks(final Data.Array array) {
		for(int i = 0; i < array.getLength(); i++) {
			final Data item = array.getItem(i);
			final String objectSet = _isOnlyTypeSelection ? null : item.getTextValue("Mengenbezeichnung").getText();
			if(Region.isStringNullOrBlank(objectSet)) {
				if(item.getArray("Region").getLength() == 0) {
					// Filtern nur nach Typ.
					_selectionBlocks.add(new ObjectSelectionBlockTypeSimple(item, true));
				}
				else {
					// Filtern nur nach Region und evtl. nach Typ
					_selectionBlocks.add(new ObjectSelectionBlockRegionSimple(item));
				}
			}
			else {
				if(item.getArray("Region").getLength() == 0) {
					_selectionBlocks.add(new ObjectSelectionBlockObjectSet(objectSet, new ObjectSelectionBlockTypeSimple(item, true)));
				}
				else {
					_selectionBlocks.add(new ObjectSelectionBlockObjectSet(objectSet, new ObjectSelectionBlockRegionSimple(item)));
				}
			}
		}
	}

	/**
	 * Verarbeitet die einzelnen AuswahlObjekte-Bl�cke und kapselt diese in Klassen
	 *
	 * @param array Datenarray "AuswahlObjekte"
	 */
	private void createObjectsBlocks(final Data.Array array) {
		for(int i = 0; i < array.getLength(); i++) {
			final Data item = array.getItem(i);
			final String objectSet = item.getTextValue("Mengenbezeichnung").getText();
			if(item.getArray("Objekt").getLength() > 0) {
				if(Region.isStringNullOrBlank(objectSet)) {
					_selectionBlocks.add(new ObjectSelectionBlockObjectsSimple(item));
				}
				else {
					_selectionBlocks.add(new ObjectSelectionBlockObjectSet(objectSet, new ObjectSelectionBlockObjectsSimple(item)));
				}
			}
			else {
				if(Region.isStringNullOrBlank(objectSet)) {
					_selectionBlocks.add(new ObjectSelectionBlockTypeSimple(item, false));
				}
				else {
					_selectionBlocks.add(new ObjectSelectionBlockObjectSet(objectSet, new ObjectSelectionBlockTypeSimple(item, false)));
				}
			}
		}
	}

	@Override
	public String toString() {
		return _selectionBlocks.toString();
	}

	/**
	 * Pr�ft ob ein spezielles Systemobjekt in dieser Auswahl enthalten ist.
	 *
	 * @param object Objekt zu pr�fen
	 *
	 * @return true wenn es enthalten ist
	 */
	public boolean contains(final SystemObject object) {
		for(final ObjectSelectionBlock objectSelectionBlockArea : _selectionBlocks) {
			if(objectSelectionBlockArea.contains(object)) return true;
		}
		return false;
	}

	/**
	 * Alle Objekte, die durch diesen Block ausgew�hlt werden
	 *
	 * @param types Systemobjekttypen die beachtet werden sollen
	 *
	 * @return Alle Objekte, die durch diesen Block ausgew�hlt werden
	 */
	public List<SystemObject> getAllObjects(final Collection<? extends SystemObjectType> types) {
		final List<SystemObject> list = new ArrayList<SystemObject>();
		for(final ObjectSelectionBlock objectSelectionBlock : _selectionBlocks) {
			list.addAll(objectSelectionBlock.getAllObjects(types));
		}
		return list;
	}

	/**
	 * Gibt alle referenzierten Unter-Regionen zur�ck
	 *
	 * @return alle referenzierten Unter-Regionen
	 */
	public Collection<Region> getRegions() {
		final Collection<Region> list = new ArrayList<Region>();
		for(final ObjectSelectionBlock objectSubSet : _selectionBlocks) {
			if(objectSubSet instanceof ObjectSelectionBlockRegion) {
				final ObjectSelectionBlockRegion region = (ObjectSelectionBlockRegion)objectSubSet;
				list.addAll(region.getRegions());
			}
		}
		return list;
	}

	/**
	 * F�gt einen Listener auf �nderungen hinzu
	 *
	 * @param listener Listener auf �nderungen
	 */
	public void addChangeListener(final ObjectCollectionChangeListener listener) {
		for(final ObjectSelectionBlock objectSelectionBlockArea : _selectionBlocks) {
			objectSelectionBlockArea.addChangeListener(listener);
		}
	}

	/**
	 * Entfernt einen Listener auf �nderungen
	 *
	 * @param listener Listener auf �nderungen
	 */
	public void removeChangeListener(final ObjectCollectionChangeListener listener) {
		for(final ObjectSelectionBlock objectSelectionBlockArea : _selectionBlocks) {
			objectSelectionBlockArea.removeChangeListener(listener);
		}
	}

	/**
	 * Stellt den Block "AuswahlBereich" dar, aber nur, wenn mindestens ein KB oder KV ausgew�hlt ist und keine Menge angegeben ist. Kann sich zur Laufzeit �ndern,
	 * da neue dynamische Objekte erstellt werden k�nnten.
	 */
	private final class ObjectSelectionBlockAreaSimple extends AbstractObjectSelectionBlock {

		private final Collection<ConfigurationAuthority> _configurationAuthorities = new HashSet<ConfigurationAuthority>();

		private final Collection<ConfigurationArea> _configurationAreas = new HashSet<ConfigurationArea>();

		private final Collection<SystemObjectType> _types = new HashSet<SystemObjectType>();

		private final MutableCollectionChangeListener _changeListener = new MutableCollectionChangeListener() {
			public void collectionChanged(
					final MutableCollection mutableCollection,
					final short simulationVariant,
					final List<SystemObject> addedElements,
					final List<SystemObject> removedElements) {
				notifyBlockChanged();
			}
		};

		/**
		 * Erstellt einen Auswahlbereich-Block
		 * @param item Data-Objekt
		 * @throws IllegalArgumentException falls ein Data-Objekt mit Textwerten benutzt wurde und kein g�ltiges Objekt enthalten war
		 */
		public ObjectSelectionBlockAreaSimple(final Data item) {
			if(item.getItem("Konfigurationsverantwortlicher").getAttributeType() instanceof ReferenceAttributeType){
				final Data.ReferenceArray authorities = item.getReferenceArray("Konfigurationsverantwortlicher");
				for(int i = 0; i < authorities.getLength(); i++) {
					_configurationAuthorities.add((ConfigurationAuthority)authorities.getReferenceValue(i).getSystemObject());
				}
				final Data.ReferenceArray areas = item.getReferenceArray("Konfigurationsbereich");
				for(int i = 0; i < areas.getLength(); i++) {
					_configurationAreas.add((ConfigurationArea)areas.getReferenceValue(i).getSystemObject());
				}
			}
			else{
				final Data.TextArray authorities = item.getTextArray("Konfigurationsverantwortlicher");
				for(int i = 0; i < authorities.getLength(); i++) {
					final SystemObject object = _connection.getDataModel().getObject(authorities.getText(i));
					if(object == null || !(object instanceof ConfigurationAuthority)){
						throw new IllegalArgumentException("Es wurde ein ung�ltiger Konfigurationsverantwortlicher angegeben: " + authorities.getText(i));
					}
					_configurationAuthorities.add((ConfigurationAuthority)object);
				}
				final Data.TextArray areas = item.getTextArray("Konfigurationsbereich");
				for(int i = 0; i < areas.getLength(); i++) {
					final SystemObject object = _connection.getDataModel().getObject(areas.getText(i));
					if(object == null || !(object instanceof ConfigurationArea)){
						throw new IllegalArgumentException("Es wurde ein ung�ltiger Konfigurationsbereich angegeben: " + areas.getText(i));
					}
					_configurationAreas.add((ConfigurationArea)object);
				}
			}
			final Data.ReferenceArray types = item.getReferenceArray("Typ");
			for(int i = 0; i < types.getLength(); i++) {
				_types.add((SystemObjectType)types.getReferenceValue(i).getSystemObject());
			}
		}

		@Override
		public String toString() {
			return "ObjectSelectionBlockAreaSimple{" + "_types=" + _types + ", _configurationAuthorities=" + _configurationAuthorities
			       + ", _configurationAreas=" + _configurationAreas + '}';
		}

		public boolean contains(final SystemObject object) {
			return matchesConfigurationArea(object) && matchesType(object);
		}

		private boolean matchesConfigurationArea(final SystemObject object) {
			if(_configurationAreas.size() == 0 && _configurationAuthorities.size() == 0) return true;
			final ConfigurationArea objectArea = object.getConfigurationArea();
			for(final ConfigurationArea configurationArea : _configurationAreas) {
				if(objectArea.equals(configurationArea)) return true;
			}
			final ConfigurationAuthority objectAuthority = objectArea.getConfigurationAuthority();
			for(final ConfigurationAuthority configurationAuthority : _configurationAuthorities) {
				if(objectAuthority.equals(configurationAuthority)) return true;
			}
			return false;
		}

		private boolean matchesType(final SystemObject object) {
			if(_types.size() == 0) return true;
			for(final SystemObjectType type : _types) {
				if(object.isOfType(type)) return true;
			}
			return false;
		}

		public Collection<SystemObjectType> getAllObjectTypes() {
			if(_types.size() > 0) return Collections.unmodifiableCollection(_types);
			return _connection.getDataModel().getBaseTypes();
		}

		public Collection<SystemObject> getAllObjects(final Collection<? extends SystemObjectType> types) {
			final Collection<ConfigurationArea> configurationAreas = new ArrayList<ConfigurationArea>();
			configurationAreas.addAll(_configurationAreas);
			if(_configurationAuthorities.size() > 0) {
				final SystemObjectType configurationAreaType = _connection.getDataModel().getType("typ.konfigurationsBereich");
				for(final SystemObject configurationArea : configurationAreaType.getObjects()) {
					if(configurationArea instanceof ConfigurationArea) {
						final ConfigurationArea area = (ConfigurationArea)configurationArea;
						if(_configurationAuthorities.contains(area.getConfigurationAuthority())) {
							configurationAreas.add(area);
						}
					}
				}
			}
			return _connection.getDataModel().getObjects(
					configurationAreas,
					_types.size() == 0 ? new ArrayList<SystemObjectType>(types) : Region.mergeTypes(types, _types),
					ObjectTimeSpecification.valid()
			);
		}

		@Override
		void startChangeListener() {
			for(final DynamicObjectType dynamicObjectType : getDynamicTypes()) {
				dynamicObjectType.addChangeListener((short)0, _changeListener);
			}
		}

		Collection<DynamicObjectType> getDynamicTypes() {
			final Collection<SystemObjectType> types;
			final Collection<DynamicObjectType> result = new ArrayList<DynamicObjectType>();
			if(_types.size() > 0) {
				types = _types;
			}
			else {
				types = _connection.getDataModel().getBaseTypes();
			}
			for(final SystemObjectType type : types) {
				if(type instanceof DynamicObjectType) {
					final DynamicObjectType dynamicObjectType = (DynamicObjectType)type;
					result.add(dynamicObjectType);
				}
			}
			return result;
		}

		@Override
		void stopChangeListener() {
			for(final DynamicObjectType dynamicObjectType : getDynamicTypes()) {
				dynamicObjectType.removeChangeListener((short)0, _changeListener);
			}
		}
	}

	/**
	 * Kapselt einen "AuswahlRegion" oder einen "AuswahlBereich"-Block mit leerer Mengenangabe, bei dem das Region[]-Array bzw. die KV[] und KB[]-Arrays leer sind,
	 * das also nur zum Filtern nach Typ benutzt wird. Wird zudem f�r einen AuswahlObjekt-Block benutzt, wenn keine Objektliste angegeben wurde, also alle
	 * Systemobjekte ausgew�hlt sind. Ist zur Laufzeit �nderbar, wenn die verwalteten Typen dynamisch sind bzw. kein Typ angegeben wurde.
	 */
	private final class ObjectSelectionBlockTypeSimple extends AbstractObjectSelectionBlock {

		private Collection<SystemObjectType> _types;

		private final MutableCollectionChangeListener _changeListener = new MutableCollectionChangeListener() {
			public void collectionChanged(
					final MutableCollection mutableCollection,
					final short simulationVariant,
					final List<SystemObject> addedElements,
					final List<SystemObject> removedElements) {
				notifyBlockChanged();
			}
		};

		/**
		 * Erstellt einen neuen Block, der nach Typ ausw�hlt (oder alle Objekte ausw�hlt, wenn kein Typ angegeben ist)
		 *
		 * @param item        Daten-Objekt
		 * @param dataHasType Ob das Daten-Objekt ein Referenz-Array namens "Typ" hat. Wenn ja wird nach diesem der typ ausgew�hlt, wenn nein werden alle
		 *                    Systemobjekte ausgew�hlt
		 */
		public ObjectSelectionBlockTypeSimple(final Data item, final boolean dataHasType) {
			_types = new ArrayList<SystemObjectType>();
			if(dataHasType) {
				final Data.ReferenceArray types = item.getReferenceArray("Typ");
				for(int i = 0; i < types.getLength(); i++) {
					_types.add((SystemObjectType)types.getReferenceValue(i).getSystemObject());
				}
			}
			if(_types.size() == 0) {
				_types = _connection.getDataModel().getBaseTypes();
			}
		}

		@Override
		public String toString() {
			return "ObjectSelectionBlockTypeSimple{" + "_types=" + _types + '}';
		}

		public boolean contains(final SystemObject object) {
			return matchesType(object);
		}

		private boolean matchesType(final SystemObject object) {
			if(_types.size() == 0) return true;
			for(final SystemObjectType type : _types) {
				if(object.isOfType(type)) return true;
			}
			return false;
		}

		public Collection<SystemObjectType> getAllObjectTypes() {
			return Collections.unmodifiableCollection(_types);
		}

		public Collection<SystemObject> getAllObjects(final Collection<? extends SystemObjectType> types) {
			final Collection<SystemObject> list = new ArrayList<SystemObject>();
			for(final SystemObjectType systemObjectType : Region.mergeTypes(types, _types)) {
				list.addAll(systemObjectType.getObjects());
			}
			return list;
		}

		@Override
		public void startChangeListener() {
			for(final SystemObjectType type : _types) {
				if(type instanceof DynamicObjectType) {
					final MutableCollection dynamicObjectType = (DynamicObjectType)type;
					dynamicObjectType.addChangeListener((short)0, _changeListener);
				}
			}
		}

		@Override
		public void stopChangeListener() {
			for(final SystemObjectType type : _types) {
				if(type instanceof DynamicObjectType) {
					final MutableCollection dynamicObjectType = (DynamicObjectType)type;
					dynamicObjectType.removeChangeListener((short)0, _changeListener);
				}
			}
		}
	}

	/**
	 * Kapselt einen AuswahlRegion-Block mit Region-Angabe(n) ohne Mengenangabe. Kann zur Laufzeit ver�ndert werden, da sich die enthaltenen Regionen ver�ndern
	 * k�nnen.
	 */
	private final class ObjectSelectionBlockRegionSimple extends AbstractObjectSelectionBlock implements ObjectSelectionBlockRegion {

		private final Collection<Region> _regions;

		private final Collection<SystemObjectType> _types;

		private final RegionChangeListener _innerRegionChangeListener = new RegionChangeListener() {
			public void regionChanged(final Region region) {
				notifyBlockChanged();
			}
		};

		/**
		 * Erstellt einen AuswahlRegion-Block
		 * @param item Data-Objekt
		 * @throws IllegalArgumentException falls ein Data-Objekt mit Textwerten benutzt wurde und kein g�ltiges Objekt enthalten war
		 */
		public ObjectSelectionBlockRegionSimple(final Data item) {
			_regions = new ArrayList<Region>();
			_types = new ArrayList<SystemObjectType>();
			if(item.getItem("Region").getAttributeType() instanceof ReferenceAttributeType){
				final Data.ReferenceArray areas = item.getReferenceArray("Region");
				for(int i = 0; i < areas.getLength(); i++) {
					_regions.add(_region.getRegion(areas.getReferenceValue(i).getSystemObject()));
				}
			}
			else{
				final Data.TextArray areas = item.getTextArray("Region");
				for(int i = 0; i < areas.getLength(); i++) {
					final SystemObject object = _connection.getDataModel().getObject(areas.getText(i));
					if(object == null) throw new IllegalArgumentException("Es wurde eine ung�ltige Region angegeben: " + areas.getText(i));
					_regions.add(_region.getRegion(object));
				}
			}
			final Data.ReferenceArray types = item.getReferenceArray("Typ");
			for(int i = 0; i < types.getLength(); i++) {
				_types.add((SystemObjectType)types.getReferenceValue(i).getSystemObject());
			}
		}

		@Override
		public String toString() {
			return "ObjectSelectionBlockRegionSimple{" + "_types=" + _types + ", _regions=" + _regions + '}';
		}

		public boolean contains(final SystemObject object) {
			return matchesRegion(object) && matchesType(object);
		}

		private boolean matchesRegion(final SystemObject object) {
			if(_regions.size() == 0) return true;
			for(final Region region : _regions) {
				if(!_region.isDisabled(region)) {
					if(region.contains(object)) return true;
				}
			}
			return false;
		}

		private boolean matchesType(final SystemObject object) {
			if(_types.size() == 0) return true;
			for(final SystemObjectType type : _types) {
				if(object.isOfType(type)) return true;
			}
			return false;
		}

		public Collection<SystemObjectType> getAllObjectTypes() {
			if(_types.size() == 0) return _connection.getDataModel().getBaseTypes();
			return Collections.unmodifiableCollection(_types);
		}

		public Collection<SystemObject> getAllObjects(final Collection<? extends SystemObjectType> types) {
			final Collection<SystemObject> list = new ArrayList<SystemObject>();
			for(final Region region : _regions) {
				if(!_region.isDisabled(region)) {
					final Collection<SystemObject> systemObjectList = region.getAllObjects(types);
					for(final SystemObject systemObject : systemObjectList) {
						if(matchesType(systemObject)) list.add(systemObject);
					}
				}
			}
			return list;
		}

		public Collection<Region> getRegions() {
			return Collections.unmodifiableCollection(_regions);
		}

		@Override
		void startChangeListener() {
			for(final Region region : _regions) {
				region.addRegionChangeListener(_innerRegionChangeListener);
			}
		}

		@Override
		void stopChangeListener() {
			for(final Region region : _regions) {
				// N�chste Pr�fung ist wichtig um zyklische Rekursionen zu vermeiden
				if(!_region.isDisabled(region)) {
					region.removeRegionChangeListener(_innerRegionChangeListener);
				}
			}
		}
	}

	/** Kapselt einen AuswahlObjekte-Block ohne festgelegte Menge mit festgelegter Objektliste */
	private final class ObjectSelectionBlockObjectsSimple extends AbstractObjectSelectionBlock {

		private final Collection<SystemObject> _objects = new HashSet<SystemObject>();

		public ObjectSelectionBlockObjectsSimple(final Data item) {
			final Data.ReferenceArray objects = item.getReferenceArray("Objekt");
			for(int i = 0; i < objects.getLength(); i++) {
				_objects.add(objects.getReferenceValue(i).getSystemObject());
			}
		}

		@Override
		public String toString() {
			return "ObjectSelectionBlockObjectsSimple{" + "_objects=" + _objects + '}';
		}

		public boolean contains(final SystemObject object) {
			return matchesObjectList(object);
		}

		private boolean matchesObjectList(final SystemObject object) {
			return _objects.contains(object);
		}

		public Collection<SystemObjectType> getAllObjectTypes() {
			return _connection.getDataModel().getBaseTypes();
		}

		public Collection<SystemObject> getAllObjects(final Collection<? extends SystemObjectType> types) {
			return Collections.unmodifiableCollection(_objects);
		}
	}

	/** Kapselt einen Block mit festgelegter Menge. Enth�lt intern den Block ohne festgelegte Menge und ruft die Mengen von dessen Objektliste ab. */
	private final class ObjectSelectionBlockObjectSet extends AbstractObjectSelectionBlock implements ObjectSelectionBlockRegion {

		/** Hier werden die Objekte gespeichert, die in dieser Region enthalten sind (die also in der angegebenen Menge von den angegebenen Objekten vorkommen) */
		private final Collection<SystemObject> _objectCache = new HashSet<SystemObject>();

		/**
		 * Hier werden zu Optimierungsgr�nden die Typen gespeichert, die �berhaupt Mengen mit diesem Namen unterst�tzen. Objekte von anderen Typen m�ssen nicht
		 * beachtet werden.
		 */
		private Collection<ConfigurationObjectType> _relevantTypes = new HashSet<ConfigurationObjectType>();

		/** Hier werden die gefundenen Mengen zwischengespeichert um sich auf �nderungen anzumelden */
		private final Collection<MutableSet> _mutableSets = new HashSet<MutableSet>();

		private final ObjectCollectionChangeListener _innerChangeListener = new ObjectCollectionChangeListener() {
			public void blockChanged() {
				refreshObjectCache();
				notifyBlockChanged();
			}
		};

		private final MutableSetChangeListener _mutableSetChangeListener = new MutableSetChangeListener() {
			public void update(final MutableSet set, final SystemObject[] addedObjects, final SystemObject[] removedObjects) {
				refreshObjectCache();
				notifyBlockChanged();
			}
		};

		private final String _objectSetName;

		private final ObjectSelectionBlock _innerBlock;

		private boolean isInitialized = false;

		public ObjectSelectionBlockObjectSet(final String objectSetName, final ObjectSelectionBlock innerBlock) {
			_objectSetName = objectSetName;
			_innerBlock = innerBlock;
		}

		private void initialize() {
			_relevantTypes = initializeRelevantObjectTypes(_objectSetName);
			refreshObjectCache();
			_innerBlock.addChangeListener(_innerChangeListener);
			isInitialized = true;
		}

		private Collection<ConfigurationObjectType> initializeRelevantObjectTypes(final String objectSetName) {
			final Collection<ConfigurationObjectType> result = new HashSet<ConfigurationObjectType>();
			for(final SystemObjectType type : _innerBlock.getAllObjectTypes()) {
				if(type instanceof ConfigurationObjectType) {
					final ConfigurationObjectType configurationObjectType = (ConfigurationObjectType)type;
					addToListIfTypeHasObjectSet(objectSetName, configurationObjectType, result);
				}
			}
			return result;
		}

		private void addToListIfTypeHasObjectSet(
				final String objectSetName, final ConfigurationObjectType configurationObjectType, final Collection<ConfigurationObjectType> resultList) {
			for(final ObjectSetUse objectSetUse : configurationObjectType.getObjectSetUses()) {
				if(objectSetUse.getObjectSetName().equals(objectSetName)) {
					resultList.add(configurationObjectType);
					return;
				}
			}
			for(final SystemObjectType systemObjectType : configurationObjectType.getSubTypes()) {
				addToListIfTypeHasObjectSet(objectSetName, (ConfigurationObjectType)systemObjectType, resultList);
			}
		}

		private void refreshObjectCache() {
			stopMutableSetChangeListeners();
			_mutableSets.clear();
			_objectCache.clear();
			// Liste der Objekte, von denen die Mengen abgefragt werden
			for(final SystemObject object : _innerBlock.getAllObjects(_relevantTypes)) {
				if(object instanceof ConfigurationObject) {
					final ConfigurationObject configurationObject = (ConfigurationObject)object;
					final de.bsvrz.dav.daf.main.config.ObjectSet objectSet = configurationObject.getObjectSet(_objectSetName);
					if(objectSet != null) {
						_objectCache.addAll(objectSet.getElements());
						if(objectSet instanceof MutableSet) {
							final MutableSet mutableSet = (MutableSet)objectSet;
							_mutableSets.add(mutableSet);
						}
					}
				}
			}
			startMutableSetChangeListeners();
		}

		private void stopMutableSetChangeListeners() {
			for(final MutableSet mutableSet : _mutableSets) {
				mutableSet.removeChangeListener(_mutableSetChangeListener);
			}
		}

		private void startMutableSetChangeListeners() {
			for(final MutableSet mutableSet : _mutableSets) {
				mutableSet.addChangeListener(_mutableSetChangeListener);
			}
		}

		@Override
		public String toString() {
			return "ObjectSelectionBlockObjectSet{" + "_objectSetName='" + _objectSetName + '\'' + ", _innerBlock=" + _innerBlock + '}';
		}

		public boolean contains(final SystemObject object) {
			if(!isInitialized) initialize();
			return _objectCache.contains(object);
		}

		/** Wird derzeit nicht gebraucht, da Mengenabfragen nicht verschachtelt werden k�nnen. Implementierung schadet aber nicht und ist trivial. */
		public Collection<SystemObjectType> getAllObjectTypes() {
			if(!isInitialized) initialize();
			return _connection.getDataModel().getBaseTypes();
		}

		public Collection<SystemObject> getAllObjects(final Collection<? extends SystemObjectType> types) {
			if(!isInitialized) initialize();
			return Collections.unmodifiableCollection(_objectCache);
		}

		public Collection<Region> getRegions() {
			if(_innerBlock instanceof ObjectSelectionBlockRegion) {
				final ObjectSelectionBlockRegion objectSelectionBlockRegion = (ObjectSelectionBlockRegion)_innerBlock;
				return objectSelectionBlockRegion.getRegions();
			}
			return Collections.emptyList();
		}
	}
}
