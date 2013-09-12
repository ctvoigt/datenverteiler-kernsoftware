/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.puk.config.
 * 
 * de.bsvrz.puk.config is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.puk.config is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.puk.config; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.puk.config.main.dataview;

import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectLookup;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.puk.config.configFile.datamodel.ConfigDataModel;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Klasse zum Zugriff auf Konfigurationdaten, bei der f�r jeden Konfigurationsbereich eine bestimmte vorgegebene Version ber�cksichtigt wird.
 * Beim Erzeugen des Objekts wird dem Konstruktor mitgeteilt, welcher Konfigurationsbereich in welcher Version zu betrachten ist.
 * Alle Abfragemethoden ber�cksichtigen dann die angegebenen Versionen.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 0 $
 */
public class VersionedView implements ObjectLookup {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private DataModel _dataModel;

	private Map<ConfigurationArea, Short> _configurationAreaVersions;

	/** Map der aktuell noch nicht g�ltigen Objekte, die aber in der durch den View zu betrachtenden Version g�ltig werden. Schl�ssel ist die Pid des Objekts. */
	private Map<String,SystemObject> _newlyActiveObjects = null;

	public VersionedView(final DataModel dataModel, final Map<ConfigurationArea, Short> configurationAreaVersions) {
		_dataModel = dataModel;
		_configurationAreaVersions = configurationAreaVersions;
	}

	/**
	 * Bestimmt, ob das angegebene System-Objekt ein Element des angegebenen Typs ist. Ein System-Objekt ist Element des Typs, wenn der Typ des Objekts mit
	 * dem angegebenen Typ �bereinstimmt oder diesen erweitert.
	 *
	 * @param object Zu pr�fendes Objekt.
	 * @param ancestorType   Zu pr�fender Typ.
	 *
	 * @return <code>true</code>, wenn der �bergebene Typ mit dem Typ des Objekts oder mit einem der direkten oder indirekten Vorg�nger in der Vererbungshierarchie
	 *         �bereinstimmt; sonst <code>false</code>.
	 */
	public boolean isOfType(SystemObject object, SystemObjectType ancestorType) {
		final SystemObjectType objectType = object.getType();
		if(ancestorType == objectType) return true;
		return inheritsFrom(objectType, ancestorType);
	}

	/**
	 * Pr�ft, ob der angegebene Typ <code>ancestorType</code> in der Typhierarchie oberhalb angegebenen Typs <code>derivedType</code> vorkommt.
	 * Dies ist dann der Fall, wenn <code>derivedType</code> direkt oder indirekt <code>ancestorType</code> erweitert und damit dessen
	 * Eigenschaften erbt.
	 *
	 * @param derivedType Zu pr�fender abgeleiteter Typ
	 * @param ancestorType        Zu pr�fender �bergeordneter Typ
	 *
	 * @return <code>true</code> wenn <code>derivedType</code> direkt oder indirekt <code>ancestorType</code> erweitert, sonst <code>false</code>.
	 */
	public boolean inheritsFrom(final SystemObjectType derivedType, final SystemObjectType ancestorType) {
		final Collection<SystemObjectType> superTypes = getSuperTypes(derivedType);
		// direkte Vererbung
		if (superTypes.contains(ancestorType)) return true;
		// indirekte Vererbung
		for (SystemObjectType superType : superTypes) {
			if (inheritsFrom(superType, ancestorType)) return true;
		}
		return false;
	}

	/**
	 * Liefert eine Liste der Typ-Objekte die von dem angegebenen Typ-Objekt erweitert werden.
	 *
	 * @return Liste von {@link SystemObjectType Typ-Objekten}
	 */
	public Collection<SystemObjectType> getSuperTypes(final SystemObjectType type) {
		Collection<SystemObjectType> superTypes = new ArrayList<SystemObjectType>();
		NonMutableSet set = (NonMutableSet)type.getObjectSet("SuperTypen");
		if (set != null) {
			final Collection<SystemObject> elements = getElements(set);
			for (SystemObject systemObject : elements) {
				superTypes.add((SystemObjectType) systemObject);
			}
		}
		return superTypes;
	}

	public List<ObjectSetUse> getDirectObjectSetUses(SystemObjectType type) {
		List<ObjectSetUse> directObjectSetUses = new ArrayList<ObjectSetUse>();
		ObjectSet set = type.getObjectSet("Mengen");
		if(set != null) {
			for(SystemObject systemObject : getElements(set)) {
				directObjectSetUses.add((ObjectSetUse)systemObject);
			}
		}
		return directObjectSetUses;
	}

	public List<ObjectSetUse> getObjectSetUses(SystemObjectType type) {
		Set<ObjectSetUse> objectSetUses = new HashSet<ObjectSetUse>();
		objectSetUses.addAll(getDirectObjectSetUses(type));
		for(SystemObjectType superType : getSuperTypes(type)) {
			objectSetUses.addAll(getObjectSetUses(superType));
		}
		return new ArrayList(objectSetUses);
	}

	/** Bestimmt die Elemente der angegebenen Menge
	 *
	 * @param set  Zu betrachtende Menge.
	 * @return Elemente der Menge.
	 */
	public Collection<SystemObject> getElements(final ObjectSet set) {
		if(set instanceof NonMutableSet) {
			NonMutableSet nonMutableSet = (NonMutableSet)set;
			short version = getVersion(nonMutableSet);
			return nonMutableSet.getElementsInVersion(version);
		}
		if(set instanceof MutableSet) {
			MutableSet mutableSet = (MutableSet)set;
			return mutableSet.getElements();
		}
		throw new IllegalStateException("ObjektMenge hat einen unbekannten Typ");
	}

	/**
	 * Bestimmt die betrachtete Version des Konfigurationsbereichs in dem das angegebene Objekt enthalten ist.
	 * @param object Systemobjekt zu dem die Version ermittelt werden soll.
	 * @return Version des Konfigurationsbereichs des angegebenen Systemobjekts
	 */
	public short getVersion(final SystemObject object) {
		final ConfigurationArea configurationArea = object.getConfigurationArea();
		Short version = _configurationAreaVersions.get(configurationArea);
		if(version != null) return version.shortValue();
		// throw new IllegalStateException("Version des Konfigurationsbereichs " + object.getConfigurationArea().getPidOrNameOrId() + " nicht definiert");
		// Falls in der Map ein Konfigurationsbereich nicht enthalten ist, dann wird die aktive Version des Bereichs zur�ckgegeben.
		return configurationArea.getActiveVersion();
	}

	/**
	 * Liefert das System-Objekt mit der angegebenen PID zur�ck.
	 *
	 * @param pid Die permanente ID des System-Objekts
	 *
	 * @return Das gew�nschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen PID gibt.
	 *
	 * @see de.bsvrz.dav.daf.main.config.DataModel
	 */
	public SystemObject getObject(String pid) {
		if(pid == null || pid.equals("") || pid.equals("null")) return null;
		SystemObject object = _dataModel.getObject(pid);
		if(isValid(object)) return object;
		synchronized(this) {
			// Sans, STS, KonfigAss: Korrektur _newlyActiveObjects
			if(_newlyActiveObjects == null) {
				_newlyActiveObjects = new HashMap<String, SystemObject>();
				

				long startTime = System.currentTimeMillis();
				final Map<String, ConfigurationArea> areas = ((ConfigDataModel)_dataModel).getAllConfigurationAreas();
				for(ConfigurationArea configurationArea : areas.values()) {
					final Collection<SystemObject> newObjects = configurationArea.getNewObjects();
					for(SystemObject newObject : newObjects) {
						final String newObjectPid = newObject.getPid();
						// Sans, STS, KonfigAss: isValid(newObject) nicht ben�tigt
						if(newObjectPid != null && newObjectPid.length()>0) {
							_newlyActiveObjects.put(newObjectPid,newObject);
						}
					}

					_debug.fine("Zwischenspeicherung von neuerdings aktiven Objekten dauerte in Millisekunden", System.currentTimeMillis() - startTime);
				}
			}
			return _newlyActiveObjects.get(pid);
		}


//    final Set<ConfigurationArea> configurationAreas = _configurationAreaVersions.keySet();
//    for(ConfigurationArea configurationArea : configurationAreas) {
//      final Collection<SystemObject> newObjects = configurationArea.getNewObjects();
//      for(SystemObject systemObject : newObjects) {
//        if(systemObject.getPid().equals(pid) && isValid(systemObject)) return systemObject;
//      }
//    }
//    return null;
	}

	public boolean isValid(final SystemObject object) {
		if(object == null) return false;
		if(object instanceof ConfigurationObject) {
			ConfigurationObject configurationObject = (ConfigurationObject)object;
			short viewingVersion = getVersion(configurationObject);
			final short validSince = configurationObject.getValidSince();
			final short notValidSince = configurationObject.getNotValidSince();
			return validSince <= viewingVersion && (notValidSince == 0 || viewingVersion < notValidSince);
		}
		else  return object.isValid();
	}

	/**
	 * Liefert das System-Objekt mit der angegebenen Objekt-ID zur�ck.
	 *
	 * @param id Die Objekt-ID des System-Objekts
	 *
	 * @return Das gew�nschte System-Objekt oder <code>null</code>, wenn es kein Objekt mit der angegebenen ID gibt.
	 *
	 * @see de.bsvrz.dav.daf.main.config.DataModel
	 */
	public SystemObject getObject(long id) {
		return _dataModel.getObject(id);
	}
}
