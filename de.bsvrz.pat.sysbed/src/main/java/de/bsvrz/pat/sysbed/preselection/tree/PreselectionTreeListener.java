/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.pat.sysbed.preselection.tree;

import de.bsvrz.dav.daf.main.config.SystemObject;

import java.util.*;

/**
 * Listener zum Anmelden bei einem Objekt der Klasse {@link PreselectionTree}.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5052 $
 * @see PreselectionTreeListener#setObjects(java.util.Collection)
 */
public interface PreselectionTreeListener {

	/**
	 * Methode zum �bergeben der Systemobjekte (z.B. an das PreselectionLists-Panel)
	 *
	 * @param systemObjects die zu �bergebenden Systemobjekte
	 */
	void setObjects(Collection<SystemObject> systemObjects);
}
