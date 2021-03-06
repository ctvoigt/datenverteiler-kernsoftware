/*
 * Copyright 2008 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.sys.funclib.application.
 * 
 * de.bsvrz.sys.funclib.application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.sys.funclib.application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.sys.funclib.application; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.bsvrz.sys.funclib.application;

import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Implementierung eines UncaughtExceptionHandlers, der bei nicht abgefangenen Exceptions und Errors entsprechende Ausgaben macht und im Falle eines Errors den
 * Prozess terminiert.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5923 $
 */
public class ExitOnErrorUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Debug _debug = Debug.getLogger();

	/** Speicherreserve, die freigegeben wird, wenn ein Error auftritt, damit die Ausgaben nach einem OutOfMemoryError funktionieren */
	private volatile byte[] _reserve = new byte[20000];

	public void uncaughtException(Thread t, Throwable e) {
		if(e instanceof Error) {
			// Speicherreserve freigeben, damit die Ausgaben nach einem OutOfMemoryError funktionieren
			_reserve = null;
			try {
				System.err.println("Schwerwiegender Laufzeitfehler: Ein Thread hat sich wegen eines Errors beendet, Prozess wird terminiert");
				System.err.println(t);
				e.printStackTrace(System.err);
				_debug.error("Schwerwiegender Laufzeitfehler: " + t + " hat sich wegen eines Errors beendet, Prozess wird terminiert", e);
			}
			catch(Throwable ignored) {
				// Weitere Fehler w�hrend der Ausgaben werden ignoriert, damit folgendes exit() auf jeden Fall ausgef�hrt wird.
			}
			System.exit(1);
		}
		else {
			System.err.println("Laufzeitfehler: Ein Thread hat sich wegen einer Exception beendet:");
			System.err.println(t);
			e.printStackTrace(System.err);
			_debug.warning("Laufzeitfehler: " + t + " hat sich wegen einer Exception beendet", e);
		}
	}
}
