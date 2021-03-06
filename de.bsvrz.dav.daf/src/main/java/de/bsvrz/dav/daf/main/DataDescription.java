/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.dav.daf.main;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;

/**
 * Klasse zum Zugriff auf die beschreibenden Eigenschaften eines Datensatzes. Das sind Attributgruppe, Aspekt und Simulationsvariante. Beim Erzeugen von
 * Objekten dieser Klasse durch eine Applikation wird die Simulationsvariante im allgemeinen nicht explizit spezifiziert (Wert {@link
 * #NO_SIMULATION_VARIANT_SET}) und bei der Kommunikation mit dem Datenverteiler automatisch durch den Defaultwert ersetzt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5058 $
 */
public class DataDescription {

	/**
	 * Konstante, die signalisiert, dass die Simulationsvariante nicht explizit vorgegeben wurde, sondern der Defaultwert benutzt werden soll. Der Defaultwert kann
	 * beim Start einer Applikation �ber den Aufrufparameter -simVariante=... vorgegeben werden. Der Defaultwert wird aus den {@link de.bsvrz.dav.daf.main.ClientDavParameters
	 * Verbindungsparametern} (normalerweise <code>0</code>) bestimmt und kann damit �ber Aufrufargumente der Applikation (-simVariante=...) vorgegeben werden.
	 * Siehe dazu auch die Schnittstellenbeschreibung <i>DatenverteilerApplikationsfunktionen-Starter</i>.
	 */
	public static final short NO_SIMULATION_VARIANT_SET = -1;

	private AttributeGroup _attributeGroup;

	private Aspect _aspect;

	private short _simulationVariant;

	/**
	 * Vergleicht die Datenbeschreibung mit einer anderen Datenbeschreibung. Zwei Datenbeschreibungen sind gleich, wenn die Attributgruppen, die Aspekte und die
	 * Simulationsvarianten gleich sind.
	 *
	 * @param other Andere Datenbeschreibung mit der diese Datenbeschreibung verglichen werden soll.
	 *
	 * @return <code>true</code>, wenn die Datenbeschreibungen gleich sind, sonst <code>false</code>.
	 */
	public final boolean equals(Object other) {
		if(this == other) return true;

		if(!(other instanceof DataDescription)) {
			return false;
		}
		DataDescription o = (DataDescription)other;

		return (_attributeGroup == null ? o._attributeGroup == null : _attributeGroup.equals(o._attributeGroup))
		       && (_aspect == null ? o._aspect == null : _aspect.equals(o._aspect)) && (o.getSimulationVariant() == getSimulationVariant());
	}

	/**
	 * Liefert einen Hash-Code f�r das Objekt. Implementierung h�lt sich an die Ratschl�ge in "Bloch, Joshua: Effective Java".
	 *
	 * @return int	den Hash-Code des Objekts
	 */
	public int hashCode() {
		int result = 17;
		if(_attributeGroup != null) result = 37 * result + _attributeGroup.hashCode();
		if(_aspect != null) result = 37 * result + _aspect.hashCode();
		result = 37 * result + (int)getSimulationVariant();
		return result;
	}

	/**
	 * Erzeugt eine neue Datenbeschreibung mit den �bergebenen Werten f�r die Attributgruppe und den Aspekt. Die Simulationsvariante wird auf den Wert {@link
	 * #NO_SIMULATION_VARIANT_SET} gesetzt, was bedeutet, dass der Defaultwert aus den {@link de.bsvrz.dav.daf.main.ClientDavParameters Verbindungsparametern} (normalerweise
	 * <code>0</code>) benutzt werden soll und damit �ber Aufrufargumente der Applikation vorgegeben werden kann. Siehe dazu auch die Schnittstellenbeschreibung
	 * <i>DatenverteilerApplikationsfunktionen-Starter</i>.
	 *
	 * @param attributeGroup Attributgruppe der Datenbeschreibung
	 * @param aspect         Aspekt der Datenbeschreibung
	 */
	public DataDescription(AttributeGroup attributeGroup, Aspect aspect) {
		this(attributeGroup, aspect, (short)NO_SIMULATION_VARIANT_SET);
	}

	/**
	 * Erzeugt eine neue Datenbeschreibung mit den �bergebenen Werten f�r die Attributgruppe, den Aspekt und die Simulationsvariante. Diese Konstruktor-Variante
	 * ist zu Testzwecken und f�r spezielle Applikationen vorgesehen, die die Simulationsvariante explizit vorgeben m�ssen. Eine Applikation, die man mehrfach mit
	 * verschiedenen Simulationsvarianten starten k�nnen soll, sollte die Simulationsvariante nicht selbst spezifizieren, sondern den {@link
	 * #DataDescription(de.bsvrz.dav.daf.main.config.AttributeGroup,Aspect) Konstruktor ohne Simulationsvariante} benutzen.
	 *
	 * @param attributeGroup    Attributgruppe der Datenbeschreibung
	 * @param aspect            Aspekt der Datenbeschreibung
	 * @param simulationVariant Simulationsvariante der Datenbeschreibung
	 */
	public DataDescription(AttributeGroup attributeGroup, Aspect aspect, short simulationVariant) {
		this._attributeGroup = attributeGroup;
		this._aspect = aspect;
		this._simulationVariant = simulationVariant;
	}

	/**
	 * Liefert die Attributgruppe dieser Datenbeschreibung zur�ck.
	 *
	 * @return Attributgruppe dieser Datenbeschreibung
	 */
	public final AttributeGroup getAttributeGroup() {
		return _attributeGroup;
	}

	/**
	 * Liefert den Aspekt dieser Datenbeschreibung zur�ck.
	 *
	 * @return Aspekt dieser Datenbeschreibung
	 */
	public final Aspect getAspect() {
		return _aspect;
	}

	/**
	 * Liefert die Simulationsvariante dieser Datenbeschreibung zur�ck.
	 *
	 * @return Simulationsvariante dieser Datenbeschreibung oder {@link #NO_SIMULATION_VARIANT_SET}, wenn die Simulationsvariante nicht explizit spezifiziert ist.
	 */
	public final short getSimulationVariant() {
		return _simulationVariant;
	}

	/**
	 * Setzt die Simulationsvariante dieser Datenbeschreibung.
	 *
	 * @param simulationVariant Simulationsvariante dieser Datenbeschreibung
	 *
	 * @deprecated Sollte nicht mehr verwendet werden, da die �nderung dieses Objekts zu Problemen f�hren kann, wenn es bereits als Schl�ssel in einer Map
	 *             eingetragen wurde.
	 */
	public final void setSimulationVariant(short simulationVariant) {
		this._simulationVariant = simulationVariant;
	}

	/**
	 * Gibt eine Kopie dieses Objektes mit der gegebenen Attributgruppe und der gegebenen Simulationsvariante und dem �bergebenen Aspekt.
	 *
	 * @param aspect der zu verwendende Aspekt
	 *
	 * @return eine Kopie dieses Objekts mit ausgetauschtem Aspekt
	 */
	public final DataDescription getRedirectedDescription(Aspect aspect) {
		return new DataDescription(_attributeGroup, aspect, _simulationVariant);
	}

	/**
	 * Liefert eine textuelle Beschreibung dieses Objekts zur�ck. Das genaue Format ist nicht festgelegt und kann sich �ndern.
	 *
	 * @return Beschreibung dieses Objekts.
	 */
	public String toString() {
		return "DataDescription{" + (_attributeGroup == null ? "-" : _attributeGroup.getPid()) + ", " + (_aspect == null ? "-" : _aspect.getPid()) + ", "
		       + _simulationVariant + "}";
	}
}
