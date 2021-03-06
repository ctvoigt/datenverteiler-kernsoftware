/*
 * Copyright 2009 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2004 by Kappich+Kni� Systemberatung, Aachen
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

package de.bsvrz.dav.dav.communication.davProtocol;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.ConnectionInterface;
import de.bsvrz.dav.daf.communication.lowLevel.HighLevelCommunicationCallbackInterface;
import de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunicationInterface;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ClosingTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.RoutingUpdate;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TerminateOrderTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterAuthentificationAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterAuthentificationRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterAuthentificationTextAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterAuthentificationTextRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterBestWayUpdate;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterComParametersAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterComParametersRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterDataSubscription;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterDataSubscriptionReceipt;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterDataUnsubscription;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterListsDeliveryUnsubscription;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterListsSubscription;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterListsUnsubscription;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterListsUpdate;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterProtocolVersionAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterProtocolVersionRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterTelegramTimeAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TransmitterTelegramTimeRequest;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.dav.communication.accessControl.AccessControlUtil;
import de.bsvrz.dav.dav.main.AuthentificationComponent;
import de.bsvrz.dav.dav.main.BestWayManager;
import de.bsvrz.dav.dav.main.ConnectionsManager;
import de.bsvrz.dav.dav.main.ServerConnectionProperties;
import de.bsvrz.dav.dav.main.SubscriptionsFromDavStorage;
import de.bsvrz.dav.dav.main.SubscriptionsFromRemoteStorage;
import de.bsvrz.dav.dav.main.SubscriptionsManager;
import de.bsvrz.dav.dav.main.Transmitter;
import de.bsvrz.dav.dav.communication.accessControl.AccessControlPlugin;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;


/**
 * Diese Klasse stellt die Funktionalit�ten f�r die Kommunikation zwischen zwei Datenverteilern zur Verf�gung. Hier wird die Verbindung zwischen zwei DAV
 * aufgebaut, sowie die Authentifizierung durchgef�hrt.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8333 $
 */
public class T_T_HighLevelCommunication implements T_T_HighLevelCommunicationInterface, HighLevelCommunicationCallbackInterface {

	private static final Debug _debug = Debug.getLogger();

	/** Die Id des �ber diesen Kanal verbundenen Datenverteiler */
	private long _connectedTransmitterId;

	/** Die Id des Remotebenutzers */
	private long _remoteUserId;

	/** Die erste Ebene der Kommunikation */
	private LowLevelCommunicationInterface _lowLevelCommunication;

	/** Die Eigenschaften dieser Verbindung */
	private ServerConnectionProperties _properties;

	/** Die unterst�tzten Versionen des Datenverteilers */
	private int _versions[];

	/** Die Version, mit der die Kommunikation erfolgt */
	private int _version;

	/** Die Verwaltung der Anmelde Komponenten */
	private SubscriptionsManager _subscriptionsManager;

	/** Die Verwaltung der Datenverteiler Verbindungen */
	private ConnectionsManager _connectionsManager;

	/** Die Anmeldekomponente diese Verbindung */
	private SubscriptionsFromDavStorage _subscriptionsFromDavStorage;

	/** Die Authentifizierungskomponente */
	private AuthentificationComponent _authentificationComponent;

	/** Tempor�re Liste der Systemtelegramme f�r interne Synchronisationszwecke. */
	private LinkedList<DataTelegram> _syncSystemTelegramList;

	/** Tempor�re Liste der Telegramme, die vor die Initialisierung eingetroffen sind. */
	private LinkedList<DataTelegram> _fastTelegramsList;

	/** Gewichtung dieser Verbindung */
	private short _weight;

	/** Signalisiert ob die Initialisierungsphase abgeschlossen ist */
	private boolean _initComplete = false;

	/** Die Information ob auf die Konfiguration gewartet werden muss. */
	private boolean _waitForConfiguration;

	/** Objekt zur internen Synchronization */
	private Integer _sync;

	/** Objekt zur internen Synchronization */
	private Integer _authentificationSync;

	/** Legt fest ob eine gegen Authentifizierung notwendig ist. */
	private boolean _isAcceptedConnection;

	/** Signalisiert dass diese Verbindung terminiert ist */
	private volatile boolean _closed = false;

	private Object _closedLock = new Object();

	/** Verwaltung der g�nstigsten Wege zu anderen Datenverteilern */
	private BestWayManager _bestWayManager;

	/** Benutzername mit dem sich dieser Datenverteiler beim anderen Datenverteiler authentifizieren soll */
	private String _authentifyAsUser;

	/** Passwort des Benutzers mit dem sich dieser Datenverteiler beim anderen Datenverteiler authentifizieren soll */
	private String _authentifyWithPassword;

	/** Installierte PlugIn-Filter, die zur Zugriffsrechtepr�fung bestimmte ATGUs filtern */
	private final Map<Long, List<AccessControlPlugin>> _pluginFilters;

	private final AccessControlUtil _accessControlUtil;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param properties             Eigenschaften dieser Verbindung
	 * @param subscriptionsManager   Anmeldungsverwaltung
	 * @param connectionsManager     Verbindungsverwaltung
	 * @param bestWayManager         Verwaltung der g�nstigsten Wege zu anderen Datenverteilern
	 * @param weight                 Gewichtung dieser Verbindung
	 * @param waitForConfiguration   true: auf die KOnfiguration muss gewartet werden, false: Konfiguration ist vorhanden
	 * @param authentifyAsUser       Benutzername mit dem sich dieser Datenverteiler beim anderen Datenverteiler authentifizieren soll
	 * @param authentifyWithPassword Passwort des Benutzers mit dem sich dieser Datenverteiler beim anderen Datenverteiler authentifizieren soll
	 */
	public T_T_HighLevelCommunication(
			ServerConnectionProperties properties,
			SubscriptionsManager subscriptionsManager,
			ConnectionsManager connectionsManager,
			final BestWayManager bestWayManager,
			short weight,
			boolean waitForConfiguration,
			final String authentifyAsUser,
			final String authentifyWithPassword) {
		_authentifyWithPassword = authentifyWithPassword;
		_authentifyAsUser = authentifyAsUser;
		_bestWayManager = bestWayManager;
		_connectedTransmitterId = -1;
		_versions = new int[1];
		_versions[0] = 2;
		_weight = weight;
		_properties = properties;
		_lowLevelCommunication = _properties.getLowLevelCommunication();
		_subscriptionsManager = subscriptionsManager;
		_connectionsManager = connectionsManager;
		_pluginFilters = connectionsManager.getPluginFilterMap();		
		_authentificationComponent = _properties.getAuthentificationComponent();
		_syncSystemTelegramList = new LinkedList<DataTelegram>();
		_fastTelegramsList = new LinkedList<DataTelegram>();
		_waitForConfiguration = waitForConfiguration;
		_sync = hashCode();
		_authentificationSync = hashCode();
		_isAcceptedConnection = true;
		_lowLevelCommunication.setHighLevelComponent(this);
		_accessControlUtil = new AccessControlUtil();
	}

	/**
	 * Diese Methode wird von der Verbindungsverwaltung aufgerufen, um eine logische Verbindung zwischen zwei Datenverteilern herzustellen. Zun�chst wird die
	 * Protokollversion verhandelt. In einem Systemtelegramm ?TransmitterProtocolVersionRequest? werden die unterst�tzten Versionen �ber die Telegrammverwaltung an
	 * den zweiten Datenverteiler gesendet. Auf die Antwort wird eine gewisse Zeit gewartet (maximale Wartezeit auf synchrone Antworten). Wenn die Antwort
	 * innerhalb diese Zeit nicht angekommen bzw. keine der Protokollversionen vom anderen Datenverteiler unterst�tzt wird, wird eine CommunicationErrorAusnahme
	 * erzeugt. <br>Danach erfolgt die Authentifizierung: �ber die Telegrammverwaltung wird ein Telegramm? TransmitterAuthentificationTextRequest? zum anderen
	 * Datenverteiler gesendet, um einen Schl�ssel f�r die Authentifizierung anzufordern. Die ID des sendenden Datenverteilers wird den ServerConnectionProperties
	 * entnommen. Auf die Antwort ?TransmitterAuthentificationTextAnswer? wird eine gewisse Zeit gewartet (maximale Wartezeit auf synchrone Antworten). Wenn die
	 * Antwort nicht innerhalb dieser Zeit angekommen ist, wird eine CommunicationError-Ausnahme erzeugt. Das Passwort, welches in den ServerConnectionProperties
	 * spezifiziert ist, wird mit diesem Schl�ssel und dem spezifizierten Authentifizierungsverfahren verschl�sselt. Aus dem Authentifizierungsverfahrennamen, dem
	 * verschl�sselten Passwort und dem Benutzernamen wird ein ?TransmitterAuthentificationRequest?-Telegramm gebildet und mittels Telegrammverwaltung zum anderen
	 * Datenverteiler gesendet. Auf die Antwort ?TransmitterAuthentificationAnswer? wird eine gewisse Zeit gewartet (maximale Wartezeit auf synchrone Antworten).
	 * Wenn die Antwort nicht innerhalb dieser Zeit angekommen ist oder konnte die Authentifizierung nicht erfolgreich abgeschlossen werden, so wird eine
	 * CommunicationError-Ausnahme erzeugt <br>Danach geht diese Methode geht in den Wartezustand, bis der andere Datenverteiler sich in umgekehrter Richtung auch
	 * erfolgreich authentifiziert hat. Dabei durchl�uft der andere Datenverteiler das gleiche Prozedere wie zuvor beschrieben. <br>Im n�chsten Schritt verhandeln
	 * die Datenverteiler die Keep-alive-Parameter und die Durchsatzpr�fungsparameter (Verbindungsparameter). Ein ?TransmitterComParametersRequest? wird zum
	 * anderen Datenverteiler gesendet. Auch hier wird eine gewisse Zeit auf die Antwort ?TransmitterComParametersAnswer? gewartet (maximale Wartezeit auf
	 * synchrone Antworten). Wenn die Antwort nicht innerhalb dieser Zeit angekommen ist, wird eine CommunicationError-Ausnahme erzeugt. Sonst ist der
	 * Verbindungsaufbau erfolgreich abund der Austausch von Daten kann sicher durchgef�hrt werden.
	 *
	 * @throws CommunicationError , wenn bei der initialen Kommunikation mit dem Datenverteiler Fehler aufgetreten sind
	 */
	public final void connect() throws CommunicationError {
		_syncSystemTelegramList.clear();
		_isAcceptedConnection = false;
		// Protokollversion verhandeln
		TransmitterProtocolVersionRequest protocolVersionRequest = new TransmitterProtocolVersionRequest(_versions);
		sendTelegram(protocolVersionRequest);

		TransmitterProtocolVersionAnswer protocolVersionAnswer = (TransmitterProtocolVersionAnswer)waitForAnswerTelegram(
				DataTelegram.TRANSMITTER_PROTOCOL_VERSION_ANSWER_TYPE, "Antwort auf Verhandlung der Protokollversionen"
		);
		_version = protocolVersionAnswer.getPreferredVersion();
		int i = 0;
		for(; i < _versions.length; ++i) {
			if(_version == _versions[i]) {
				break;
			}
		}
		if(i >= _versions.length) {
			throw new CommunicationError("Der Datenverteiler unterst�zt keine der gegebenen Versionen.\n");
		}

		_remoteUserId = 0;

		authentify();

		synchronized(_authentificationSync) {
			try {
				while(_remoteUserId == 0) {
					if(_closed) return;
					_authentificationSync.wait(1000);
				}
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				return;
			}
		}
		if(_remoteUserId < 0) return;

		// Timeouts Parameter verhandeln
		TransmitterComParametersRequest comParametersRequest = new TransmitterComParametersRequest(
				_properties.getKeepAliveSendTimeOut(), _properties.getKeepAliveReceiveTimeOut()
		);
		sendTelegram(comParametersRequest);
		TransmitterComParametersAnswer comParametersAnswer = (TransmitterComParametersAnswer)waitForAnswerTelegram(
				DataTelegram.TRANSMITTER_COM_PARAMETER_ANSWER_TYPE, "Antwort auf Verhandlung der Kommunikationsparameter"
		);
		_lowLevelCommunication.updateKeepAliveParameters(
				comParametersAnswer.getKeepAliveSendTimeOut(), comParametersAnswer.getKeepAliveReceiveTimeOut()
		);
	}

	/** @return Liefert <code>true</code> zur�ck, falls die Verbindung geschlossen wurde, sonst <code>false</code>. */
	boolean isClosed() {
		return _closed;
	}


	private DataTelegram waitForAnswerTelegram(final byte telegramType, final String descriptionOfExpectedTelegram) throws CommunicationError {
		long waitingTime = 0;
		long startTime = System.currentTimeMillis();
		long sleepTime = 10;
		final String expected = (" Erwartet wurde: " + descriptionOfExpectedTelegram);
		while(waitingTime < CommunicationConstant.MAX_WAITING_TIME_FOR_SYNC_RESPONCE) {
			try {
				synchronized(_syncSystemTelegramList) {
					if(_closed) throw new CommunicationError("Verbindung terminiert." + expected);
					_syncSystemTelegramList.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					ListIterator<DataTelegram> iterator = _syncSystemTelegramList.listIterator(0);
					while(iterator.hasNext()) {
						final DataTelegram telegram = iterator.next();
						if(telegram != null) {
							if(telegram.getType() == telegramType) {
								iterator.remove();
								return telegram;
							}
							else {
								System.out.println(telegram.parseToString());
							}
						}
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				throw new CommunicationError("Interrupt." + expected);
			}
		}
		throw new CommunicationError("Der Datenverteiler antwortet nicht." + expected);
	}


	public final long getTelegrammTime(final long maxWaitingTime) throws CommunicationError {
		long time = System.currentTimeMillis();
		TransmitterTelegramTimeRequest telegramTimeRequest = new TransmitterTelegramTimeRequest(time);
		sendTelegram(telegramTimeRequest);

		TransmitterTelegramTimeAnswer telegramTimeAnswer = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < maxWaitingTime) {
			try {
				synchronized(_syncSystemTelegramList) {
					if(_closed) throw new CommunicationError("Verbindung terminiert. Erwartet wurde: Antwort auf eine Telegrammlaufzeitermittlung");
					_syncSystemTelegramList.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					ListIterator<DataTelegram> iterator = _syncSystemTelegramList.listIterator(0);
					while(iterator.hasNext()) {
						final DataTelegram telegram = iterator.next();
						if((telegram != null) && (telegram.getType() == DataTelegram.TRANSMITTER_TELEGRAM_TIME_ANSWER_TYPE)) {
							if(((TransmitterTelegramTimeAnswer)telegram).getTelegramStartTime() == time) {
								telegramTimeAnswer = (TransmitterTelegramTimeAnswer)telegram;
								iterator.remove();
								break;
							}
						}
					}
					if(telegramTimeAnswer != null) {
						break;
					}
				}
				waitingTime = System.currentTimeMillis() - startTime;
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
				throw new CommunicationError("Thread wurde unterbrochen.", ex);
			}
		}
		if(telegramTimeAnswer == null) {
			return -1;
		}
		return telegramTimeAnswer.getRoundTripTime();
	}

	public final long getRemoteNodeId() {
		return _connectedTransmitterId;
	}

	public final int getThroughputResistance() {
		return _weight;
	}

	public final void sendRoutingUpdate(RoutingUpdate routingUpdates[]) {
		if(routingUpdates == null) {
			throw new IllegalArgumentException("Argument ist null");
		}
		sendTelegram(new TransmitterBestWayUpdate(routingUpdates));
	}

	public final long getRemoteUserId() {
		return _remoteUserId;
	}

	public final long getId() {
		return _connectedTransmitterId;
	}

	/**
	 * Gibt die Information zur�ck, ob diese Verbindung von dem anderen Datenverteiler akzeptiert wurde.
	 *
	 * @return true: verbindung wurde akzeptiert, false: Verbindung wurde nicht akzeptiert.
	 */
	public final boolean isAcceptedConnection() {
		return _isAcceptedConnection;
	}

	/** @return  */
	public final String getRemoteAdress() {
		if(_lowLevelCommunication == null) {
			return null;
		}
		ConnectionInterface connection = _lowLevelCommunication.getConnectionInterface();
		if(connection == null) {
			return null;
		}
		return connection.getMainAdress();
	}


	/**
	 * Diese Methode gibt die Subadresse des Kommunikationspartners zur�ck.
	 *
	 * @return die Subadresse des Kommunikationspartners
	 */
	public final int getRemoteSubadress() {
		if(_lowLevelCommunication == null) {
			return -1;
		}
		ConnectionInterface connection = _lowLevelCommunication.getConnectionInterface();
		if(connection == null) {
			return -1;
		}
		return connection.getSubAdressNumber();
	}

	public void continueAuthentification() {
		synchronized(_sync) {
			_waitForConfiguration = false;
			_sync.notify();
		}
	}

	public void terminate(boolean error, String message) {
		final DataTelegram terminationTelegram;
		if(error) {
			terminationTelegram = new TerminateOrderTelegram(message);
		}
		else {
			terminationTelegram = new ClosingTelegram();
		}
		terminate(error, message, terminationTelegram);
	}

	public final void terminate(boolean error, String message, DataTelegram terminationTelegram) {
		synchronized(_closedLock) {
			if(_closed) return;
			_closed = true;
		}
		synchronized(this) {
			String debugMessage = "Verbindung zum Datenverteiler " + getId() + " wird terminiert. Ursache: " + message;
			if(error) {
				_debug.error(debugMessage);
			}
			else {
				_debug.info(debugMessage);
			}
			if(_lowLevelCommunication != null) {
				_lowLevelCommunication.disconnect(error, message, terminationTelegram);
			}
			if(_connectionsManager != null) {
				_connectionsManager.unsubscribeConnection(this);
			}
			if((_subscriptionsManager != null) && (_subscriptionsFromDavStorage != null)) {
				_subscriptionsManager.remove(_subscriptionsFromDavStorage);
			}
		}
	}


	public void disconnected(boolean error, final String message) {
		terminate(error, message);
	}

	public void updateConfigData(SendDataObject receivedData) {
		throw new UnsupportedOperationException("updateConfigData nicht implementiert");
	}

	public void sendTelegram(DataTelegram telegram) {
		if(Transmitter._debugLevel > 5) System.err.println("T_T  -> " + telegram.toShortDebugString());
		_lowLevelCommunication.send(telegram);
	}

	public void sendTelegrams(DataTelegram[] telegrams) {
		if(Transmitter._debugLevel > 5) {
			for(int i = 0; i < telegrams.length; i++) {
				DataTelegram telegram = telegrams[i];
				System.err.println("T_T  -> " + telegram.toShortDebugString());
			}
		}
		_lowLevelCommunication.send(telegrams);
	}

	public final SubscriptionsFromRemoteStorage getSubscriptionsFromRemoteStorage() {
		return _subscriptionsFromDavStorage;
	}


	public void update(DataTelegram telegram) {
		if(Transmitter._debugLevel > 5) {
			System.err.println("T_T <-  " + (telegram == null ? "null" : telegram.toShortDebugString()));
		}
		if(telegram == null) {
			return;
		}
		switch(telegram.getType()) {
			case DataTelegram.TRANSMITTER_PROTOCOL_VERSION_REQUEST_TYPE: {
				TransmitterProtocolVersionRequest protocolVersionRequest = (TransmitterProtocolVersionRequest)telegram;
				int version = getPrefferedVersion(protocolVersionRequest.getVersions());
				TransmitterProtocolVersionAnswer protocolVersionAnswer = new TransmitterProtocolVersionAnswer(version);
				sendTelegram(protocolVersionAnswer);
				break;
			}
			case DataTelegram.TRANSMITTER_PROTOCOL_VERSION_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.TRANSMITTER_AUTHENTIFICATION_TEXT_REQUEST_TYPE: {
				TransmitterAuthentificationTextRequest authentificationTextRequest = (TransmitterAuthentificationTextRequest)telegram;
				if(_waitForConfiguration) {
					synchronized(_sync) {
						try {
							while(_waitForConfiguration) {
								if(_closed) return;
								_sync.wait(1000);
							}
						}
						catch(InterruptedException ex) {
							ex.printStackTrace();
							return;
						}
					}
				}
				final long remoteTransmitterId = authentificationTextRequest.getTransmitterId();
				_debug.info("Datenverteiler " + remoteTransmitterId + " m�chte sich authentifizieren");
				final T_T_HighLevelCommunication transmitterConnection;
				synchronized(_connectionsManager) {
					transmitterConnection = _connectionsManager.getTransmitterConnection(remoteTransmitterId);
				}
				if(transmitterConnection != null) {
					final String message = "Neue Verbindung zum Datenverteiler " + remoteTransmitterId
					                       + " wird terminiert, weil noch eine andere Verbindung zu diesem Datenverteiler besteht.";
					_debug.warning(message);
					terminate(true, message);
					return;
				}
				_connectedTransmitterId = remoteTransmitterId;
				_lowLevelCommunication.setRemoteName("DAV " + _connectedTransmitterId);
				_weight = _connectionsManager.getWeight(_connectedTransmitterId);
				_authentifyAsUser = _connectionsManager.getUserNameForAuthentification(_connectedTransmitterId);
				_authentifyWithPassword = _connectionsManager.getPasswordForAuthentification(_connectedTransmitterId);
				String text = _authentificationComponent.getAuthentificationText(Long.toString(_connectedTransmitterId));
				TransmitterAuthentificationTextAnswer authentificationTextAnswer = new TransmitterAuthentificationTextAnswer(text);
				sendTelegram(authentificationTextAnswer);
				break;
			}
			case DataTelegram.TRANSMITTER_AUTHENTIFICATION_TEXT_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.TRANSMITTER_AUTHENTIFICATION_REQUEST_TYPE: {
				TransmitterAuthentificationRequest authentificationRequest = (TransmitterAuthentificationRequest)telegram;
				String userName = authentificationRequest.getUserName();
				try {
					_remoteUserId = _connectionsManager.isValidUser(
							userName,
							authentificationRequest.getUserPassword(),
							_authentificationComponent.getAuthentificationText(Long.toString(_connectedTransmitterId)),
							_authentificationComponent.getAuthentificationProcess(),
							""
					);
					if(_remoteUserId > -1) {
						_debug.info("Datenverteiler " + _connectedTransmitterId + " hat sich als '" + userName + "' erfolgreich authentifiziert");
						TransmitterAuthentificationAnswer authentificationAnswer = new TransmitterAuthentificationAnswer(
								true, _properties.getDataTransmitterId()
						);
						sendTelegram(authentificationAnswer);
						synchronized(_authentificationSync) {
							_authentificationSync.notifyAll();
						}
						if(_isAcceptedConnection) {
							Runnable runnable = new Runnable() {
								public void run() {
									try {
										authentify();
									}
									catch(CommunicationError ex) {
										ex.printStackTrace();
									}
								}
							};
							Thread thread = new Thread(runnable);
							thread.start();
						}
					}
					else {
						synchronized(_authentificationSync) {
							_authentificationSync.notifyAll();
						}
						_debug.info("Datenverteiler " + _connectedTransmitterId + " hat vergeblich versucht sich als '" + userName + "' zu authentifizieren");
						TransmitterAuthentificationAnswer authentificationAnswer = new TransmitterAuthentificationAnswer(false, -1);
						sendTelegram(authentificationAnswer);
					}
				}
				catch(ConfigurationException ex) {
					ex.printStackTrace();
					terminate(
							true, "Fehler w�hrend der Authentifizierung eines anderen Datenverteilers beim Zugriff auf die Konfiguration: " + ex.getMessage()
					);
					return;
				}
				break;
			}
			case DataTelegram.TRANSMITTER_AUTHENTIFICATION_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.TRANSMITTER_COM_PARAMETER_REQUEST_TYPE: {
				TransmitterComParametersRequest comParametersRequest = (TransmitterComParametersRequest)telegram;
				long keepAliveSendTimeOut = comParametersRequest.getKeepAliveSendTimeOut();
				if(keepAliveSendTimeOut < 5000) keepAliveSendTimeOut = 5000;
				long keepAliveReceiveTimeOut = comParametersRequest.getKeepAliveReceiveTimeOut();
				if(keepAliveReceiveTimeOut < 6000) keepAliveReceiveTimeOut = 6000;

				TransmitterComParametersAnswer comParametersAnswer = null;
//				if(keepAliveSendTimeOut < keepAliveReceiveTimeOut) {
//					long tmp = keepAliveSendTimeOut;
//					keepAliveSendTimeOut = keepAliveReceiveTimeOut;
//					keepAliveReceiveTimeOut = tmp;
//				}
				comParametersAnswer = new TransmitterComParametersAnswer(keepAliveSendTimeOut, keepAliveReceiveTimeOut);
				sendTelegram(comParametersAnswer);
				_lowLevelCommunication.updateKeepAliveParameters(keepAliveSendTimeOut, keepAliveReceiveTimeOut);
				completeInitialisation();
				break;
			}
			case DataTelegram.TRANSMITTER_COM_PARAMETER_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.TRANSMITTER_TELEGRAM_TIME_REQUEST_TYPE: {
				TransmitterTelegramTimeRequest telegramTimeRequest = (TransmitterTelegramTimeRequest)telegram;
				sendTelegram(new TransmitterTelegramTimeAnswer(telegramTimeRequest.getTelegramRequestTime()));
				break;
			}
			case DataTelegram.TRANSMITTER_TELEGRAM_TIME_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.TRANSMITTER_DATA_SUBSCRIPTION_TYPE: {
				if(_initComplete) {
					TransmitterDataSubscription subscription = (TransmitterDataSubscription)telegram;
					_connectionsManager.handleTransmitterSubscription(this, subscription);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_DATA_SUBSCRIPTION_RECEIPT_TYPE: {
				if(_initComplete) {
					TransmitterDataSubscriptionReceipt receipt = (TransmitterDataSubscriptionReceipt)telegram;
					_connectionsManager.handleTransmitterSubscriptionReceip(this, receipt);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_DATA_UNSUBSCRIPTION_TYPE: {
				if(_initComplete) {
					TransmitterDataUnsubscription unsubscription = (TransmitterDataUnsubscription)telegram;
					_connectionsManager.handleTransmitterUnsubscription(this, unsubscription);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_BEST_WAY_UPDATE_TYPE: {
				if(_initComplete) {
					TransmitterBestWayUpdate transmitterBestWayUpdate = (TransmitterBestWayUpdate)telegram;
					synchronized(_connectionsManager) {
						_bestWayManager.update(this, transmitterBestWayUpdate);
					}
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_LISTS_SUBSCRIPTION_TYPE: {
				if(_initComplete) {
					TransmitterListsSubscription transmitterListsSubscription = (TransmitterListsSubscription)telegram;
					_connectionsManager.handleListsSubscription(this, transmitterListsSubscription);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_LISTS_UNSUBSCRIPTION_TYPE: {
				if(_initComplete) {
					TransmitterListsUnsubscription transmitterListsUnsubscription = (TransmitterListsUnsubscription)telegram;
					_connectionsManager.handleListsUnsubscription(this, transmitterListsUnsubscription);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_LISTS_DELIVERY_UNSUBSCRIPTION_TYPE: {
				if(_initComplete) {
					TransmitterListsDeliveryUnsubscription transmitterListsDeliveryUnsubscription = (TransmitterListsDeliveryUnsubscription)telegram;
					_connectionsManager.handleListsDeliveryUnsubscription(this, transmitterListsDeliveryUnsubscription);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_LISTS_UPDATE_TYPE:
			case DataTelegram.TRANSMITTER_LISTS_UPDATE_2_TYPE: {
				if(_initComplete) {
					TransmitterListsUpdate transmitterListsUpdate = (TransmitterListsUpdate)telegram;
					_connectionsManager.handleListsUpdate(this, transmitterListsUpdate);
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TRANSMITTER_DATA_TELEGRAM_TYPE: {
				if(_initComplete) {
					TransmitterDataTelegram transmitterDataTelegram = (TransmitterDataTelegram)telegram;
					final long usageIdentification = transmitterDataTelegram.getBaseSubscriptionInfo().getUsageIdentification();
					final List<AccessControlPlugin> controlPluginInterfaceList = _pluginFilters.get(usageIdentification);
					if(controlPluginInterfaceList != null) {
						// Es sind Plugins zust�ndig. Daten an Plugin �bergeben...
						final ApplicationDataTelegram[] telegrams = _accessControlUtil.processTelegramByPlugins(
								transmitterDataTelegram.getApplicationDataTelegram(), controlPluginInterfaceList, _remoteUserId, _connectionsManager
						);
						// ... und wieder verschicken. Da das Datenobjekt durch die Verarbeitung gr��er werden kann, m�ssen hier eventuell mehrere
						// Telegramme verschickt werden. Oder gar keins, wenn erst auf weitere (Teil-)Telegramme gewartet werden muss bzw. das Plugin die Nachricht verwirft.
						for(ApplicationDataTelegram dataTelegram : telegrams) {
							_subscriptionsManager.sendData(this, new TransmitterDataTelegram(dataTelegram, transmitterDataTelegram.getDirection()));
						}
					}
					else {
						// Kein Plugin zust�ndig, Daten einfach weiter verarbeiten
						_subscriptionsManager.sendData(this, transmitterDataTelegram);
					}
				}
				else {
					synchronized(_fastTelegramsList) {
						_fastTelegramsList.add(telegram);
					}
				}
				break;
			}
			case DataTelegram.TERMINATE_ORDER_TYPE: {
				TerminateOrderTelegram terminateOrderTelegram = (TerminateOrderTelegram)telegram;
				terminate(true, "Verbindung wurde vom anderen Datenverteiler terminiert. Ursache: " + terminateOrderTelegram.getCause(), null);
			}
			case DataTelegram.CLOSING_TYPE: {
				terminate(false, "Verbindung wurde vom anderen Datenverteiler geschlossen", null);
				break;
			}
			case DataTelegram.TRANSMITTER_KEEP_ALIVE_TYPE: {
				break;
			}
			default: {
				break;
			}
		}
	}

	/**
	 * Diese Methode wird von der Verbindungsverwaltung aufgerufen, um die Initialisierung einer Verbindung abzuschlie�en. Zuerst wird eine Instanz der
	 * Anmeldungsverwaltung f�r diese Verbindung erzeugt und zur Anmeldeverwaltung hinzugef�gt. Danach wird die addWayMethode der Wegverwaltung aufgerufen, um
	 * einen Eintrag f�r den verbundenen Datenverteiler zu erzeugen. Danach werden die Telegramme bearbeitet, die nicht zum Etablieren dieser Verbindung dienen und
	 * vor Fertigstellung der Initialisierung angekommen sind (Online-Daten, Wegeanmeldungen, Listenanmeldungen usw.).
	 *
	 * @return true: Initialisierung abgeschlossen, false: Initialisierung nicht abgeschlossen
	 */
	public final boolean completeInitialisation() {
		if(!_initComplete) {
			synchronized(_connectionsManager) {
				_subscriptionsFromDavStorage = new SubscriptionsFromDavStorage(this);
				_subscriptionsManager.subscribe(_subscriptionsFromDavStorage);
				_bestWayManager.addWay(this);
			}
			_initComplete = true;
			synchronized(_fastTelegramsList) {
				int size = _fastTelegramsList.size();
				if(size > 0) {
					for(int i = 0; i < size; ++i) {
						update(_fastTelegramsList.removeFirst());
					}
				}
			}
		}
		return _initComplete;
	}

	/**
	 * Gibt die h�chhste unterst�tzte Version aus den gegebenen Versionen oder -1, wenn keine von den gegebenen Versionen unterst�tzt wird, zur�ck.
	 *
	 * @param versions Feld der Versionen
	 *
	 * @return die h�chste unterst�tzte version oder -1
	 */
	private int getPrefferedVersion(int versions[]) {

		if(_versions == null) {
			return -1;
		}
		for(int i = 0; i < versions.length; ++i) {
			for(int j = 0; j < _versions.length; ++j) {
				if(versions[i] == _versions[j]) {
					return versions[i];
				}
			}
		}
		return -1;
	}


	/**
	 * Erledigt den Authentifizierungsprozess.
	 *
	 * @throws de.bsvrz.dav.daf.main.CommunicationError,
	 *          wenn bei der initialen Kommunikation mit dem Datenverteiler Fehler aufgetreten sind
	 */
	private void authentify() throws CommunicationError {
		// Authentifikationstext holen
		TransmitterAuthentificationTextRequest authentificationTextRequest = new TransmitterAuthentificationTextRequest(
				_properties.getDataTransmitterId()
		);
		sendTelegram(authentificationTextRequest);
		TransmitterAuthentificationTextAnswer authentificationTextAnswer = (TransmitterAuthentificationTextAnswer)waitForAnswerTelegram(
				DataTelegram.TRANSMITTER_AUTHENTIFICATION_TEXT_ANSWER_TYPE, "Aufforderung zur Authentifizierung"
		);
		byte encriptedUserPassword[] = authentificationTextAnswer.getEncryptedPassword(
				_properties.getAuthentificationProcess(), _authentifyWithPassword
		);

		// User Authentifizierung
		String authentificationProcessName = _properties.getAuthentificationProcess().getName();
		TransmitterAuthentificationRequest authentificationRequest = new TransmitterAuthentificationRequest(
				authentificationProcessName, _authentifyAsUser, encriptedUserPassword
		);
		sendTelegram(authentificationRequest);

		TransmitterAuthentificationAnswer authentificationAnswer = (TransmitterAuthentificationAnswer)waitForAnswerTelegram(
				DataTelegram.TRANSMITTER_AUTHENTIFICATION_ANSWER_TYPE, "Antwort auf eine Authentifizierungsanfrage"
		);
		if(!authentificationAnswer.isSuccessfullyAuthentified()) {
			throw new CommunicationError("Die Authentifizierung beim anderen Datenverteiler ist fehlgeschlagen");
		}
		_connectedTransmitterId = authentificationAnswer.getCommunicationTransmitterId();
		_lowLevelCommunication.setRemoteName("DAV " + _connectedTransmitterId);

	}
}
