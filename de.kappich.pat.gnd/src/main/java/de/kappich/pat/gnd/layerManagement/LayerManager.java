/*
 * Copyright 2009 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.kappich.pat.gnd.
 * 
 * de.kappich.pat.gnd is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.kappich.pat.gnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.kappich.pat.gnd; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package de.kappich.pat.gnd.layerManagement;

import de.kappich.pat.gnd.displayObjectToolkit.DOTManager;
import de.kappich.pat.gnd.pluginInterfaces.DisplayObjectType;

import de.bsvrz.sys.funclib.debug.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Eine Singleton-Klasse zur Verwaltung aller Layer, die das Interface TableModel implementiert, damit
 * sie in einem JTable angezeigt werden kann.
 * <p>
 * Dass diese Klasse (wie z.B. auch der {@link DOTManager} als Singleton implementiert ist, ist hinsichtlich 
 * denkbarer Erweiterungen sicherlich keine optimale L�sung, aber erspart gegenw�rtig die Implementation 
 * der Kommunikation zwischen verschiedenen Instanzen dieser Klasse.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8080 $
 *
 */
@SuppressWarnings("serial")
public class LayerManager extends AbstractTableModel implements TableModel {
	
	private static final LayerManager _instance = new LayerManager();
	
	private static final Debug _debug = Debug.getLogger();
	
	/**
	 * Die f�r ein Singleton �bliche Methode, um an die einzige Instanz der Klasse zu gelangen.
	 * 
	 * @return den LayerManager
	 */
	public static LayerManager getInstance() {
		return _instance;
	}
	
	/**
	 * Mit Hilfe dieser Methode kann man den LayerManager dazu zwingen, sich erneut zu
	 * konstruieren, was etwa nach dem Importieren von Pr�ferenzen sinnvoll ist.
	 */
	public static void refreshInstance() {
		_instance._layers.clear();
		_instance._notChangables.clear();
		_instance._layerHashMap.clear();
		_instance.initDefaultLayers();
		_instance.initUserDefinedLayers();
	}
	
	/*
	 * Ein LayerManager verwaltet alle zur GND geh�renden Layer in einer Liste. Darin 
	 * stehen einige in seinem Kode definierte Layer, die unver�nderbar sind, sowie 
	 * alle vom Benutzer definierten Layer, die in den Pr�ferenzen abgespeichert werden. 
	 */
	private LayerManager () {
		initDefaultLayers();
		initUserDefinedLayers();
	}
	
	/**
	 * Gibt die Liste aller Layer zur�ck.
	 * 
	 * @return die Liste aller Layer
	 */
	public List<Layer> getLayers() {
		return _layers;
	}
	
	/**
	 * Gibt den Layer mit dem �bergebenen Namen zur�ck.
	 * 
	 * @return den geforderten Layer
	 */
	public Layer getLayer( String layerName) {
		return _layerHashMap.get(layerName);
	}
	
	/**
	 * Gibt den Layer an der i-ten Stelle der Layerliste zur�ck, wobei die Z�hlung mit 0 beginnt.
	 * 
	 * @return den geforderten Layer
	 */
	public Layer getLayer( int i) {
		return _layers.get(i);
	}

	/*
	 * Geh�rt zur Implementation des TableModel.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	/*
	 * Geh�rt zur Implementation des TableModel.
	 */
	public int getRowCount() {
		return _layers.size();
	}

	/*
	 * Geh�rt zur Implementation des TableModel.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
	    return _layers.get(rowIndex).getName();
    }
	
	/**
	 * Definiert den Tooltipp f�r die Felder der Tabelle.
	 * Geh�rt zur Implementation des TableModel.
	 * 
	 * @param rowIndex ein Zeilenindex
	 * @param columnIndex ein Spaltenindex
	 */
	public String getTooltipAt(int rowIndex, int columnIndex) {
		if (rowIndex>=0 && rowIndex<getRowCount()) {
			return _layers.get(rowIndex).getInfo();
		} else {
			return null;
		}
    }
	
	/*
	 * Geh�rt zur Implementation des TableModel.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}
	
	/**
	 * F�gt den Layer der Layerliste an, wenn nicht schon ein gleichnamiger Layer existiert.
	 * 
	 * @param layer ein Layer
	 * @throws IllegalArgumentException wenn bereits ein gleichnamiger Layer existiert
	 */
	public void addLayer( Layer layer) throws IllegalArgumentException {
		if ( _layerHashMap.containsKey(layer.getName())) {
			throw new IllegalArgumentException("Ein Layer mit diesem Namen existiert bereits.");
		}
		_layers.add( layer);
		_layerHashMap.put(layer.getName(), layer);
		layer.putPreferences(getPreferenceStartPath());
		final int lastIndex = _layers.size()-1;
		fireTableRowsInserted( lastIndex, lastIndex);
		notifyChangeListenersLayerAdded( layer);
	}
	
	/**
	 * �ndert den gleichnamigen Layer.
	 * 
	 * @param layer ein Layer
	 * @throws IllegalArgumentException wenn der Layer nicht bekannt ist
	 */
	public void changeLayer( Layer layer) throws IllegalArgumentException {
		final String name = layer.getName();
		if ( !_layerHashMap.containsKey(name)) {
			throw new IllegalArgumentException("Ein Layer mit diesem Namen existiert nicht.");
		}
		final Layer existingLayer = _layerHashMap.get( name);
		existingLayer.deletePreferences( getPreferenceStartPath());
		existingLayer.setInfo( layer.getInfo());
		existingLayer.setGeoReferenceType( layer.getGeoReferenceType());
		existingLayer.setDotCollection( layer.getDotCollection());
		for ( int i = 0; i < _layers.size(); i++) {
			if ( _layers.get(i).getName().equals( name)) {
				fireTableRowsUpdated(i, i);
				break;
			}
		}
		layer.putPreferences( getPreferenceStartPath());
		notifyChangeListenersLayerChanged( layer);
	}
	
	/**
	 * Entfernt den �bergebenen Layer auf Basis eines Namensvergleichs aus der Liste aller Layer
	 * und damit auch aus den Pr�ferenzen. Entspricht einer kompletten L�schung des Layers.
	 * Wirkt aber nicht f�r im Kode definierte Layer. Wird ein Layer gel�scht, so erh�lt man
	 * den R�ckgabewert <code>true</code>, sonst false.
	 * 
	 * @param layer ein Layer
	 * @return <code>true</code> genau dann, wenn der Layer gel�scht wurde
	 */
	public boolean removeLayer(  Layer layer) {
		final String name = layer.getName();
		if ( !_notChangables.contains(name)) {
			final int index = remove( name);
			if ( index >-1) {
				layer.deletePreferences(getPreferenceStartPath());
				fireTableRowsDeleted(index, index);
				notifyChangeListenersLayerRemoved( name);
				return true;
			}
		}
		return false;
	}
	/**
	 * 	L�scht alle benutzerdefinierten Layer.
	 */
	public void clearLayers() {
		for ( Layer layer : _layers) {
			removeLayer( layer);
		}
	}
	
	private void initDefaultLayers() {
		initDefaultLayersLines();
		initDefaultLayersPoints();
	}
	
	private void initDefaultLayersLines() {
		Layer streetNetLayer = new Layer("Stra�ennetz", 
				"Dargestellt werden alle Stra�enteilsegmente", "typ.stra�enTeilSegment");
		final DisplayObjectType dotBlackLine = DOTManager.getInstance().getDisplayObjectType("Konfigurationslinie schwarz");
		if (dotBlackLine != null) {
			streetNetLayer.addDisplayObjectType( dotBlackLine, Integer.MAX_VALUE, 1000000);
		} else {
			_debug.warning( "Fehler in LayerManager.initDefaultLayersLines: das Darstellungsobjekt 'Konfigurationslinie' konnte nicht gefunden werden.");
		}
		final DisplayObjectType dotGreyLine = DOTManager.getInstance().getDisplayObjectType("Konfigurationslinie hellgrau");
		if (dotGreyLine != null) {
			streetNetLayer.addDisplayObjectType( dotGreyLine, 1000000, 1);
		} else {
			_debug.warning( "Fehler in LayerManager.initDefaultLayersLines: das Darstellungsobjekt 'Konfigurationslinie' konnte nicht gefunden werden.");
		}
		_layers.add( streetNetLayer);
		_layerHashMap.put(streetNetLayer.getName(), streetNetLayer);
		_notChangables.add( streetNetLayer.getName());
		
		Layer olsim1Layer = new Layer("St�rf�llzustand OLSIM 1", 
				"Berechnet an Stra�ensegmenten", "typ.stra�enSegment");
		String dotName = "St�rfallzustand OLSIM 1 (grob)";
		final DisplayObjectType dotOlsim1Rough = DOTManager.getInstance().getDisplayObjectType(dotName);
		if (dotOlsim1Rough != null) {
			olsim1Layer.addDisplayObjectType(dotOlsim1Rough, Integer.MAX_VALUE, 500000);
		} else {
			_debug.warning( "Fehler in LayerManager.initDefaultLayersLines: das Darstellungsobjekt '" + dotName + "' konnte nicht gefunden werden.");
		}
		dotName = "St�rfallzustand OLSIM 1 (fein)";
		final DisplayObjectType dotOlsim1Smooth = DOTManager.getInstance().getDisplayObjectType( dotName);
		if (dotOlsim1Smooth != null) {
			olsim1Layer.addDisplayObjectType( dotOlsim1Smooth, 500000, 1);
		} else {
			_debug.warning( "Fehler in LayerManager.initDefaultLayersLines: das Darstellungsobjekt '" + dotName + "' konnte nicht gefunden werden.");
		}
		_layers.add( olsim1Layer);
		_layerHashMap.put(olsim1Layer.getName(), olsim1Layer);
		_notChangables.add( olsim1Layer.getName());
	}
	
	private void initDefaultLayersPoints () {
		Layer simpleDetectionSiteLayer = new Layer("Messquerschnitte", null, "typ.messQuerschnitt");
		String dotName = "Verkehrsdatenanalyse kurz";
		final DisplayObjectType dotConstraint = DOTManager.getInstance().getDisplayObjectType( dotName);
		if ( dotConstraint != null) {
			simpleDetectionSiteLayer.addDisplayObjectType( dotConstraint, 300000, 1);
		} else {
			_debug.warning("Fehler in LayerManager.initDefaultLayersPoints: das Darstellungsobjekt '" + dotName +"' konnte nicht gefunden werden.");
		}
		_layers.add( simpleDetectionSiteLayer);
		_layerHashMap.put(simpleDetectionSiteLayer.getName(), simpleDetectionSiteLayer);
		_notChangables.add( simpleDetectionSiteLayer.getName());
		
		Layer advancedDetectionSiteLayer = new Layer("Messquerschnitte (erweitert)", null, "typ.messQuerschnitt");
		dotName = "MQ, einfach kombinierte Darstellung";
		final DisplayObjectType dotCombined = DOTManager.getInstance().getDisplayObjectType( dotName);
		if ( dotCombined != null) {
			advancedDetectionSiteLayer.addDisplayObjectType( dotCombined, 300000, 1);
		} else {
			_debug.warning( "Fehler in LayerManager.initDefaultLayersPoints: das Darstellungsobjekt '" + dotName +"' konnte nicht gefunden werden.");
		}
		_layers.add( advancedDetectionSiteLayer);
		_layerHashMap.put(advancedDetectionSiteLayer.getName(), advancedDetectionSiteLayer);
		_notChangables.add( advancedDetectionSiteLayer.getName());
		
		Layer complexTestLayer = new Layer("Messquerschnitte (Testlayer)", "MQ", "typ.messQuerschnitt");
		dotName = "MQ, Testdarstellung 1";
		final DisplayObjectType dotDetectionSiteTest = DOTManager.getInstance().getDisplayObjectType(dotName);
		if (dotDetectionSiteTest != null) {
			complexTestLayer.addDisplayObjectType(dotDetectionSiteTest, 500000, 1);
		} else {
			_debug.warning( "Fehler in LayerManager.initDefaultLayersPoints: das Darstellungsobjekt '" + dotName +"' konnte nicht gefunden werden.");
		}
		_layers.add( complexTestLayer);
		_layerHashMap.put(complexTestLayer.getName(), complexTestLayer);
		_notChangables.add( complexTestLayer.getName());
	}
	
	/**
	 * Gibt das Preferences-Objekt f�r den Ausgangspunkt zur Ablage der Pr�ferenzen des 
	 * Layermanagers zur�ck.
	 * 
	 * @return den Ausgangsknoten
	 */
	public static Preferences getPreferenceStartPath() {
		return Preferences.userRoot().node("de/kappich/pat/gnd/Layer");
	}
	
	private void initUserDefinedLayers() {
		Preferences classPrefs = getPreferenceStartPath();
		String[] layers;
		try {
			layers = classPrefs.childrenNames();
        }
        catch(BackingStoreException e) {
	        
	        throw new UnsupportedOperationException("Catch-Block nicht implementiert - BackingStoreException", e);
        }
        for ( String layerName : layers) {
        	Preferences layerPrefs = classPrefs.node(classPrefs.absolutePath() + "/" + layerName);
        	Layer layer = new Layer();
        	if ( layer.initializeFromPreferences(layerPrefs)) {
        		_layers.add( layer);
        		_layerHashMap.put(layer.getName(), layer);
        	}
        }
	}
	
	private int remove( final String layerName) {
		int index = 0;
		for ( Layer layer : _layers) {
			if ( layerName.equals( layer.getName())) {
				_layers.remove(index);
				_layerHashMap.remove(layerName);
				return index;
			}
			index++;
		}
		return -1;
	}
	
	@Override
    public String toString() {
		String s = new String();
		s = "[LayerManager: ";
		for ( Layer layer: _layers) {
			s += layer.toString();
		}
		s += "[Unver�nderebare Layer: ";
		for ( String name : _notChangables) {
			s += "[" + name + "]";
		}
		s += "]";
		return s;
	}
	
	/**
	 * Gibt <code>true</code> zur�ck, wenn der Layer ver�nderbar ist. Im Moment ist ein Layer genau
	 * dann unver�nderbar, wenn er im Kode definiert ist.
	 * 
	 * @param layer ein Layer
	 * @return <code>true</code> genau dann, wenn der Layer ver�nderbar ist 
	 */
	public boolean isChangeable( Layer layer) {
		return !(_notChangables.contains( layer.getName()));
	}
	
	/**
	 * Ein Interface f�r Listener, die �ber das Hinzuf�gen, L�schen und �ndern von Layern informiert werden wollen.
	 * 
	 * @author Kappich Systemberatung
	 * @version $Revision: 8080 $
	 *
	 */
	public interface LayerManagerChangeListener {
		/**
		 * Diese Methode wird aufgerufen, wenn der Layer hinzugef�gt wurde.
		 * 
		 * @param layer ein Layer
		 */
		void layerAdded( final Layer layer);
		/**
		 * Diese Methode wird aufgerufen, wenn der Layer ge�ndert wurde.
		 * 
		 * @param layer ein Layer
		 */
		void layerChanged( final Layer layer);
		/**
		 * Diese Methode wird aufgerufen, wenn der Layer gel�scht wurde.
		 * 
		 * @param layer ein Layer
		 */
		void layerRemoved( final String layerName);
	}
	
	/**
	 * F�gt das �bergebene Objekt der Liste der auf Layer�nderungen angemeldeten Objekte hinzu.
	 * 
	 * @param listener ein Listener
	 */
	public void addChangeListener(LayerManagerChangeListener listener) {
		_listeners.add(listener);
	}
	
	/**
	 * Entfernt das �bergebene Objekt aus der Liste der auf Layer�nderungen angemeldeten Objekte.
	 * 
	 * @param listener ein Listener
	 */
	public void removeChangeListener(LayerManagerChangeListener listener) {
		_listeners.remove(listener);
	}
	
	/**
	 * Informiert die auf Layer�nderungen angemeldeten Objekte �ber einen neu hinzugef�gten Layer.
	 * 
	 * @param layer ein Layer
	 */
	public void notifyChangeListenersLayerAdded( final Layer layer) {
		for ( LayerManagerChangeListener changeListener : _listeners) {
			changeListener.layerAdded( layer);
		}
	}
	
	/**
	 * Informiert die auf Layer�nderungen angemeldeten Objekte �ber einen ge�nderten Layer.
	 * 
	 * @param layer ein Layer
	 */
	public void notifyChangeListenersLayerChanged( final Layer layer) {
		for ( LayerManagerChangeListener changeListener : _listeners) {
			changeListener.layerChanged( layer);
		}
	}
	
	/**
	 * Informiert die auf Layer�nderungen angemeldeten Objekte �ber einen gel�schten Layer.
	 * 
	 * @param layer ein Layer
	 */
	public void notifyChangeListenersLayerRemoved( final String layerName) {
		for ( LayerManagerChangeListener changeListener : _listeners) {
			changeListener.layerRemoved( layerName);
		}
	}
	
	/**
	 * Gibt <code>true</code> zur�ck, falls der durch den Namen angegebene Darstellungstyp von einem
	 * der Layer verwendet wrd.
	 * 
	 * @param  displayObjectTypeName der Name eines Darstellungstyps
	 * @return <code>true</code> genau dann, wenn der Darstellungstyp benutzt wird
	 */
	public boolean displayObjectTypeIsUsed( final String displayObjectTypeName) {
		for ( Layer layer : _layers) {
			if ( layer.getDotCollection().displayObjectTypeIsUsed( displayObjectTypeName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gibt die Namen all der Layer zur�ck, die den durch den Namen angegebene Darstellungstyp
	 * verwenden.
	 * 
	 * @param  displayObjectTypeName der Name eines Darstellungstyps
	 * @return eine Liste von Darstellungstypen
	 */
	public List<String> getLayersUsingTheDisplayObjectType( final String displayObjectTypeName) {
		List<String> returnList = new ArrayList<String>();
		for ( Layer layer : _layers) {
			if ( layer.getDotCollection().displayObjectTypeIsUsed( displayObjectTypeName)) {
				returnList.add( layer.getName());
			}
		}
		return returnList;
	}
	
	final private static String[] columnNames = {"Name des Layers"};
	final private List<Layer> _layers = new ArrayList<Layer>();
	final private Set<String> _notChangables = new HashSet<String>();
	final private Map<String, Layer> _layerHashMap = new HashMap<String, Layer>();
	final private List<LayerManagerChangeListener> _listeners = new CopyOnWriteArrayList<LayerManagerChangeListener>();
}
