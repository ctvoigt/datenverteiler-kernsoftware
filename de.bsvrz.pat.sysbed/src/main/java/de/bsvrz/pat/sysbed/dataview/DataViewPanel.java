/*
 * Copyright 2010 by Kappich Systemberatung, Aachen
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2006 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.pat.sysbed.dataview;

import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.SelectionListener;
import de.bsvrz.pat.sysbed.dataview.selectionManagement.SelectionManager;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.*;
import java.util.List;


/**
 * Dieses Panel stellt die Datens�tze, die das {@link DataViewModel Model} �bergibt, in einem 
 * ScrollPane dar. Mit dem Konstruktor wird ein baumartiger Header erstellt, dessen Spaltenbreiten 
 * durch Schieberegler ver�ndert werden kann. Damit dies funktioniert, muss nachdem der Header 
 * erzeugt und dieser angezeigt (z.B. durch validate() oder durch Frame.setVisible()) wird 
 * mit der Methode {@link #initHeaderSize()} initialisiert werden.
 * 
 * @author Kappich Systemberatung
 * @version $Revision: 8369 $
 */

@SuppressWarnings("serial")
public class DataViewPanel extends JPanel implements DataViewListener {
	
	/** der Debug-Logger */
	private final Debug _debug = Debug.getLogger();
	
	/* ############## Variablen ############## */
	/** speichert eine Instanz des DataViewModel */
	private final DataViewModel _dataViewModel;
	
	/** speichert die Attributgruppe */
	private final AttributeGroup _attributeGroup;
	
	/** speichert alle Datens�tze, die von der Applikation gesendet wurden */
	private final List<DataTableObjectRenderer> _dtoRenderers = new ArrayList<DataTableObjectRenderer>();
	
	/** speichert das dargestellte ScrollPane */
	private final JScrollPane _scrollPane = new JScrollPane();
	
	/** speichert die Sruktur des Headers */
	private HeaderGrid _headerGrid;
	
	/** merkt sich, ob es sich um den ersten Aufruf handelt */
	private boolean _firstRun = true;
	
	/** speichert den vertikalen Scrollbalken */
	private final JScrollBar _verticalScrollBar = _scrollPane.getVerticalScrollBar();
	
	/** der obere Platzhalter f�r den Zeilenheader; wird nur f�r die H�henarithmetik ben�tigt */
	private final JPanel _emptyDummyUpperRowHeaderPanel = new JPanel();
	
	/** der untere Platzhalter des Zeilenheaders; wird nur f�r die H�henarithmetik ben�tigt */
	private final JPanel _emptyDummyLowerRowHeaderPanel = new JPanel();
	
	/** der obere Platzhalter f�r den Viewport; wird nur f�r die H�henarithmetik ben�tigt */
	private final JPanel _emptyDummyUpperViewportPanel = new JPanel();
	
	/** der untere Platzhalter des Viewports; wird nur f�r die H�henarithmetik ben�tigt */
	private final JPanel _lowerViewportPanel = new JPanel();
	
	/** der sichtbare Teil des Zeilenheaders */
	private final JPanel _rowHeaderPanel = new JPanel();
	
	/** der sichtbare Teil des Viewports */
	private final JPanel _viewportPanel = new JPanel();
	
	/** der Index des ersten Datensatzes, der angezeigt wird */
	private int _firstRow = 0;
	
	/** der Index des letzten Datensatzes, der angezeigt wird */
	private int _lastRow = 0;
	
	/** die sichtbare H�he des Viewports */
	private int _screenHeight;
	
	/** speichert das Zeitformat f�r die Spalte 'Zeit' */
	private final String TIME_FORMAT = "dd.MM.yyyy HH:mm:ss,SSS";
	
	/** die Breite des Zeilenheaders */
	private int _rowHeaderWidth = 0;
	
	/** der Selektion-Manager der Online-Tabelle */
	private SelectionManager _selectionManager;
	
	/* ########### Konstruktor und Aufbau des Panels ########### */
	
	/**
	 * Konstruktor. Ben�tigt eine Instanz des {@link DataViewModel}. Das Model gibt Benachrichtigungen, 
	 * falls sich an den anzuzeigenden Daten etwas �ndert.
	 * 
	 * @param model
	 *            das DataViewModel
	 */
	public DataViewPanel(DataViewModel model) {
		_dataViewModel = model;
		_attributeGroup = _dataViewModel.getAttributeGroup();
		
		_selectionManager = new SelectionManager( _dataViewModel);
		_selectionManager.addSelectionListener( new SelectionListener() {
			public void cellSelectionChanged(final SelectionManager selectionManager, final Set<CellKey> keys) {
				Map<RowElement, CellKey> rowElements = findRowElements( keys);
				for ( RowElement rowElement : rowElements.keySet()) {
					final CellKey rowElementsKey = rowElements.get( rowElement);
					rowElement.setSelectionColors( selectionManager.isCellKeySelected( rowElementsKey));
					rowElement.repaint();
				}
				Map<RowPanel, CellKey> rowPanels = findRowPanels( keys); 
				for ( RowPanel rowPanel : rowPanels.keySet()) {
					final CellKey rowPanelCellKey = rowPanels.get( rowPanel);
					rowPanel.setSelectionBorder( selectionManager.isCellKeySelected( rowPanelCellKey));
					rowPanel.repaint();
				}
			}

			public void rowSelectionChanged(SelectionManager selectionManager, final Set<RowKey> keys) {
				Map<JComponent, RowKey> rowHeaderRows = findRowHeaderRows(keys);
				for ( JComponent rowHeaderRow : rowHeaderRows.keySet()) {
					final RowKey rowElementsKey = rowHeaderRows.get( rowHeaderRow);
					final Component[] components = rowHeaderRow.getComponents();
					for ( Component component : components) {
						if ( selectionManager.isRowSelected( rowElementsKey)) {
							component.setBackground(component.getParent().getBackground().darker());
						} else {
							component.setBackground( null);
						}
					}
					rowHeaderRow.repaint();
				}
            }
		});
		
		createAndShowGui();
	}
	
	/**
	 * Methode zur Ermittlung des Datensatzindex
	 * 
	 * @param key
	 *            Schl�ssel, wessen Datensatzindex gesucht wird
	 * @return Datensatzindex
	 */
	public long getDataindex(String key) {
		long dataIndex = 0L;
		for(DataTableObjectRenderer renderer : _dtoRenderers) {
			String nameOrPidOrId = renderer.getDataTableObject().getObject().getPidOrNameOrId();
			dataIndex = renderer.getDataTableObject().getDataIndex();
			String prefix = nameOrPidOrId + ";" + dataIndex;
			
			if(key.startsWith(prefix)) {
				return dataIndex;
			}
		}
		return dataIndex;
	}
	
	/**
	 * Methode zur Ermittlung von Komponenten
	 * 
	 * @param keys
	 *            die Schl�ssel
	 * @return die zugeh�rige Komponenten
	 */
	public Map<JComponent, RowKey> findRowHeaderRows( final Set<RowKey> keys) {
		Map<JComponent, RowKey> rowHeaderKeyMap = new HashMap<JComponent, RowKey>();
		for(DataTableObjectRenderer renderer : _dtoRenderers) {
			final RowKey rowKey = renderer.getRowKey();
			if ( keys.contains( rowKey)) {
				rowHeaderKeyMap.put(renderer.getRowHeaderRow(TIME_FORMAT), rowKey);
			}
		}
		return rowHeaderKeyMap;
	}
	
	/**
	 * Methode zur Ermittlung von RowElements
	 * 
	 * @param keys
	 *            die Schl�ssel
	 * @return die zugeh�rige RowElements
	 */
	public Map<RowElement,CellKey> findRowElements( final Set<CellKey> keys) {
		Map<RowElement,CellKey> rowElementKeyMap = new HashMap<RowElement, CellKey>();
		for(DataTableObjectRenderer renderer : _dtoRenderers) {
			appendRowElements( renderer.getRowData(), keys, rowElementKeyMap);
		}
		return rowElementKeyMap;
	}
	
	/**
	 * Methode zur Ermittlung von RowPanels
	 * 
	 * @param keys
	 *            die Schl�ssel
	 * @return die zugeh�rige RowPanels
	 */
	public Map<RowPanel,CellKey> findRowPanels( final Set<CellKey> keys) {
		Map<RowPanel,CellKey> rowPanelKeyMap = new HashMap<RowPanel, CellKey>();
		for(DataTableObjectRenderer renderer : _dtoRenderers) {
			final RowData rowData = renderer.getRowData();
			final JComponent component = rowData.getComponent();
			if ( component instanceof RowPanel) {
				final RowPanel panel = (RowPanel) component;
				rowPanelKeyMap.put( panel, rowData.getCellKey());
			}
		}
		return rowPanelKeyMap;
	}
	
	private void appendRowElements( final RowData rowData, 
			final Set<CellKey> keys, 
			final Map<RowElement,CellKey> rowElementKeyMap) {
		final List<Object> successors = rowData.getSuccessors();
		if ( successors.isEmpty()) { // hat eine RowElement-Komponente
			final CellKey cellKey = rowData.getCellKey();
			if ( keys.contains( cellKey)) {
				final JComponent component = rowData.getComponent();
				if ( (component != null) && (component instanceof RowElement)) {
					final RowElement rowElement = (RowElement) component;
					rowElementKeyMap.put( rowElement, cellKey);
				}
			}
		} else {
			for ( Object object : successors) {
				if ( object instanceof RowData) {	// keine Liste
					appendRowElements( (RowData) object, keys, rowElementKeyMap);
				} else {	// eine Liste
					final RowSuccessor rowSuccessor = (RowSuccessor) object;
					final List<RowData> successors2 = rowSuccessor.getSuccessors();
					for ( RowData rowData2 : successors2) {
						appendRowElements( rowData2, keys, rowElementKeyMap);
					}
				}
			}
		}
	}
	
	/** Erstellt die Elemente f�r das Panel und ordnet diese an. */
	private void createAndShowGui() {
		setScrollPane();
		
		// anordnen des ScrollPanes auf dem Panel (DataViewPanel)
		setLayout(new BorderLayout());
		add(_scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Das ScrollPane wird mit Spaltenheader, Zeilenheader und oberer linker Ecke ausgestattet 
	 * und initialisiert. Die vertikale Scrollbar wird ebenfalls initialisiert und ein 
	 * {@link VerticalScrollBarAdjustmentListener AdjustmentListener} wird angemeldet.
	 */
	private void setScrollPane() {
		// Initialisierung des oberen und unteren Platzhalters
		setUpperPanel(0);
		setLowerPanel(0);
		
		// Aufbau des ScrollPanes
		_scrollPane.setColumnHeaderView(createColumnHeader());
		_scrollPane.setRowHeaderView(createRowHeader());
		_scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, createUpperLeftCorner());
		_scrollPane.setViewportView(createViewport());
		
		// Aufbau des vertikalen ScrollBars
		_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		_verticalScrollBar.setUnitIncrement(20); // eine Zeile hat momentan 20 Pixel in der H�he!
		_verticalScrollBar.setBlockIncrement(20); // wie oben
		_verticalScrollBar.addAdjustmentListener(new VerticalScrollBarAdjustmentListener());
		
		// Horizontale Scrollbar
		JScrollBar horizontalScrollBar = _scrollPane.getHorizontalScrollBar();
		horizontalScrollBar.setUnitIncrement(30);
		horizontalScrollBar.setBlockIncrement(30);
	}
	
	/**
	 * Erstellt den vollst�ndigen Spaltenheader f�r das ScrollPane.
	 * 
	 * @return Spaltenheader
	 */
	private JPanel createColumnHeader() {
		_headerGrid = new HeaderGrid(null, _attributeGroup, this);
		setHeaderSuccessors(_headerGrid);
		JPanel westHeader = new JPanel(new BorderLayout());
		westHeader.add(_headerGrid.createHeader(), BorderLayout.WEST);
		return westHeader;
	}
	
	/**
	 * Erstellt den initialen Zeilenheader f�r das ScrollPane.
	 * 
	 * @return Zeilenheader
	 */
	private JPanel createRowHeader() {
		JPanel rowHeader = new JPanel();
		rowHeader.setLayout(new BoxLayout(rowHeader, BoxLayout.Y_AXIS));
		_rowHeaderPanel.setLayout(new BoxLayout(_rowHeaderPanel, BoxLayout.Y_AXIS));
		rowHeader.add(_emptyDummyUpperRowHeaderPanel);
		rowHeader.add(_rowHeaderPanel);
		rowHeader.add(_emptyDummyLowerRowHeaderPanel);
		
		JPanel westRowHeader = new JPanel(new BorderLayout());
		westRowHeader.add(rowHeader, BorderLayout.WEST);
		
		return westRowHeader;
	}
	
	/**
	 * Erstellt die Komponente f�r die obere linke Ecke im ScrollPane. Sie zeigt die Zeit und das Objekt an.
	 * 
	 * @return die Komponente
	 */
	private JComponent createUpperLeftCorner() {
		JLabel dataKindLabel = new JLabel("Art");
		dataKindLabel.setBorder(new EtchedBorder());
		dataKindLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel timeLabel = new JLabel("Zeit");
		timeLabel.setBorder(new EtchedBorder());
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel objectLabel = new JLabel("Objekt");
		objectLabel.setBorder(new EtchedBorder());
		objectLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		// passt die Breite dieser Headerspalte mit der Breite bei den Datens�tzen?
		JPanel dataKindPanel = new JPanel(new BorderLayout());
		dataKindPanel.add(dataKindLabel, BorderLayout.CENTER);
		
		JPanel gridPanel = new JPanel(new GridLayout(1, 2));
		gridPanel.add(timeLabel);
		gridPanel.add(objectLabel);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(dataKindPanel, BorderLayout.WEST);
		panel.add(gridPanel, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * Erstellt den initialen Viewport f�r das Scrollpane
	 * 
	 * @return Viewport des Scrollpanes
	 */
	private JPanel createViewport() {
		JPanel viewport = new JPanel();
		viewport.setLayout(new BoxLayout(viewport, BoxLayout.Y_AXIS));
		_viewportPanel.setLayout(new BoxLayout(_viewportPanel, BoxLayout.Y_AXIS));
		viewport.add(_emptyDummyUpperViewportPanel);
		viewport.add(_viewportPanel);
		viewport.add(_lowerViewportPanel);
		
		JPanel westViewport = new JPanel(new BorderLayout());
		westViewport.add(viewport, BorderLayout.WEST);
		
		return westViewport;
	}
	
	/* ################ Hilfsmethoden f�r das DataViewPanel ################### */
	
	/**
	 * Gibt den SelectionManager zur�ck.
	 * 
	 * @return den SelectionManager
	 */
	public SelectionManager getSelectionManager() {
		return _selectionManager;
	}
	
	/**
	 * Gibt den Spaltenheader zur�ck.
	 * 
	 * @return den Spaltenheader
	 */
	public HeaderGrid getHeaderGrid() {
		return _headerGrid;
	}
	
	/**
	 * Die Gr��e des Headers wird initial gesetzt. Dies ist notwendig, damit die Headergr��e durch die Maus 
	 * ver�ndert werden kann. Diese Methode muss nach dem Anzeigen der Tabelle, allerdings bevor die ersten 
	 * Daten kommen, aufgerufen werden.
	 * 
	 * @see #setHeaderSizes(HeaderGrid,int)
	 */
	public void initHeaderSize() {
		HeaderGrid.HeaderElement element = _headerGrid.getHeaderElement();
		int elementWidth = element.getSize().width;
		int viewportWidth = _scrollPane.getHorizontalScrollBar().getVisibleAmount() - _rowHeaderWidth; // 350 f�r den RowHeader
		int percent;
		if(elementWidth == 0 || elementWidth >= viewportWidth) {
			percent = 0;
		}
		else {
			if(elementWidth >= 500) {
				percent = 100;
			}
			else {
				percent = (int)(500 * 100) / elementWidth;
			}
		}
		setHeaderSizes(_headerGrid, percent);
		_scrollPane.validate();
	}
	
	/**
	 * Initialisiert die Gr��en aller Elemente des Headers rekursiv.
	 * 
	 * @param headerGrid
	 *            Spaltenheader
	 * @param percent
	 *            gibt die prozentuale Ausdehnung an
	 */
	private void setHeaderSizes(HeaderGrid headerGrid, int percent) {
		HeaderGrid.HeaderElement element = headerGrid.getHeaderElement();
		element.setMinimumSize(new Dimension(0, 0)); // wird vom GridBagLayout nicht ben�tigt
		element.setMaximumSize(new Dimension(0, 0)); // wie setMinimumSize()
		Dimension size = element.getSize();
		headerGrid.getSplitter().setPreferredSize(new Dimension(5, size.height));
		final List<HeaderGrid> headerSuccessors = headerGrid.getHeaderSuccessors();
		if(headerSuccessors.size() > 0) {
			element.setPreferredSize(new Dimension(0, size.height));
			int numberOfColumns = 0;
			for(HeaderGrid successor : headerSuccessors) {
				setHeaderSizes(successor, percent);
				numberOfColumns += successor.getNumberOfColumns();
			}
			
			headerGrid.setNumberOfColumns(numberOfColumns);
		}
		else { // Blattknoten
			headerGrid.setNumberOfColumns(1);
			
			int newWidth;
			if(percent == 0) {
				newWidth = size.width + 1;
			}
			else {
				newWidth = (int)((size.width * percent) / 100);
			}
			Dimension dim = new Dimension(newWidth, size.height);
			element.setPreferredSize(dim);
			element.setSize(dim);
		}
	}
	
	/**
	 * Bestimmt die Nachfolger einer Attributgruppe bzw. einer Attributliste.
	 * 
	 * @param attributeList
	 *            Objekt vom Typ <code>HeaderGrid</code>, von dem die Nachfolger bestimmt werden sollen.
	 */
	private void setHeaderSuccessors(HeaderGrid attributeList) {
		/* Nachfolger bestimmen und anf�gen */
		Object node = attributeList.getHeaderElement().getObject();
		if(node instanceof AttributeGroup) { // es handelt sich um die Attributgruppe
			AttributeGroup atg = (AttributeGroup)node;
			final List<Attribute> attributes = atg.getAttributes();
			for(Attribute attribute : attributes) {
				HeaderGrid successor = new HeaderGrid(attributeList, attribute, this);
				attributeList.addHeaderSuccessor(successor);
				setHeaderSuccessors(successor); // weitere Nachfolger?
			}
		}
		else if(node instanceof Attribute) { // es handelt sich um ein Nachfolgeelement
			Attribute attribute = (Attribute)node;
			AttributeType attributeType = attribute.getAttributeType();
			if(attributeType instanceof AttributeListDefinition) { // Nachfolgeelement ist Attributliste
				AttributeListDefinition attributeListDefinition = (AttributeListDefinition)attributeType;
				final List<Attribute> attributes = attributeListDefinition.getAttributes();
				for(Attribute att : attributes) {
					HeaderGrid successor = new HeaderGrid(attributeList, att, this);
					attributeList.addHeaderSuccessor(successor);
					setHeaderSuccessors(successor);
				}
			}
		}
	}
	
	/**
	 * Hoffentlich bald �berfl�ssig. Sp�testens, wenn die Methode {@link #initHeaderSize()} 
	 * von der Applikation aufgerufen wird.
	 * 
	 * @return gibt <code>true</code> oder <code>false</code> zur�ck
	 */
	public boolean getFirstRun() {
		return _firstRun;
	}
	
	/**
	 * Kann auf false gesetzt werden, falls die Gr��en des Spaltenheaders initialisiert worden sind.
	 * 
	 * @param firstRun
	 *            <code>true</code> ist Default-Einstellung
	 */
	public void setFirstRun(boolean firstRun) {
		_firstRun = firstRun;
	}
	
	/* ################### Methoden des DataViewListeners ############ */
	
	/**
	 * F�gt eine beliebige Anzahl neuer Datens�tze an die bestehenden Datens�tze hinten an.
	 * 
	 * Achtung: es ist nicht gekl�rt, ob diese Methode auch daf�r sorgt, dass die Zeilen
	 * der Datens�tze sichtbar werden, falls sie im sichtbaren Bereich sind, oder ob dazu 
	 * noch der AdjustmentListener getriggert werden muss. S. addDataTableObject(..).
	 * 
	 * @param dataTableObjects
	 *            Liste neuer Datens�tze
	 */
	public void addDataTableObjects(final List<DataTableObject> dataTableObjects) {
		if(getFirstRun()) {
			initHeaderSize();
			setFirstRun(false);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(DataTableObject dataTableObject : dataTableObjects) {
					final DataTableObjectRenderer renderer = 
						new DataTableObjectRenderer(_headerGrid, dataTableObject, _selectionManager);
					_dtoRenderers.add(renderer);
					
					boolean check = checkRowHeight();
					if(check) {
						renderer.setLinks();
						_lastRow = _dtoRenderers.indexOf(renderer);
						
						/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
						 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
						 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
						 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
						 */
						final JComponent viewportRow = renderer.getViewportRow();
						
						JComponent rowHeaderRow = renderer.getRowHeaderRow(TIME_FORMAT);
						int height = renderer.getHeight();
						
						rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, height));
						rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, height));
						viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, height));
						
						_rowHeaderPanel.add(rowHeaderRow);
						_viewportPanel.add(viewportRow);
						_scrollPane.validate();
					}
					else {
						// H�he = alte H�he des Panels + neue H�he
						int lowerHeight = _lowerViewportPanel.getSize().height;
						lowerHeight += renderer.getHeight();
						setLowerPanel(lowerHeight);
						_scrollPane.revalidate();
					}
				}
			}
		});
	}
	
	/**
	 * TBD
	 * F�gt einen Datensatz an eine bestimmte Position der bisherigen Datens�tze ein. Befindet sich 
	 * die Position innerhalb der gerade dargestellten Datens�tze, dann wird die Darstellung neu 
	 * erzeugt und angezeigt.
	 * 
	 * DIESER KOMMENTAR IST FALSCH: Tats�chlich wird nur die erste Zeile ins Panel eingef�gt, 
	 * alle anderen kommen erst durch den AdjustmentListener hinzu. Da ein AdjustmentEvent
	 * k�nstlich nur recht komisch auszul�sen ist, h�tte man das besser so wie beschrieben
	 * implementiert. TN, nach langer Fehlersuche.
	 * 
	 * @param index
	 *            Position des neuen Datensatzes
	 * @param dataTableObject
	 *            der neue Datensatz
	 */
	public void addDataTableObject(final int index, final DataTableObject dataTableObject) {
		if(getFirstRun()) {
			initHeaderSize();
			setFirstRun(false);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DataTableObjectRenderer renderer = 
					new DataTableObjectRenderer(_headerGrid, dataTableObject, _selectionManager);
				int upperHeight = _emptyDummyUpperViewportPanel.getSize().height;
				int viewportHeight = _viewportPanel.getSize().height;
				int lowerHeight = _lowerViewportPanel.getSize().height;
				int value = _verticalScrollBar.getModel().getValue();
				_screenHeight = _verticalScrollBar.getVisibleAmount();
				
				if(_dtoRenderers.isEmpty()) {
					// einfach hinzuf�gen, _firstRow, _lastRow setzen
					_dtoRenderers.add(renderer);
					_firstRow = 0;
					_lastRow = 0;
					
					renderer.setLinks();
					
					/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
					 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
					 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
					 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
					 */
					JComponent rowHeaderRow = renderer.getRowHeaderRow(TIME_FORMAT);
					JComponent viewportRow = renderer.getViewportRow();
					
					int nextRowHeight = renderer.getHeight();
					
					rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, nextRowHeight));
					rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, nextRowHeight));
					viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, nextRowHeight));
					
					_rowHeaderPanel.add(rowHeaderRow);
					_viewportPanel.add(viewportRow);
				}
				else {
					// an Position index einen Datensatz einf�gen
					if(index < _firstRow) {
						int height = renderer.getHeight();
						setUpperPanel(_emptyDummyUpperViewportPanel.getSize().height + height);
						_dtoRenderers.add(index, renderer);
						_firstRow++;
						_lastRow++;
						_scrollPane.validate();
						_verticalScrollBar.getModel().setValue(_verticalScrollBar.getModel().getValue() + height);
					}
					else if(index > _lastRow) {
						int height = renderer.getHeight();
						setLowerPanel(_lowerViewportPanel.getSize().height + height);
						try {
							_dtoRenderers.add(index, renderer);
						} catch (IndexOutOfBoundsException e) {
							_dtoRenderers.add( renderer);
						}
					}
					else { // index >= _firstRow && index <= _lastRow
						// erst einf�gen, dann ggf. l�schen
						int position = index - _firstRow;
						
						renderer.setLinks();
						
						/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
						 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
						 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
						 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
						 */
						JComponent rowHeaderRow = renderer.getRowHeaderRow(TIME_FORMAT);
						JComponent viewportRow = renderer.getViewportRow();
						
						int nextRowHeight = renderer.getHeight();
						viewportHeight += nextRowHeight;
						
						rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, nextRowHeight));
						rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, nextRowHeight));
						viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, nextRowHeight));
						
						_rowHeaderPanel.add(rowHeaderRow, position);
						_viewportPanel.add(viewportRow, position);
						_dtoRenderers.add(index, renderer);
						_lastRow++;
						
						// neue letzte zeile ermitteln
						DataTableObjectRenderer firstRenderer = _dtoRenderers.get(_firstRow);
						int firstRowHeight = firstRenderer.getHeight();
						viewportHeight = 0;
						int newLastRow = _firstRow;
						while(upperHeight + viewportHeight < 
								value + _screenHeight + firstRowHeight && newLastRow < _dtoRenderers.size()) {
							DataTableObjectRenderer nextRenderer = _dtoRenderers.get(newLastRow++);
							viewportHeight += nextRenderer.getHeight();
						}
						newLastRow--;
						for(int i = _lastRow; i > newLastRow; i--) { // �berfl�ssige Zeilen l�schen
							DataTableObjectRenderer nextRenderer = _dtoRenderers.get(i);
							lowerHeight += nextRenderer.getHeight();
							_rowHeaderPanel.remove(_rowHeaderPanel.getComponentCount() - 1);
							_viewportPanel.remove(_viewportPanel.getComponentCount() - 1);
							nextRenderer.unsetLinks();
						}
						_lastRow = newLastRow;
						setLowerPanel(lowerHeight);
					}
				}
				_scrollPane.revalidate();
			}
		});
	}
	
	/**
	 * Alle bisherigen Datens�tze werden gel�scht und die neuen werden �bernommen. Die ersten Datens�tze, 
	 * die angezeigt werden k�nnen, werden dargestellt.
	 * 
	 * F�r diese Methode ist nicht klar, ob sie von den Problemen der Methode addDataTableObject
	 * betroffen ist. 
	 * 
	 * @param dataTableObjects
	 *            Liste neuer Datens�tze
	 */
	public void setDataTableObjects(final List<DataTableObject> dataTableObjects) {
		if(getFirstRun()) {
			initHeaderSize();
			setFirstRun(false);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// alle bisherigen Datens�tze l�schen
				

				for(DataTableObjectRenderer renderer : _dtoRenderers) {
					renderer.unsetLinks();
				}
				_dtoRenderers.clear();
				_firstRow = 0;
				_lastRow = -1;
				
				// die Anzeige initialisieren
				setUpperPanel(0);
				while(_viewportPanel.getComponentCount() > 0) {
					_rowHeaderPanel.remove(0);
					_viewportPanel.remove(0);
				}
				_rowHeaderPanel.setSize(new Dimension(_rowHeaderPanel.getSize().width, 0));
				_viewportPanel.setSize(new Dimension(_viewportPanel.getSize().width, 0));
				setLowerPanel(0);
				for(DataTableObject dataTableObject : dataTableObjects) {
					DataTableObjectRenderer renderer = 
						new DataTableObjectRenderer(_headerGrid, dataTableObject, _selectionManager);
					_dtoRenderers.add(renderer);
					
					boolean check = checkRowHeight();
					if(check) {
						_lastRow = _dtoRenderers.indexOf(renderer);
						
						renderer.setLinks();
						/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
						 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
						 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
						 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
						 */
						JComponent rowHeaderRow = renderer.getRowHeaderRow(TIME_FORMAT);
						JComponent viewportRow = renderer.getViewportRow();
						
						int height = renderer.getHeight();
						rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, height));
						rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, height));
						viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, height));
						
						_rowHeaderPanel.add(rowHeaderRow);
						_viewportPanel.add(viewportRow);
						
						_scrollPane.validate();
					}
					else {
						// H�he = alte H�he des Panels + neue H�he
						int lowerHeight = _lowerViewportPanel.getSize().height;
						lowerHeight += renderer.getHeight();
						setLowerPanel(lowerHeight);
						_scrollPane.revalidate();
					}
				}
			}
		});
	}
	
	/**
	 * L�scht einen Datensatz an angegebener Position.
	 * 
	 * @param index
	 *            Position des zu l�schenden Datensatzes
	 */
	public void removeDataTableObject(final int index) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// gibt es diesen Index �berhaupt?
				int upperHeight = _emptyDummyUpperViewportPanel.getSize().height;
				int viewportHeight = _viewportPanel.getSize().height;
				int lowerHeight = _lowerViewportPanel.getSize().height;
				int value = _verticalScrollBar.getModel().getValue();
				_screenHeight = _verticalScrollBar.getVisibleAmount();
				
				if(index >= _firstRow && index <= _lastRow) {
					// welche Zeile l�schen
					
					DataTableObjectRenderer renderer = _dtoRenderers.get(index);
					viewportHeight -= renderer.getHeight();
					
					renderer.unsetLinks();
					_dtoRenderers.remove(index);
					int row = index - _firstRow;
					_rowHeaderPanel.remove(row);
					_viewportPanel.remove(row);
					// ggf. neue hinzuf�gen - aber nur, wenn Datensatz existiert
					if(_dtoRenderers.size() > 0 && _firstRow < _dtoRenderers.size()) {
						DataTableObjectRenderer firstRenderer = _dtoRenderers.get(_firstRow);
						int firstRowHeight = firstRenderer.getHeight();
						while((upperHeight + viewportHeight < 
								value + _screenHeight + firstRowHeight) && (_lastRow < _dtoRenderers.size())) {
							DataTableObjectRenderer lastRenderer = _dtoRenderers.get(_lastRow++);
							lastRenderer.setLinks();

							



							JComponent rowHeaderRow = lastRenderer.getRowHeaderRow(TIME_FORMAT);
							JComponent viewportRow = lastRenderer.getViewportRow();
							
							int lastRowHeight = lastRenderer.getHeight();
							viewportHeight += lastRowHeight;
							lowerHeight -= lastRowHeight;
							
							rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, lastRowHeight));
							rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, lastRowHeight));
							viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, lastRowHeight));

							_rowHeaderPanel.add(rowHeaderRow);
							_viewportPanel.add(viewportRow);
						}
						_lastRow--;
						
						int componentCount = _viewportPanel.getComponentCount();
						if(componentCount != ((_lastRow - _firstRow) + 1)) {
							_debug.error("Anzahl der Komponenten stimmt nicht mit _firstRow und _lastRow �berein!");
						}
					}
				}
				else {
					DataTableObjectRenderer renderer = _dtoRenderers.get(index);
					int height = renderer.getHeight();
					if(index < _firstRow) {
						upperHeight -= height;
						_verticalScrollBar.getModel().setValue(_verticalScrollBar.getModel().getValue() - height);
					}
					else { // index > _lastRow
						lowerHeight -= height;
					}
					_dtoRenderers.remove(index); // Datensatz l�schen
				}
				setUpperPanel(upperHeight);
				setLowerPanel(lowerHeight);
				_scrollPane.validate();
			}
		});
	}
	
	/**
	 * Aktualisiert an angegebener Position den Datensatz.
	 * 
	 * Von dieser Methode ist nicht bekannt, ob sie von den Problemen der Methode addDataTableObject
	 * betroffen ist.
	 * 
	 * @param index
	 *            Position des zu aktualisierenden Datensatzes
	 * @param dataTableObject
	 *            aktueller Datensatz
	 */
	public void update(final int index, final DataTableObject dataTableObject) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(index >= _firstRow && index <= _lastRow) {
					int upperHeight = _emptyDummyUpperViewportPanel.getSize().height;
					int viewportHeight = _viewportPanel.getSize().height;
					int lowerHeight = _lowerViewportPanel.getSize().height;
					int value = _verticalScrollBar.getModel().getValue();
					_screenHeight = _verticalScrollBar.getVisibleAmount();
					
					int position = index - _firstRow;
					
					// entfernen
					_rowHeaderPanel.remove(position);
					_viewportPanel.remove(position);
					
					DataTableObjectRenderer renderer = _dtoRenderers.get(index);
					int height = renderer.getHeight();
					viewportHeight -= height;
					renderer.unsetLinks();
					// einf�gen
					DataTableObjectRenderer newRenderer = new DataTableObjectRenderer(_headerGrid, dataTableObject, _selectionManager);
					_dtoRenderers.set(index, newRenderer);
					newRenderer.setLinks();
					height = newRenderer.getHeight();
					viewportHeight += height;
					
					/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
					 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
					 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
					 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
					 */
					JComponent rowHeaderRow = newRenderer.getRowHeaderRow(TIME_FORMAT);
					JComponent viewportRow = newRenderer.getViewportRow();
					
					rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, height));
					rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, height));
					viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, height));
					
					_rowHeaderPanel.add(rowHeaderRow, position);
					_viewportPanel.add(viewportRow, position);
					
					// ist die H�he auch hoch genug? ist auch genug im Fenster sichtbar?
					DataTableObjectRenderer firstRenderer = _dtoRenderers.get(_firstRow);
					int firstRowHeight = firstRenderer.getHeight();
					if(upperHeight + viewportHeight < value + _screenHeight + firstRowHeight) {
						_lastRow++;
						while((upperHeight + viewportHeight < value + _screenHeight + firstRowHeight) && (_lastRow < _dtoRenderers.size())) {
							DataTableObjectRenderer lastRenderer = _dtoRenderers.get(_lastRow++);
							lastRenderer.setLinks();
							
							/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
							 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
							 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
							 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
							 */
							JComponent rowHeaderRow2 = lastRenderer.getRowHeaderRow(TIME_FORMAT);
							JComponent viewportRow2 = lastRenderer.getViewportRow();
							
							int lastRowHeight = lastRenderer.getHeight();
							viewportHeight += lastRowHeight;
							lowerHeight -= lastRowHeight;
							
							rowHeaderRow2.setPreferredSize(new Dimension(rowHeaderRow2.getPreferredSize().width, lastRowHeight));
							rowHeaderRow2.setMaximumSize(new Dimension(rowHeaderRow2.getMaximumSize().width, lastRowHeight));
							viewportRow2.setMaximumSize(new Dimension(viewportRow2.getMaximumSize().width, lastRowHeight));
							
							_rowHeaderPanel.add(rowHeaderRow2);
							_viewportPanel.add(viewportRow2);
						}
						_lastRow--;
						
						// untere Platzhalter bestimmen
						setLowerPanel(lowerHeight);
					}
					int anzahl = _viewportPanel.getComponentCount();
					if(anzahl != ((_lastRow - _firstRow) + 1)) {
						_debug.error("Anzahl der Komponenten stimmt nicht mit _firstRow und _lastRow �berein!");
					}
					_scrollPane.validate();
				}
				else {
					DataTableObjectRenderer renderer = new DataTableObjectRenderer(_headerGrid, dataTableObject, _selectionManager);
					DataTableObjectRenderer oldRenderer = _dtoRenderers.get(index);
					int diff = renderer.getHeight() - oldRenderer.getHeight();
					_dtoRenderers.set(index, renderer);
					if(index < _firstRow) {
						setUpperPanel(_emptyDummyUpperViewportPanel.getSize().height + diff);
					}
					else { // index > _lastRow
						setLowerPanel(_lowerViewportPanel.getSize().height + diff);
					}
					_scrollPane.validate();
				}
			}
		});
	}
	
	/* ############## Methoden f�r das ScrollPane ############ */
	
	/**
	 * Setzt f�r die oberen Platzhalter des Zeilenheaders und des Viewports die H�he.
	 * 
	 * @param height
	 *            die neue H�he der Platzhalter
	 */
	private void setUpperPanel(final int height) {
		Dimension size = new Dimension(1, height);
		_emptyDummyUpperRowHeaderPanel.setPreferredSize(size);
		_emptyDummyUpperRowHeaderPanel.setSize(size);
		_emptyDummyUpperRowHeaderPanel.setMinimumSize(size);
		_emptyDummyUpperRowHeaderPanel.setMaximumSize(size);
		_emptyDummyUpperRowHeaderPanel.validate();
		
		_emptyDummyUpperViewportPanel.setPreferredSize(size);
		_emptyDummyUpperViewportPanel.setSize(size);
		_emptyDummyUpperViewportPanel.setMinimumSize(size);
		_emptyDummyUpperViewportPanel.setMaximumSize(size);
		_emptyDummyUpperViewportPanel.validate();
	}
	
	/**
	 * Setzt f�r die unteren Platzhalter des Zeilenheaders und des Viewports die H�he.
	 * 
	 * @param height
	 *            die neue H�he der Platzhalter
	 */
	public void setLowerPanel(final int height) {
		Dimension size = new Dimension(1, height);
		_emptyDummyLowerRowHeaderPanel.setPreferredSize(size);
		_emptyDummyLowerRowHeaderPanel.setSize(size);
		_emptyDummyLowerRowHeaderPanel.setMinimumSize(size);
		_emptyDummyLowerRowHeaderPanel.setMaximumSize(size);
		_emptyDummyLowerRowHeaderPanel.validate();
		
		
		_lowerViewportPanel.setPreferredSize(size);
		_lowerViewportPanel.setSize(size);
		_lowerViewportPanel.setMinimumSize(size);
		_lowerViewportPanel.setMaximumSize(size);
		_lowerViewportPanel.validate();
	}
	
	/**
	 * Vergr��ert den unteren Platzhalter um die angebene H�he
	 * 
	 * @param height
	 *            die zu addierende H�he f�r den Platzhalter
	 */
	public void increaseLowerPanel(final int height) {
		setLowerPanel( _lowerViewportPanel.getSize().height + height);
	}
	
	/**
	 * Pr�ft, wieviel Platz die Komponente in der H�he verbraucht und ob sie noch in der ScrollPane 
	 * angezeigt werden kann.
	 * 
	 * @return gibt an, ob sie noch in der ScrollPane angezeigt werden kann
	 */
	private boolean checkRowHeight() {
		_screenHeight = _verticalScrollBar.getVisibleAmount();
		DataTableObjectRenderer renderer = _dtoRenderers.get(0);
		int visibleAmount = _screenHeight + renderer.getHeight();
		
		int usedHeight = _emptyDummyUpperViewportPanel.getSize().height + _viewportPanel.getSize().height;
		usedHeight += _lowerViewportPanel.getSize().height;
		if(usedHeight < visibleAmount) { // ist dann noch platz -> true, sonst false
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Wird ben�tigt, um das Scrollverhalten des vertikalen Scrollbalkens zu steuern. Je nachdem, 
	 * wohin der Scrollbalken verschoben wird, muss der Viewport aktualisiert werden, damit nicht 
	 * zu allen Datens�tzen die Komponenten angezeigt werden m�ssen. Dies spart Speicherplatz.
	 */
	private class VerticalScrollBarAdjustmentListener implements AdjustmentListener {
		
		/* ############## Variablen ############## */
		/** Gibt an, welcher Datensatz der oberste noch sichtbare Datensatz im Fenster ist. */
		private int _mostUpperDataTableObjectIndex;
		
		/** Die H�he des obersten Platzhalters. */
		private int _upperHeight;
		
		/** Die H�he des Viewport im ScrollPane. */
		private int _viewportHeight;
		
		/** Die aktuelle Position des Schiebers im vertikalen Scrollbalken. */
		private int _scrollBarPosition;
		
		/** Die H�he des unteren Platzhalters. */
		private int _lowerHeight;
		
		private final Object _lock = new Object();
		
		/* ############### Methoden der Klasse ############## */
		
		/** Ermittelt anhand des AdjustmentEvents den Index des obersten sichtbaren Datensatzes 
		 * und die H�he f�r den oberen Platzhalter. */
		private void initHeightsAndPosition( AdjustmentEvent e) {
			_scrollBarPosition = e.getValue();
			_screenHeight = _verticalScrollBar.getVisibleAmount();
			_mostUpperDataTableObjectIndex = 0;
			_upperHeight = 0;
			DataTableObjectRenderer renderer = null;
			while(_upperHeight <= _scrollBarPosition) {
				renderer = _dtoRenderers.get(_mostUpperDataTableObjectIndex++);
				_upperHeight += renderer.getHeight();
			}
			if(renderer != null) {
				_mostUpperDataTableObjectIndex--;
				_upperHeight -= renderer.getHeight();
			}
			_viewportHeight = _viewportPanel.getSize().height;
			_lowerHeight = _lowerViewportPanel.getSize().height;
		}
		
		/**
		 * Hier wird gepr�ft, welche Datens�tze gerade im sichtbaren Bereich des ScrollPane 
		 * angezeigt werden k�nnen. Datens�tze, die fehlen, werden hinzugef�gt,
		 * Datens�tze die nicht mehr ben�tigt werden, werden aus der Ansicht entfernt.
		 * 
		 * Etwas genauer: Diese Methode ist die Implementation des AdjustmentListeners, und
		 * mit Ausnahme einer ersten Zeile, werden hier die Zeilen in das Panel gesteckt.
		 * Das Bedeutet, dass das Hinzuf�gen mit addDataTableObject ab der zweiten Zeile
		 * nicht dazu f�hrt, dass Zeilen erscheinen, sondern erst ein AdjustmentEvent.
		 * 
		 * @param e
		 *            Event vom Scrollbalken
		 */
		public void adjustmentValueChanged(AdjustmentEvent e) {
			synchronized(_lock) {
				// falls es keine Daten gibt zum Darstellen ->  mache nichts
				if(_dtoRenderers.isEmpty()) return;
				
				initHeightsAndPosition(e);
				
				if(_mostUpperDataTableObjectIndex >= _firstRow && _mostUpperDataTableObjectIndex <= _lastRow) { 
					// langsam nach unten scrollen: nach oben entschwindende Rows werden entfernt,
					// von unten hinzukommende hinzugef�gt.
					while(_firstRow < _mostUpperDataTableObjectIndex) {
						DataTableObjectRenderer nextRenderer = _dtoRenderers.get(_firstRow);
						_viewportHeight -= nextRenderer.getHeight();
						nextRenderer.unsetLinks();
						_rowHeaderPanel.remove(0);
						_viewportPanel.remove(0);
						_firstRow++;
					}
					setUpperPanel(_upperHeight);
					_firstRow = _mostUpperDataTableObjectIndex;
					DataTableObjectRenderer firstRenderer = _dtoRenderers.get(_firstRow);
					int firstRowHeight = firstRenderer.getHeight();
					_lastRow++;
					
					while((_upperHeight + _viewportHeight < 
							_scrollBarPosition + _screenHeight + firstRowHeight) && (_lastRow < _dtoRenderers.size())) {
						DataTableObjectRenderer lastRenderer = _dtoRenderers.get(_lastRow++);
						lastRenderer.setLinks();
						
						/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
						 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
						 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
						 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
						 */
						JComponent rowHeaderRow = lastRenderer.getRowHeaderRow(TIME_FORMAT);
						JComponent viewportRow = lastRenderer.getViewportRow();
						
						int lastRowHeight = lastRenderer.getHeight();
						_viewportHeight += lastRowHeight;
						_lowerHeight -= lastRowHeight;
						
						rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, lastRowHeight));
						rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, lastRowHeight));
						viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, lastRowHeight));
						
						_rowHeaderPanel.add(rowHeaderRow);
						_viewportPanel.add(viewportRow);
					}
					_lastRow--;
					
					_rowHeaderPanel.revalidate();
					_viewportPanel.revalidate();
					
					// untere Platzhalter bestimmen
					setLowerPanel(_lowerHeight);
					int componentCount = _viewportPanel.getComponentCount();
					if(componentCount != ((_lastRow - _firstRow) + 1)) {
						_debug.error("Anzahl der Komponenten (" + _viewportPanel.getComponentCount() + 
								") stimmt nicht mit _firstRow (" + _firstRow
								+ ") und _lastRow (" + _lastRow + ") �berein!");
					}
				}
				else if(_mostUpperDataTableObjectIndex < _firstRow) { // nach oben scrollen
					setUpperPanel(_upperHeight);
					
					int newFirstRow = _mostUpperDataTableObjectIndex;
					int newLastRow = _mostUpperDataTableObjectIndex;
					
					DataTableObjectRenderer firstRenderer = _dtoRenderers.get(newLastRow);
					int firstRowHeight = firstRenderer.getHeight();
					
					_viewportHeight = 0;
					int index = 0;
					while(_upperHeight + _viewportHeight < 
							_scrollBarPosition + _screenHeight + firstRowHeight && newLastRow <= _lastRow) {
						DataTableObjectRenderer lastRenderer = _dtoRenderers.get(newLastRow);
						if(newLastRow < _firstRow) { // neue Zeilen hinzuf�gen
							lastRenderer.setLinks();
							
							/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
							 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
							 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
							 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
							 */
							JComponent rowHeaderRow = lastRenderer.getRowHeaderRow(TIME_FORMAT);
							JComponent viewportRow = lastRenderer.getViewportRow();
							
							int lastRowHeight = lastRenderer.getHeight();
							_viewportHeight += lastRowHeight;
							
							rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, lastRowHeight));
							rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, lastRowHeight));
							viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, lastRowHeight));
							
							_rowHeaderPanel.add(rowHeaderRow, index);
							_viewportPanel.add(viewportRow, index);
						}
						else {
							_viewportHeight += lastRenderer.getHeight();
						}
						
						newLastRow++;
						index++;
					}
					newLastRow--; // _lastRow
					index--;
					
					// letzte Elemente l�schen
					if(newLastRow < _firstRow) {
						// alles zwischen _firstRow und _lastRow l�schen
						for(int i = _lastRow; i >= _firstRow; i--) {
							DataTableObjectRenderer renderer = _dtoRenderers.get(i);
							renderer.unsetLinks();
							_rowHeaderPanel.remove(_rowHeaderPanel.getComponentCount() - 1);
							_viewportPanel.remove(_viewportPanel.getComponentCount() - 1);
						}
					}
					if(newLastRow >= _firstRow) { // letzten Element l�schen
						for(int i = _lastRow; i > newLastRow; i--) {
							DataTableObjectRenderer renderer = _dtoRenderers.get(i);
							renderer.unsetLinks();
							_rowHeaderPanel.remove(_rowHeaderPanel.getComponentCount() - 1);
							_viewportPanel.remove(_viewportPanel.getComponentCount() - 1);
						}
					}
					_firstRow = newFirstRow;
					_lastRow = newLastRow;
					
					_rowHeaderPanel.validate();
					_viewportPanel.validate();
					
					// untere Platzhalter
					_lowerHeight = 0;
					for(int i = _lastRow + 1, n = _dtoRenderers.size(); i < n; i++) {
						DataTableObjectRenderer renderer = _dtoRenderers.get(i);
						_lowerHeight += renderer.getHeight();
					}
					setLowerPanel(_lowerHeight);
					int anzahl = _viewportPanel.getComponentCount();
					if(anzahl != (_lastRow - _firstRow + 1)) {
						_debug.error("Anzahl der Komponenten stimmt nicht mit _firstRow und _lastRow �berein!");
					}
				}
				else if(_mostUpperDataTableObjectIndex > _lastRow) { // 'schnell' nach unten scrollen
					while(_firstRow <= _lastRow) {
						DataTableObjectRenderer nextRenderer = _dtoRenderers.get(_firstRow);
						_viewportHeight -= nextRenderer.getHeight();
						nextRenderer.unsetLinks();
						_rowHeaderPanel.remove(0);
						_viewportPanel.remove(0);
						_firstRow++;
					}
					_firstRow = _mostUpperDataTableObjectIndex;
					_lastRow = _mostUpperDataTableObjectIndex;
					
					// oberen Platzhalter ermitteln
					setUpperPanel(_upperHeight);
					
					DataTableObjectRenderer firstRenderer = _dtoRenderers.get(_firstRow);
					int firstRowHeight = firstRenderer.getHeight();
					while((_upperHeight + _viewportHeight < 
							_scrollBarPosition + _screenHeight + firstRowHeight) && (_lastRow < _dtoRenderers.size())) {
						DataTableObjectRenderer lastRenderer = _dtoRenderers.get(_lastRow++);
						lastRenderer.setLinks();
						
						/* Kode wie dieser l��t einen Gruseln: die H�hen der beiden Komponeneten sind
						 * voneinander abh�ngig, und was man nicht sieht: die Reihenfolge ist wichtig, 
						 * wenn man irgendetwas an anderen Stellen �ndert, z.B. bei der Behebung eines
						 * Bugs zu Zeilenh�hen bei leeren Arrays! Also Vorsicht hier!
						 */
						JComponent rowHeaderRow = lastRenderer.getRowHeaderRow(TIME_FORMAT);
						JComponent viewportRow = lastRenderer.getViewportRow();
						
						int lastRowHeight = lastRenderer.getHeight();
						_viewportHeight += lastRowHeight;
						
						rowHeaderRow.setPreferredSize(new Dimension(rowHeaderRow.getPreferredSize().width, lastRowHeight));
						rowHeaderRow.setMaximumSize(new Dimension(rowHeaderRow.getMaximumSize().width, lastRowHeight));
						viewportRow.setMaximumSize(new Dimension(viewportRow.getMaximumSize().width, lastRowHeight));
						
						_rowHeaderPanel.add(rowHeaderRow);
						_viewportPanel.add(viewportRow);
						
						_rowHeaderPanel.validate();
						_viewportPanel.validate();
					}
					
					_lastRow--;
					// LowerHeight ermitteln
					_lowerHeight = 0;
					for(int i = _lastRow + 1, n = _dtoRenderers.size(); i < n; i++) {
						DataTableObjectRenderer renderer = _dtoRenderers.get(i);
						_lowerHeight += renderer.getHeight();
					}
					setLowerPanel(_lowerHeight);
					int anzahl = _viewportPanel.getComponentCount();
					if(anzahl != (_lastRow - _firstRow + 1)) {
						_debug.error("Anzahl der Komponenten stimmt nicht mit _firstRow und _lastRow �berein!");
					}
				}
				_scrollPane.validate();
			}
		}
	}
	
	/**
	 * Setzt den Wert des vertikalen Rollbalkens.
	 * 
	 * @param value der neue Wert
	 */
	public void setVerticalScrollBarValue( int value) {
		_verticalScrollBar.setValue(value);
	}
	
	/**
	 * Gibt den Maximalwert des vertikalen Rollbalkens zur�ck.
	 * 
	 * @return gibt den Maximalwert des vertikalen Rollbalkens zur�ck
	 */
	public int getVerticalScrollBarsMaximumValue() {
		return _verticalScrollBar.getMaximum();
	}
	
	/**
	 * Setzt den maximalen Wert des vertikalen Rollbalkens.
	 * 
	 * @param value der neue Wert
	 */
	public void setVerticalScrollBarsMaximumValue( final int value) {
		_verticalScrollBar.setMaximum( value);
	}
	
	/**
	 * Gibt den Ausdehung des Knopfes der vertikalen Rollbalkens zur�ck.
	 * 
	 * @return gibt den Ausdehung des Knopfes der vertikalen Rollbalkens zur�ck
	 */
	public int getVerticalScrollBarsVisibleAmount() {
		return _verticalScrollBar.getVisibleAmount();
	}
	
	/**
	 * Gibt die H�he des sichtbaren Bereich des Viewports zur�ck.
	 * 
	 * @return gibt die H�he des sichtbaren Bereich des Viewports zur�ck
	 */
	public int getVisibleViewPortHeight() {
		return _screenHeight;
	}
	
	/**
	 * Gibt <code>true</code> zur�ck, wenn der horizontale Rolbalken sichtbar ist.
	 * 
	 * @return gibt <code>true</code> zur�ck, wenn der horizontale Rolbalken sichtbar ist
	 */
	public boolean isHorizontalScrollBarVisible() {
		return _scrollPane.getHorizontalScrollBar().isVisible();
	}
	
	/**
	 * Setzt den Modus des horizontalen Rollbalkens.
	 *  
	 * @param horizontalPolicy der neue Modus
	 */
	public void setHorizontalScrollBarPolicy( final int horizontalPolicy) {
		_scrollPane.setHorizontalScrollBarPolicy( horizontalPolicy);
	}
	
}
