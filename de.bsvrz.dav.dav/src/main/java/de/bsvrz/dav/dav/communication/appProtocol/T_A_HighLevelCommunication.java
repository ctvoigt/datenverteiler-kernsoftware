/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.dav.communication.appProtocol;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.SendDataObject;
import de.bsvrz.dav.daf.communication.lowLevel.HighLevelCommunicationCallbackInterface;
import de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunicationInterface;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ApplicationDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationTextAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.AuthentificationTextRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ClosingTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ComParametersAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ComParametersRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.DataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ProtocolVersionAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ProtocolVersionRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveSubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.ReceiveUnsubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.RequestSenderDataTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendSubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.SendUnsubscriptionTelegram;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TelegramTimeAnswer;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TelegramTimeRequest;
import de.bsvrz.dav.daf.communication.lowLevel.telegrams.TerminateOrderTelegram;
import de.bsvrz.dav.daf.main.CommunicationError;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.impl.CommunicationConstant;
import de.bsvrz.dav.dav.communication.accessControl.AccessControlPlugin;
import de.bsvrz.dav.dav.communication.accessControl.AccessControlUtil;
import de.bsvrz.dav.dav.main.AuthentificationComponent;
import de.bsvrz.dav.dav.main.ConnectionsManager;
import de.bsvrz.dav.dav.main.ServerConnectionProperties;
import de.bsvrz.dav.dav.main.SubscriptionsFromApplicationStorage;
import de.bsvrz.dav.dav.main.SubscriptionsFromRemoteStorage;
import de.bsvrz.dav.dav.main.SubscriptionsManager;
import de.bsvrz.dav.dav.main.Transmitter;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.*;

/**
 * Diese Klasse stellt eine Verbindung vom Datenverteiler zur Applikation dar. �ber diese Verbindung k�nnen Telegramme an eine Applikation verschickt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 8333 $
 */
public class T_A_HighLevelCommunication implements T_A_HighLevelCommunicationInterface, HighLevelCommunicationCallbackInterface {

	private static final Debug _debug = Debug.getLogger();

	/** Die erste Ebene der Kommunikation */
	private LowLevelCommunicationInterface _lowLevelCommunication;

	/** Die Eigenschaften diese Verbindung */
	private ServerConnectionProperties _properties;

	/** Die unterst�tzten Versionen des Datenverteilers */
	private int _versions[];

	/** Der Applikation Id */
	private long _applicationId;

	/** Die Id des Benutzers */
	private long _remoteUserId;

	/** Der Konfiguration Id */
	private long _configurationId;

	/** Der Name der Applikation */
	private String _applicationName;

	/** Die Pid des Applikationstyps */
	private String _applicationTypePid;

	/** Die Pid der Konfiguration */
	private String _configurationPid;

	/** Die Verwaltung der Anmelde Komponenten */
	private SubscriptionsManager _subscriptionsManager;

	/** Die Verwaltung der Datenverteiler Verbindungen */
	private ConnectionsManager _connectionsManager;

	/** Die Anmeldekomponente diese Verbindung */
	private SubscriptionsFromApplicationStorage _subscriptionsFromApplicationStorage;

	/** Die Authentifizierungskomponente */
	private AuthentificationComponent _authentificationComponent;

	/** Tempor�re Liste der Systemtelegramme f�r interne Synchronisationszwecke. */
	private List<DataTelegram> _syncSystemTelegramList;

	/** Die Information ob auf die Konfiguration gewartet werden muss. */
	private boolean _waitForConfiguration;

	/** Objekt zur internen Synchronization */
	private Object _sync;

	private boolean _closed = false;

	private Object _closedLock = new Object();

	private final long _connectionCreatedTime;

	/** Wird benachrichtigt, sobald die Verbindung zur Applikation komplett aufgebaut wurde. Ist dieses Objekt nicht vorhanden, wird nichts gemacht. */
	private ConnectionsManager.ApplicationStatusUpdater _applicationStatusUpdater = null;

	/** Map in der eine je Datenidentifikation eine Liste von empfangenen Telegrammen, die zu einem Datensatz geh�ren zwischengespeichert werden k�nnen */
	private Map<BaseSubscriptionInfo, List<ApplicationDataTelegram>> _stalledTelegramListMap = new HashMap<BaseSubscriptionInfo, List<ApplicationDataTelegram>>();

	/** Installierte PlugIn-Filter, die zur Zugriffsrechtepr�fung bestimmte ATGUs filtern */
	private final Map<Long, List<AccessControlPlugin>> _pluginFilters;

	private final AccessControlUtil _accessControlUtil;

	/**
	 * Erzeugt ein neues Objekt mit den gegebenen Parametern.
	 *
	 * @param properties           stellt die Parameter einer Verbindung zwischen zwei Servern
	 * @param subscriptionsManager Verwaltung der Anmeldungen
	 * @param connectionsManager   Verbindungsverwaltung
	 * @param waitForConfiguration true: ,false:
	 */
	public T_A_HighLevelCommunication(
			ServerConnectionProperties properties,
			SubscriptionsManager subscriptionsManager,
			ConnectionsManager connectionsManager,
			boolean waitForConfiguration
	) {
		_applicationId = -1;
		_versions = new int[1];
		_versions[0] = 3;
		_lowLevelCommunication = properties.getLowLevelCommunication();
		_properties = properties;
		_subscriptionsManager = subscriptionsManager;
		_connectionsManager = connectionsManager;
		_pluginFilters = connectionsManager.getPluginFilterMap();
		_authentificationComponent = _properties.getAuthentificationComponent();
		_syncSystemTelegramList = new LinkedList<DataTelegram>();
		_waitForConfiguration = waitForConfiguration;
		_sync = hashCode();
		_connectionCreatedTime = System.currentTimeMillis();
		_lowLevelCommunication.setHighLevelComponent(this);
		_accessControlUtil = new AccessControlUtil();
	}

	/**
	 * Dieses Objekt wird benachrichtigt, sobald die Verbindung zur Applikation aufgebaut wurde.
	 *
	 * @param applicationStatusUpdater Objekt, das benachrichtigt wird.
	 */
	public void setApplicationStatusUpdater(ConnectionsManager.ApplicationStatusUpdater applicationStatusUpdater) {
		_applicationStatusUpdater = applicationStatusUpdater;
	}

	public final long getTelegrammTime(final long maxWaitingTime) throws CommunicationError {
		long time = System.currentTimeMillis();
		TelegramTimeRequest telegramTimeRequest = new TelegramTimeRequest(time);
		_lowLevelCommunication.send(telegramTimeRequest);

		TelegramTimeAnswer telegramTimeAnswer = null;
		long waitingTime = 0, startTime = System.currentTimeMillis();
		long sleepTime = 10;
		while(waitingTime < maxWaitingTime) {
			try {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.wait(sleepTime);
					if(sleepTime < 1000) sleepTime *= 2;
					DataTelegram telegram = null;
					ListIterator _iterator = _syncSystemTelegramList.listIterator(0);
					while(_iterator.hasNext()) {
						telegram = (DataTelegram)_iterator.next();
						if((telegram != null) && (telegram.getType() == DataTelegram.TELEGRAM_TIME_ANSWER_TYPE)) {
							if(((TelegramTimeAnswer)telegram).getTelegramStartTime() == time) {
								telegramTimeAnswer = (TelegramTimeAnswer)telegram;
								_iterator.remove();
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

	public final void sendData(ApplicationDataTelegram telegram) {
		_lowLevelCommunication.send(telegram);
	}

	public final void sendData(ApplicationDataTelegram telegrams[]) {
		_lowLevelCommunication.send(telegrams);
	}

	public final void terminate(final boolean error, final String message) {
		final DataTelegram terminationTelegram;
		if(error) {
			terminationTelegram = new TerminateOrderTelegram(message);
		}
		else {
			terminationTelegram = new ClosingTelegram();
		}
		terminate(error, message, terminationTelegram);
	}

	/**
	 * Zeitpunkt, an dem das Objekt erstellt wurde und somit eine Verbindung zum DaV bestand.
	 *
	 * @return Zeit in ms seit dem 1.1.1970
	 */
	public long getConnectionCreatedTime() {
		return _connectionCreatedTime;
	}

	/**
	 * Liefert einen beschreibenden Text mit dem Zustand des Sendepuffers aus der LowLevelCommunication.
	 *
	 * @return Sendepufferzustand als Text
	 *
	 * @see de.bsvrz.dav.daf.communication.lowLevel.LowLevelCommunicationInterface#getSendBufferState()
	 */
	public String getSendBufferState() {
		return _lowLevelCommunication.getSendBufferState();
	}

	public final void terminate(boolean error, String message, DataTelegram terminationTelegram) {
		synchronized(_closedLock) {
			if(_closed) return;
			_closed = true;
		}
		synchronized(this) {
			String debugMessage = "Verbindung zur Applikation (id: " + getId() + ", typ: " + getApplicationTypePid() + ", name: " + getApplicationName()
			                      + ") wird terminiert. Ursache: " + message;
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
			if((_subscriptionsManager != null) && (_subscriptionsFromApplicationStorage != null)) {
				_subscriptionsManager.remove(_subscriptionsFromApplicationStorage);
			}
		}
	}


	public void disconnected(boolean error, final String message) {
		terminate(error, message);
	}

	public void updateConfigData(SendDataObject receivedData) {
		throw new UnsupportedOperationException("updateConfigData nicht implementiert");
	}

	public final void triggerSender(BaseSubscriptionInfo data, byte state) {
		RequestSenderDataTelegram requestSenderDataTelegram = new RequestSenderDataTelegram(data, state);
		_lowLevelCommunication.send(requestSenderDataTelegram);
	}

	public final SubscriptionsFromRemoteStorage getSubscriptionsFromRemoteStorage() {
		return _subscriptionsFromApplicationStorage;
	}

	public final long getId() {
		return _applicationId;
	}

	public final long getRemoteUserId() {
		return _remoteUserId;
	}

	public final long getConfigurationId() {
		return _configurationId;
	}

	public final long getDataTransmitterId() {
		return _properties.getDataTransmitterId();
	}

	public final String getApplicationTypePid() {
		return _applicationTypePid;
	}

	public final String getApplicationName() {
		return _applicationName;
	}

	public final boolean isConfiguration() {
		if(CommunicationConstant.CONFIGURATION_TYPE_PID.equals(_applicationTypePid)) {
			Object objects[] = _properties.getLocalModeParameter();
			if(objects != null) {
				String configurationPid = (String)objects[0];
				if(_configurationPid.equals(configurationPid)) {
					return true;
				}
			}
		}
		return false;
	}

	public final void continueAuthentification() {
		synchronized(_sync) {
			_waitForConfiguration = false;
			_sync.notify();
		}
	}


	/**
	 * Gibt die Version zur�ck, die von dieser Verbindung unterst�tzt wird.
	 *
	 * @param versions Versionen, die unterst�tzt werden sollen. Wird <code>null</code> �bergeben, so wird -1 zur�ckgegeben.
	 *
	 * @return Version, die aus den gegebenen Versionen unterst�tzt wird. Wird keine der �bergebenen Versionen unterst�tzt, so wird -1 zur�ckgegeben.
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

	public final void update(DataTelegram telegram) {
		if(Transmitter._debugLevel > 10) {
			System.err.println("T_A <-  " + (telegram == null ? "null" : telegram.toShortDebugString()));
		}
		if(telegram == null) {
			return;
		}
		switch(telegram.getType()) {
			case DataTelegram.TELEGRAM_TIME_ANSWER_TYPE: {
				synchronized(_syncSystemTelegramList) {
					_syncSystemTelegramList.add(telegram);
					_syncSystemTelegramList.notifyAll();
				}
				break;
			}
			case DataTelegram.PROTOCOL_VERSION_REQUEST_TYPE: {
				ProtocolVersionRequest protocolVersionRequest = (ProtocolVersionRequest)telegram;
				int version = getPrefferedVersion(protocolVersionRequest.getVersions());
				ProtocolVersionAnswer protocolVersionAnswer = new ProtocolVersionAnswer(version);
				_lowLevelCommunication.send(protocolVersionAnswer);
				break;
			}
			case DataTelegram.AUTHENTIFICATION_TEXT_REQUEST_TYPE: {
				long formativeConfigurationId = 0;
				AuthentificationTextRequest authentificationTextRequest = (AuthentificationTextRequest)telegram;
				_configurationPid = authentificationTextRequest.getConfigurationPid();
				if("".equals(_configurationPid)) {
					_configurationPid = CommunicationConstant.LOCALE_CONFIGURATION_PID_ALIASE;
				}
				else {
					String[] strings = _configurationPid.split(":");
					if(strings.length > 1) {
						_configurationPid = strings[0];
						try {
							// Id des Konfigurationsverantwortlichen wird mit Doppelpunkt getrennt hinter der Pid erwartet,
							// wenn sich die Konfiguration anmeldet
							formativeConfigurationId = Long.parseLong(strings[1]);
						}
						catch(NumberFormatException e) {
							_debug.error("Fehler beim Parsen der mit Doppelpunkt getrennten Id an der Pid des Konfigurationsverantwortlichen", e);
						}
					}
				}
				_applicationName = authentificationTextRequest.getApplicationName();
				_debug.finest("applicationName", _applicationName);
				_applicationTypePid = authentificationTextRequest.getApplicationTypePid();
				_debug.finest("applicationTypePid", _applicationTypePid);
				_lowLevelCommunication.setRemoteName(_applicationName + " (Typ: " + _applicationTypePid + ")");
				if(_waitForConfiguration) {
					boolean mustWait = true;
					if(CommunicationConstant.CONFIGURATION_TYPE_PID.equals(_applicationTypePid)) {
						if(_properties.isLocalMode()) {
							if(formativeConfigurationId != 0) {
								// Die von der Konfiguration vorgegebene Pid und Id des Konfigurationsverantwortlichen wird als Default f�r die Applikationen
								// gespeichert
								_properties.setLocalModeParameter(_configurationPid, formativeConfigurationId);
								_connectionsManager.setLocaleModeParameter(_configurationPid, formativeConfigurationId);
								_debug.info("Default-Konfiguration " + _configurationPid + ", Id " + formativeConfigurationId);
								mustWait = false;
							}
							else {
								terminate(true, "Konfiguration hat die Id des Konfigurationsverantwortlichen nicht vorgegeben");
								return;
							}
						}
					}
					if(mustWait) {
						synchronized(_sync) {
							try {
								_debug.finest("mustWait", mustWait);
								while(_waitForConfiguration) {
									if(_closed) return;
									_sync.wait(1000);
								}
							}
							catch(InterruptedException ex) {
								return;
							}
						}
					}
				}
				_waitForConfiguration = false;
				_debug.finest("waitForConfiguration", _waitForConfiguration);
				String text = _authentificationComponent.getAuthentificationText(_applicationName);
				AuthentificationTextAnswer authentificationTextAnswer = new AuthentificationTextAnswer(text);
				_lowLevelCommunication.send(authentificationTextAnswer);
				break;
			}
			case DataTelegram.AUTHENTIFICATION_REQUEST_TYPE: {
				AuthentificationRequest authentificationRequest = (AuthentificationRequest)telegram;
				String userName = authentificationRequest.getUserName();

				try {
					_remoteUserId = _connectionsManager.isValidUser(
							userName,
							authentificationRequest.getUserPassword(),
							_authentificationComponent.getAuthentificationText(_applicationName),
							_authentificationComponent.getAuthentificationProcess(),
							_applicationTypePid
					);

					AuthentificationAnswer authentificationAnswer = null;
					if(_remoteUserId > -1) {
						// Pid und Id der Default-Konfiguration aus globalen Einstellungen holen und in lokalen Einstellungen speichern
						if(_properties.isLocalMode()) {
							String pid = _connectionsManager.getLocaleModeConfigurationPid();
							long id = _connectionsManager.getLocaleModeConfigurationId();
							_properties.setLocalModeParameter(pid, id);
						}
						if(CommunicationConstant.LOCALE_CONFIGURATION_PID_ALIASE.equals(_configurationPid)) {
							Object objects[] = _properties.getLocalModeParameter();
							if(objects != null) {
								_configurationPid = (String)objects[0];
								_configurationId = ((Long)objects[1]).longValue();
							}
							else {
								_configurationId = _connectionsManager.getConfigurationId(_configurationPid);
							}
						}
						else {
							_configurationId = _connectionsManager.getConfigurationId(_configurationPid);
						}

						if(CommunicationConstant.CONFIGURATION_TYPE_PID.equals(_applicationTypePid)) {
							Object objects[] = _properties.getLocalModeParameter();
							if(objects == null) {
								_applicationId = _connectionsManager.getApplicationId(_applicationTypePid, _applicationName);
							}
							else {
								String _configurationPid = (String)objects[0];
								if(this._configurationPid.equals(_configurationPid)) {
									_applicationId = 0;
								}
								else {
									_applicationId = _connectionsManager.getApplicationId(_applicationTypePid, _applicationName);
								}
							}
						}
						else {
							_applicationId = _connectionsManager.getApplicationId(_applicationTypePid, _applicationName);
						}
						if(_applicationId == -1) {
							terminate(
									true,
									"Die Id der Applikation konnte nicht ermittelt werden, ApplikationsTyp: " + _applicationTypePid + ", ApplikationsName: "
									+ _applicationName
							);
							return;
						}
						if(_configurationId == -1) {
							terminate(true, "Ung�ltige Pid der Konfiguration: " + _configurationPid);
							return;
						}
						authentificationAnswer = new AuthentificationAnswer(
								_remoteUserId, _applicationId, _configurationId, _properties.getDataTransmitterId()
						);

						// Der Benutzer hat sich erfolgreich angemeldet. Also ist die Verbindung hergestellt.
						if(_applicationStatusUpdater != null) {
							_applicationStatusUpdater.applicationAdded(this);
						}
					}
					else {
						authentificationAnswer = new AuthentificationAnswer(false);
					}
					if(authentificationAnswer != null) {
						_lowLevelCommunication.send(authentificationAnswer);
					}
				}
				catch(ConfigurationChangeException ex) {
					ex.printStackTrace();
					terminate(
							true, "Fehler w�hrend der Authentifizierung einer Applikation beim Zugriff auf die Konfiguration: " + ex.getMessage()
					);
					return;
				}
				break;
			}
			case DataTelegram.COM_PARAMETER_REQUEST_TYPE: {
				ComParametersRequest comParametersRequest = (ComParametersRequest)telegram;
				// Empfangene Timeoutparameter werden �bernommen und nach unten begrenzt
				long keepAliveSendTimeOut = comParametersRequest.getKeepAliveSendTimeOut();
				if(keepAliveSendTimeOut < 5000) keepAliveSendTimeOut = 5000;
				long keepAliveReceiveTimeOut = comParametersRequest.getKeepAliveReceiveTimeOut();
				if(keepAliveReceiveTimeOut < 6000) keepAliveReceiveTimeOut = 6000;

				ComParametersAnswer comParametersAnswer = null;
//				if(keepAliveSendTimeOut < keepAliveReceiveTimeOut) {
//					long tmp = keepAliveSendTimeOut;
//					keepAliveSendTimeOut = keepAliveReceiveTimeOut;
//					keepAliveReceiveTimeOut = tmp;
//				}
				byte cacheThresholdPercentage = comParametersRequest.getCacheThresholdPercentage();
				short flowControlThresholdTime = comParametersRequest.getFlowControlThresholdTime();
				int minConnectionSpeed = comParametersRequest.getMinConnectionSpeed();
				comParametersAnswer = new ComParametersAnswer(
						keepAliveSendTimeOut, keepAliveReceiveTimeOut, cacheThresholdPercentage, flowControlThresholdTime, minConnectionSpeed
				);
				_lowLevelCommunication.send(comParametersAnswer);
				_lowLevelCommunication.updateKeepAliveParameters(keepAliveSendTimeOut, keepAliveReceiveTimeOut);
				_lowLevelCommunication.updateThroughputParameters(
						(float)cacheThresholdPercentage * 0.01f, (long)(flowControlThresholdTime * 1000), minConnectionSpeed
				);
				_subscriptionsFromApplicationStorage = new SubscriptionsFromApplicationStorage(this);
				_subscriptionsManager.subscribe(_subscriptionsFromApplicationStorage);
				// locale Configuration
				if(CommunicationConstant.CONFIGURATION_TYPE_PID.equals(_applicationTypePid)) {
					Object objects[] = _properties.getLocalModeParameter();
					if(objects != null) {
						String configurationPid = (String)objects[0];
						if(_configurationPid.equals(configurationPid)) {
							_connectionsManager.setLocalConfigurationAvaillable();
						}
					}
				}
				break;
			}
			case DataTelegram.TELEGRAM_TIME_REQUEST_TYPE: {
				TelegramTimeRequest telegramTimeRequest = (TelegramTimeRequest)telegram;
				_lowLevelCommunication.send(new TelegramTimeAnswer(telegramTimeRequest.getTelegramRequestTime()));
				break;
			}
			case DataTelegram.SEND_SUBSCRIPTION_TYPE: {
				SendSubscriptionTelegram sendSubscriptionTelegram = (SendSubscriptionTelegram)telegram;
				_connectionsManager.handleApplicationSendSubscription(this, sendSubscriptionTelegram);
				break;
			}
			case DataTelegram.SEND_UNSUBSCRIPTION_TYPE: {
				SendUnsubscriptionTelegram sendUnsubscriptionTelegram = (SendUnsubscriptionTelegram)telegram;
				_connectionsManager.handleApplicationSendUnsubscription(this, sendUnsubscriptionTelegram);
				break;
			}
			case DataTelegram.RECEIVE_SUBSCRIPTION_TYPE: {
				ReceiveSubscriptionTelegram receiveSubscriptionTelegram = (ReceiveSubscriptionTelegram)telegram;
				_connectionsManager.handleApplicationReceiveSubscription(this, receiveSubscriptionTelegram);
				break;
			}
			case DataTelegram.RECEIVE_UNSUBSCRIPTION_TYPE: {
				ReceiveUnsubscriptionTelegram receiveUnsubscriptionTelegram = (ReceiveUnsubscriptionTelegram)telegram;
				_connectionsManager.handleApplicationReceiveUnsubscription(this, receiveUnsubscriptionTelegram);
				break;
			}
			case DataTelegram.APPLICATION_DATA_TELEGRAM_TYPE: {
				ApplicationDataTelegram applicationDataTelegram = (ApplicationDataTelegram)telegram;
				final long usageIdentification = applicationDataTelegram.getBaseSubscriptionInfo().getUsageIdentification();
				final List<AccessControlPlugin> controlPluginInterfaceList = _pluginFilters.get(usageIdentification);
				if(controlPluginInterfaceList != null) {
					// Es sind Plugins zust�ndig. Daten an Plugin �bergeben...
					final ApplicationDataTelegram[] telegrams = _accessControlUtil.processTelegramByPlugins(
							applicationDataTelegram, controlPluginInterfaceList, _remoteUserId, _connectionsManager
					);
					// ... und wieder verschicken. Da das Datenobjekt durch die Verarbeitung gr��er werden kann, m�ssen hier eventuell mehrere
					// Telegramme verschickt werden. Oder gar keins, wenn erst auf weitere (Teil-)Telegramme gewartet werden muss bzw. das Plugin die Nachricht verwirft.
					for(ApplicationDataTelegram dataTelegram : telegrams) {
						_subscriptionsManager.sendData(this, dataTelegram);
					}
				}
				else {
					// Kein Plugin zust�ndig, Daten einfach weiter verarbeiten
					_subscriptionsManager.sendData(this, applicationDataTelegram);
				}
				break;
			}
			case DataTelegram.TERMINATE_ORDER_TYPE: {
				TerminateOrderTelegram terminateOrderTelegram = (TerminateOrderTelegram)telegram;
				terminate(true, "Verbindung wurde von der Applikation terminiert. Ursache: " + terminateOrderTelegram.getCause(), null);
				break;
			}
			case DataTelegram.CLOSING_TYPE: {
				terminate(false, "Verbindung wurde von der Applikation geschlossen", null);
				break;
			}
			case DataTelegram.KEEP_ALIVE_TYPE: {
				break;
			}
			default: {
				break;
			}
		}
	}

	/**
	 * Erzeugt eine Liste f�r verz�gerte Telegramme f�r eine Datenidentifikation und speichert sie in einer Map.
	 *
	 * @param info     Datenidentifikation der verz�gerten Telegramme
	 * @param maxCount Maximale Anzahl der verz�gerten Telegramme
	 *
	 * @return Neue Liste f�r verz�gerte Telegramme
	 *
	 * @see #getStalledTelegramList(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo)
	 * @see #deleteStalledTelegramList(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo)
	 */
	public List<ApplicationDataTelegram> createStalledTelegramList(final BaseSubscriptionInfo info, int maxCount) {
		final List<ApplicationDataTelegram> stalledTelegramsList = new ArrayList<ApplicationDataTelegram>(maxCount);
		_stalledTelegramListMap.put(info, stalledTelegramsList);
		return stalledTelegramsList;
	}

	/**
	 * Liefert eine vorher erzeugte Liste f�r verz�gerte Telegramme f�r eine Datenidentifikation.
	 *
	 * @param info Datenidentifikation der verz�gerten Telegramme
	 *
	 * @return Vorher erzeugte Liste f�r verz�gerte Telegramme
	 *
	 * @see #createStalledTelegramList(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo,int)
	 * @see #deleteStalledTelegramList(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo)
	 */
	public List<ApplicationDataTelegram> getStalledTelegramList(final BaseSubscriptionInfo info) {
		return _stalledTelegramListMap.get(info);
	}

	/**
	 * Liefert eine vorher erzeugte Liste f�r verz�gerte Telegramme f�r eine Datenidentifikation und entfernt sie aus der Map.
	 *
	 * @param info Datenidentifikation der verz�gerten Telegramme
	 *
	 * @return Vorher erzeugte Liste f�r verz�gerte Telegramme
	 *
	 * @see #createStalledTelegramList(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo,int)
	 * @see #getStalledTelegramList(de.bsvrz.dav.daf.communication.lowLevel.telegrams.BaseSubscriptionInfo)
	 */
	public List<ApplicationDataTelegram> deleteStalledTelegramList(final BaseSubscriptionInfo info) {
		return _stalledTelegramListMap.remove(info);
	}
}
