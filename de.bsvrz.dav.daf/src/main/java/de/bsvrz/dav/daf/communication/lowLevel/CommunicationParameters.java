/*
 * Copyright 2007 by Kappich Systemberatung Aachen
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

package de.bsvrz.dav.daf.communication.lowLevel;

/**
 * Speichert die Parameter f�r die Kommunikation zwischen Applikation und Datenverteiler. Darin enthalten sind das Timeout zum Senden und Empfangen von
 * KeepAlive-Telegrammen, der F�llgrad des Sendepuffers, die Zeit zwischen zwei Durchsatzpr�fungen und der minimale Verbindungsdurchsatz.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5047 $
 */
public class CommunicationParameters {

	/** Das KeepAlive-Timeout beim Empfang von Telegrammen. */
	private long _sendKeepAliveTimeout;

	/** Das Timeout zum Senden von KeepAlive-Telegrammen. */
	private long _receiveKeepAliveTimeout;

	/** F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird. */
	private float _throughputControlSendBufferFactor;

	/** Die Zeit zwichen zwei Durchsatzpr�fungen in Millisekunden */
	private long _throughputControlInterval;

	/** Der minimale Verbindungsdurchsatz. */
	private int _minimumThroughput;


	public CommunicationParameters() {
	}

	/**
	 * Bestimmt das Timeout zum Senden von KeepAlive-Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu verwendenden
	 * Wert festlegt.
	 *
	 * @return Vorschlag f�r das Timeout zum Senden von KeepAlive-Telegrammen in Millisekunden.
	 */
	public long getSendKeepAliveTimeout() {
		return _sendKeepAliveTimeout;
	}

	/**
	 * Setzt das Timeout zum Senden von KeepAlive-Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu verwendenden
	 * Wert festlegt.
	 *
	 * @param timeout Vorschlag f�r das Timeout zum Senden von KeepAlive-Telegrammen in Millisekunden.
	 */
	public void setSendKeepAliveTimeout(long timeout) {
		if(timeout > 0) {
			_sendKeepAliveTimeout = timeout;
		}
		else {
			throw new IllegalArgumentException("Der angegebene Wert ist nicht erlaubt: " + timeout);
		}
	}

	/**
	 * Bestimmt das KeepAlive-Timeout beim Empfang von Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu
	 * verwendenden Wert festlegt.
	 *
	 * @return Vorschlag f�r das KeepAlive-Timeout beim Empfang von Telegrammen in Millisekunden.
	 */
	public long getReceiveKeepAliveTimeout() {
		return _receiveKeepAliveTimeout;
	}

	/**
	 * Setzt das KeepAlive-Timeout beim Empfang von Telegrammen. Der Wert dient als Vorschlag f�r die Verhandlung mit dem Datenverteiler, der den zu verwendenden
	 * Wert festlegt.
	 *
	 * @param timeout Vorschlag f�r das KeepAlive-Timeout beim Empfang von Telegrammen in Millisekunden.
	 */
	public void setReceiveKeepAliveTimeout(long timeout) {
		if(timeout > 0) {
			_receiveKeepAliveTimeout = timeout;
		}
		else {
			throw new IllegalArgumentException("Der angegebene Wert ist nicht erlaubt: " + timeout);
		}
	}

	/**
	 * Bestimmt den F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird.
	 *
	 * @return F�llgrad des Sendepuffers als Wert zwischen 0 und 1.
	 */
	public float getThroughputControlSendBufferFactor() {
		return _throughputControlSendBufferFactor;
	}

	/**
	 * Definiert den F�llgrad des Sendepuffers bei dem die Durchsatzpr�fung gestartet wird.
	 *
	 * @param sendBufferFactor F�llgrad des Sendepuffers als Wert zwischen 0 und 1.
	 */
	public void setThroughputControlSendBufferFactor(float sendBufferFactor) {
		_throughputControlSendBufferFactor = sendBufferFactor;
	}

	/**
	 * Bestimmt die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung.
	 *
	 * @return Intervalldauer in Millisekunden.
	 */
	public long getThroughputControlInterval() {
		return _throughputControlInterval;
	}

	/**
	 * Setzt die Intervalldauer f�r die Durchsatzmessung bei aktivierter Durchsatzpr�fung.
	 *
	 * @param interval Intervalldauer in Millisekunden.
	 */
	public void setThroughputControlInterval(long interval) {
		_throughputControlInterval = interval;
	}

	/**
	 * Bestimmt den minimal erlaubten Verbindungsdurchsatz bei aktivierter Durchsatzpr�fung.
	 *
	 * @return Mindestdurchsatz in Byte pro Sekunde.
	 */
	public int getMinimumThroughput() {
		return _minimumThroughput;
	}

	/**
	 * Setzt den minimal erlaubten Verbindungsdurchsatz bei aktivierter Durchsatzpr�fung.
	 *
	 * @param throughput Mindestdurchsatz in Byte pro Sekunde.
	 */
	public void setMinimumThroughput(int throughput) {
		_minimumThroughput = throughput;
	}
}
