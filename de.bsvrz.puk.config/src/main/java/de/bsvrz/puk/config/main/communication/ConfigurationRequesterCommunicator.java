/*
 * Copyright 2011 by Kappich Systemberatung, Aachen
 * Copyright 2007 by Kappich Systemberatung, Aachen
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

package de.bsvrz.puk.config.main.communication;

import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeBaseValue;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeListValue;
import de.bsvrz.dav.daf.communication.dataRepresentation.AttributeValue;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.ByteArrayAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.ByteAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.LongAttribute;
import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.StringAttribute;
import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ClientApplication;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ConfigurationAuthority;
import de.bsvrz.dav.daf.main.config.ConfigurationChangeException;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ConfigurationObjectType;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DavApplication;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.DynamicObject;
import de.bsvrz.dav.daf.main.config.DynamicObjectType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerValueRange;
import de.bsvrz.dav.daf.main.config.IntegerValueState;
import de.bsvrz.dav.daf.main.config.MutableSet;
import de.bsvrz.dav.daf.main.config.NonMutableSet;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.ObjectSetType;
import de.bsvrz.dav.daf.main.config.ObjectSetUse;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;
import de.bsvrz.dav.daf.main.impl.config.DafAspect;
import de.bsvrz.dav.daf.main.impl.config.DafAttribute;
import de.bsvrz.dav.daf.main.impl.config.DafAttributeGroup;
import de.bsvrz.dav.daf.main.impl.config.DafAttributeGroupUsage;
import de.bsvrz.dav.daf.main.impl.config.DafAttributeListDefinition;
import de.bsvrz.dav.daf.main.impl.config.DafClientApplication;
import de.bsvrz.dav.daf.main.impl.config.DafConfigurationArea;
import de.bsvrz.dav.daf.main.impl.config.DafConfigurationAuthority;
import de.bsvrz.dav.daf.main.impl.config.DafConfigurationObject;
import de.bsvrz.dav.daf.main.impl.config.DafConfigurationObjectType;
import de.bsvrz.dav.daf.main.impl.config.DafDavApplication;
import de.bsvrz.dav.daf.main.impl.config.DafDoubleAttributeType;
import de.bsvrz.dav.daf.main.impl.config.DafDynamicObject;
import de.bsvrz.dav.daf.main.impl.config.DafDynamicObjectType;
import de.bsvrz.dav.daf.main.impl.config.DafIntegerAttributeType;
import de.bsvrz.dav.daf.main.impl.config.DafIntegerValueRange;
import de.bsvrz.dav.daf.main.impl.config.DafIntegerValueState;
import de.bsvrz.dav.daf.main.impl.config.DafMutableSet;
import de.bsvrz.dav.daf.main.impl.config.DafNonMutableSet;
import de.bsvrz.dav.daf.main.impl.config.DafObjectSetType;
import de.bsvrz.dav.daf.main.impl.config.DafObjectSetUse;
import de.bsvrz.dav.daf.main.impl.config.DafReferenceAttributeType;
import de.bsvrz.dav.daf.main.impl.config.DafStringAttributeType;
import de.bsvrz.dav.daf.main.impl.config.DafSystemObject;
import de.bsvrz.dav.daf.main.impl.config.DafTimeAttributeType;
import de.bsvrz.dav.daf.main.impl.config.request.RemoteRequestManager;
import de.bsvrz.dav.daf.main.impl.config.telegrams.AuthentificationAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.AuthentificationRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ConfigTelegram;
import de.bsvrz.dav.daf.main.impl.config.telegrams.IdsToObjectsAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.IdsToObjectsRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.MetaDataAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.NewObjectAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.NewObjectRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectInvalidateAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectInvalidateRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectRevalidateAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectRevalidateRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectSetNameAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectSetNameRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.ObjectsList;
import de.bsvrz.dav.daf.main.impl.config.telegrams.PidsToObjectsAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.PidsToObjectsRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectAnswerInfo;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectRequestInfo;
import de.bsvrz.dav.daf.main.impl.config.telegrams.SystemObjectsRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TransmitterConnectionInfo;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TransmitterConnectionInfoAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TransmitterConnectionInfoRequest;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TransmitterInfo;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TypeIdsToObjectsAnswer;
import de.bsvrz.dav.daf.main.impl.config.telegrams.TypeIdsToObjectsRequest;
import de.bsvrz.puk.config.main.authentication.Authentication;
import de.bsvrz.puk.config.main.communication.async.AsyncIdsToObjectsRequest;
import de.bsvrz.puk.config.main.communication.query.ForeignObjectManager;
import de.bsvrz.puk.config.util.async.AsyncRequest;
import de.bsvrz.puk.config.util.async.AsyncRequestCompletion;
import de.bsvrz.puk.config.util.async.AsyncRequestQueue;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Diese Klasse empf�ngt Telegramme vom Typ "atg.konfigurationsAnfrage" und "atg.konfigurationsSchreibAnfrage" und verschickt Telegramme vom Typ
 * "atg.konfigurationsAntwort" und "atg.konfigurationsSchreibAntwort".
 * <p/>
 * Die Telegramme vom Typ "atg.konfigurationsAnfrage" und "atg.konfigurationsSchreibAnfrage" werden interpretiert und an das Datenmodell weitergereicht. Die
 * Antwort des Datenmodells wird in Telegrammen vom Typ "atg.konfigurationsAntwort" und "atg.konfigurationsSchreibAntwort" an die anfragende Applikation
 * verschickt.
 *
 * @author Kappich Systemberatung
 * @version $Revision:5077 $
 */
class ConfigurationRequesterCommunicator {

	/** DebugLogger f�r Debug-Ausgaben */
	private static final Debug _debug = Debug.getLogger();

	private final AsyncRequestQueue _asyncRequestQueue;

	private final DataModel _dataModel;

	private final ClientDavInterface _connection;

	private final ConfigurationAuthority _configAuthority;

	private final DataDescription _answerDataDescription;

	private final DataDescription _writeAnswerDataDescription;

	/** Dient zum anfragen anderer Konfigurationen */
	private final RemoteRequestManager _remoteRequestManager;

	/** Wird ben�tigt um "isUserValid" zu bearbeiten */
	private final Authentication _authentication;

	private final Map _clientInfos = new TreeMap();

	private Map _code2AuthorityMap = new HashMap();

	private final ConfigurationArea _defaultConfigArea;

	private final boolean WAIT_FOR_SEND_CONTROL = true;

	private final SenderRole SENDER_ROLE = SenderRole.sender();

	/**
	 * Enth�lt die Objekte, die in der Antwort auf die von Applikationen initial gestellte Anfrage nach Meta-Objekten enthalten sind.
	 * Die Antwort enth�lt alle g�ltigen Aspekte, Attribute,
	 * Attributgruppen, Attributgruppenverwendungen, Attributtypen, Konfigurationsbereiche, Konfigurationsverantwortlichem, Mengenverwendungen, Typen,
	 * Wertebereiche und Werteust�nde.
	 */
	private ConfigurationObject[] _metaDataObjects;

	private ForeignObjectManager _foreignObjectManager = null;

	ConfigurationRequesterCommunicator(DataModel dataModel, Authentication authentication, ClientDavInterface connection) {
		this(null, dataModel, authentication, connection);
	}

	ConfigurationRequesterCommunicator(AsyncRequestQueue asyncRequestQueue, DataModel dataModel, Authentication authentication, ClientDavInterface connection) {
		_asyncRequestQueue = asyncRequestQueue;
		_dataModel = dataModel;
		_configAuthority = _dataModel.getConfigurationAuthority();
		_authentication = authentication;

		_connection = connection;

		AttributeGroup configAreaPropertyAtg = _dataModel.getAttributeGroup(
				"atg.konfigurationsVerantwortlicherEigenschaften"
		);
		Data configAuthorityPropertyData = _configAuthority.getConfigurationData(
				configAreaPropertyAtg
		);
		short configAuthorityCode = configAuthorityPropertyData.getScaledValue("kodierung").shortValue();
		long objectCreationIdPattern = (long)configAuthorityCode << 48;
		_debug.info("Eindeutige Kodierung des lokalen Konfigurationsverantwortlichen: " + configAuthorityCode);
		_debug.info("Maske zum Erzeugen neuer Objekte: " + objectCreationIdPattern);

		Data.TextArray defaultConfigAreaArray = configAuthorityPropertyData.getTextArray(
				"defaultBereich"
		);
		if(defaultConfigAreaArray.getLength() != 1) {
			throw new IllegalArgumentException(
					"Kein Default-Bereich f�r neue Objekte am Konfigurationsverantwortlichen versorgt: " + _configAuthority.getPid()
			);
		}

		final String defaultConfigAreaPid = defaultConfigAreaArray.getTextValue(0).getValueText();
		_defaultConfigArea = (ConfigurationArea)dataModel.getObject(defaultConfigAreaPid);
		if(_defaultConfigArea == null) {
			throw new IllegalArgumentException(
					"Default-Bereich '" + defaultConfigAreaPid + "' f�r neue Objekte am Konfigurationsverantwortlichen '" + _configAuthority.getPid()
					+ "' nicht gefunden"
			);
		}
		_remoteRequestManager = RemoteRequestManager.getInstance(_connection, _dataModel, _configAuthority);

		_metaDataObjects = getMetaDataObjects();

		// Wird zum versenden von Konfigurationsantworten gebraucht
		Aspect answerAspect = _dataModel.getAspect("asp.antwort");
		_answerDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsAntwort"), answerAspect, (short)0
		);
		_writeAnswerDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsSchreibAntwort"), answerAspect, (short)0
		);

		// Als Senke f�r Konfigurationsanfragen anmelden
		Aspect requestAspect = _dataModel.getAspect("asp.anfrage");
		final DataDescription requestDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsAnfrage"), requestAspect, (short)0
		);

		final DataDescription writeRequestDataDescription = new DataDescription(
				_dataModel.getAttributeGroup("atg.konfigurationsSchreibAnfrage"), requestAspect, (short)0
		);

		final RequestReceiver receiver = new RequestReceiver();

		_connection.subscribeReceiver(
				receiver, _configAuthority, requestDataDescription, ReceiveOptions.normal(), ReceiverRole.drain()
		);

		_connection.subscribeReceiver(
				receiver, _configAuthority, writeRequestDataDescription, ReceiveOptions.normal(), ReceiverRole.drain()
		);
	}

	/**
	 * Ermittelt die Objekte, die in der Antwort auf die von Applikationen initial gestellte Anfrage nach Meta-Objekten enthalten sind. Die Antwort enth�lt alle
	 * g�ltigen Aspekte, Attribute, Attributgruppen, Attributgruppenverwendungen, Attributtypen, Konfigurationsbereiche, Konfigurationsverantwortlichem,
	 * Mengenverwendungen, Typen, Wertebereiche und Wertezust�nde.
	 *
	 * @return Array mit allen relevanten Meta-Objekten
	 */
	private ConfigurationObject[] getMetaDataObjects() {
		_debug.finer("getMetaDataObjects");
		String[] metaTypes = {
				"typ.aspekt", "typ.attribut", "typ.attributgruppe", "typ.attributgruppenVerwendung", "typ.attributTyp", "typ.konfigurationsBereich",
				"typ.konfigurationsVerantwortlicher", "typ.mengenVerwendung", "typ.typ", "typ.werteBereich", "typ.werteZustand", "menge.aspekte",
				"menge.attribute", "menge.attributgruppen", "menge.attributgruppenVerwendungen", "menge.mengenVerwendungen", "menge.objektTypen",
				"menge.werteZustaende"
		};
		Set<ConfigurationObject> relevantObjects = new HashSet<ConfigurationObject>();
		for(int i = 0; i < metaTypes.length; i++) {
			SystemObjectType metaType = _dataModel.getType(metaTypes[i]);
			final List<SystemObject> objectsOfType = metaType.getObjects();
			for(SystemObject object : objectsOfType) {
				if(object instanceof ConfigurationObject) {
					ConfigurationObject configurationObject = (ConfigurationObject)object;
					relevantObjects.add(configurationObject);
				}
			}
		}
		return relevantObjects.toArray(new ConfigurationObject[relevantObjects.size()]);
	}

	/**
	 * Ermittelt die Antwort auf die von Applikationen initial gestellte Anfrage nach Meta-Objekten. Die Antwort enth�lt alle g�ltigen Aspekte, Attribute,
	 * Attributgruppen, Attributgruppenverwendungen, Attributtypen, Konfigurationsbereiche, Konfigurationsverantwortlichem, Mengenverwendungen, Typen,
	 * Wertebereiche und Werteust�nde.
	 *
	 * @param objects Array mit den Konfigurationsobjekten, die in der Antwort enthalten sein sollen.
	 *
	 * @return Antwortobjekt mit allen relevanten Meta-Objekten
	 */
	private MetaDataAnswer getMetaDataAnswer(ConfigurationObject[] objects) {
		_debug.finer("determineMetaDataAnswer");
		final DafSystemObject[] metaObjectsArray = new DafSystemObject[objects.length];
		_debug.finer("metaObjectsArray.length", metaObjectsArray.length);
		for(int i = 0; i < objects.length; i++) {
			ConfigurationObject object = objects[i];
			DafSystemObject metaObject = getMetaObject(object);
			metaObjectsArray[i] = metaObject;
		}
		return new MetaDataAnswer(System.currentTimeMillis(), metaObjectsArray, null);
	}

	public void setForeignObjectManager(final ForeignObjectManager foreignObjectManager) {
		_foreignObjectManager = foreignObjectManager;
	}

	/** Nimmt Konfigurationsanfragen entgegen und leitet sie an eine Methode zum verarbeiten weiter. */
	private final class RequestReceiver implements ClientReceiverInterface {

		public void update(ResultData[] results) {
			//System.out.println("----------------------update() wurde aufgerufen------------------------");
			//printResults(results);
			for(int resultIndex = 0; resultIndex < results.length; ++resultIndex) {
				try {
					ResultData result = results[resultIndex];
					SystemObject object = result.getObject();
					DataDescription description = result.getDataDescription();
					AttributeGroup attributeGroup = description.getAttributeGroup();
					Aspect aspect = description.getAspect();
					if(result.hasData()) {
						if(object == _configAuthority) {
							if("asp.anfrage".equals(aspect.getPid())) {
								String attributeGroupPid = attributeGroup.getPid();
								Data data = result.getData();
								if("atg.konfigurationsAnfrage".equals(attributeGroupPid)) {
									processRequest(false, data);
								}
								else if("atg.konfigurationsSchreibAnfrage".equals(attributeGroupPid)) {
									processRequest(true, data);
								}
							}
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace(System.out);
					_debug.error("Fehler beim Bearbeiten einer Konfigurationsanfrage", e);
				}
			}
		}
	}

	/** Verschickt die Antworten einer Konfigurationsanfrage */
	private class ClientInfo implements ClientSenderInterface {

		private final SystemObject _client;

		private List _answers = null;

		private List _writeAnswers = null;

		private ClientInfo(SystemObject client) throws OneSubscriptionPerSendData, ConfigurationException {
			_client = client;
			if(WAIT_FOR_SEND_CONTROL) {
				_answers = new LinkedList();
				_writeAnswers = new LinkedList();
			}
			_connection.subscribeSender(this, client, _answerDataDescription, SENDER_ROLE);
			_connection.subscribeSender(this, client, _writeAnswerDataDescription, SENDER_ROLE);
		}

		private void sendData(boolean isWriteRequestAnswer, ResultData result)
				throws DataNotSubscribedException, ConfigurationException, SendSubscriptionNotConfirmed {
			synchronized(this) {
				if(isWriteRequestAnswer) {
					if(_writeAnswers == null) {
						//System.out.println("sending write request answer: " + result);
						_connection.sendData(result);
					}
					else {
						_writeAnswers.add(result);
					}
				}
				else {
					if(_answers == null) {
						//System.out.println("sending request answer: " + result);
						_connection.sendData(result);
					}
					else {
						_answers.add(result);
					}
				}
			}
		}

		/**
		 * Signalisiert einer Sendenden Quelle dass ihre Daten von einem Empf�nger angemeldet wurden. Die Quelle wird damit aufgefordert Daten zu versenden.
		 *
		 * @param object          Die Anmeldeinformation der zu versendenden Daten.
		 * @param dataDescription Beschreibende Informationen zu den abzumeldenden Daten.
		 * @param state           Informationen zur angeforderten Daten : 0: bedeutet Sendung starten
		 */
		public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
			try {
				//System.out.println("Sendesteuerung f�r " + dataDescription.getAttributeGroup().getNameOrPidOrId() + ": " + state);
				if(state != 0) {
					boolean terminate = false;
					if(dataDescription.getAttributeGroup() == _answerDataDescription.getAttributeGroup()) {
						if(_answers == null) {
							terminate = true;
						}
					}
					else {
						if(_writeAnswers == null) {
							terminate = true;
						}
					}
					if(terminate) {
						/** Nicht object sondern _client f�r die Ausgaben verwenden. Normalerweise ist beides dasselbe Objekt, aber wenn das Objekt der lokalen
						/ Konfiguration unbekannt ist, befindet sich nur in _client das {@link UnknownObject}.*/
						_debug.info(
								dataDescription.getAttributeGroup().getName() + " wird f�r " + _client.getType().getNameOrPidOrId() + " " + _client.getName() + " id "
								+ _client.getId() + " abgemeldet"
						);
						_connection.unsubscribeSender(this, _client, dataDescription);
						//System.out.println("unsubscribe sender done");
					}
				}
				else {
					synchronized(this) {
						if(dataDescription.getAttributeGroup() == _answerDataDescription.getAttributeGroup()) {
							if(_answers != null) {
								Iterator i = _answers.iterator();
								while(i.hasNext()) {
									//System.out.println("sending queued request answer");
									_connection.sendData((ResultData)i.next());
								}
								_answers = null;
							}
						}
						else if(dataDescription.getAttributeGroup() == _writeAnswerDataDescription.getAttributeGroup()) {
							if(_writeAnswers != null) {
								Iterator i = _writeAnswers.iterator();
								while(i.hasNext()) {
									//System.out.println("sending queued write request answer");
									_connection.sendData((ResultData)i.next());
								}
								_writeAnswers = null;
							}
						}
					}
				}
			}
			catch(Exception e) {
				_debug.warning("Fehler bei der Bearbeitung der Sendesteuerung: ", e);
			}
		}

		/**
		 * Liefert <code>true</code> zur�ck, um den Datenverteiler-Applikationsfunktionenen zu signalisieren, dass eine Sendesteuerung erw�nscht ist.
		 *
		 * @param object          Wird ignoriert.
		 * @param dataDescription Wird ignoriert.
		 *
		 * @return <code>true</code>.
		 */
		public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
			return true;
		}
	}


	private void processRequest(final boolean isWriteRequest, Data data)
			throws IOException, ConfigurationChangeException, DataNotSubscribedException, OneSubscriptionPerSendData, SendSubscriptionNotConfirmed {

		StringBuilder message = new StringBuilder();
		SystemObject sender = null;
		try {
			sender = data.getReferenceValue("absenderId").getSystemObject();
			if(sender == null) {
				message.append("Das SystemObjekt des Absenders einer Konfigurationsanfrage wurde nicht gefunden:\n");
			}
		}
		catch(RuntimeException e) {
			message.append("Das SystemObjekt des Absenders einer Konfigurationsanfrage wurde nicht gefunden: (").append(e.getMessage()).append(")\n");
		}
		if(sender != null) {
			if(!sender.isValid()) {
				message.append("Als Absender einer Konfigurationsanfrage ist ein nicht mehr g�ltiges Objekt angegeben\n");
			}
			if(!(sender instanceof ClientApplication) && !(sender instanceof DavApplication)) {
				message.append(
						"Als Absender einer Konfigurationsanfrage ist ein Objekt angegeben, das weder eine Applikation noch einen Datenverteiler darstellt\n"
				);
			}
		}
		
		if(sender == null){
			sender = new UnknownObject(data.getReferenceValue("absenderId").getId(), _dataModel.getConfigurationAuthority().getConfigurationArea());
			final long senderId = data.getReferenceValue("absenderId").getId();
			message.append("  Id des Absenders: ").append(senderId).append("\n");		
			_debug.warning(message.toString());
			message.setLength(0);
		}
		if(message.length() != 0) {
			final long senderId = data.getReferenceValue("absenderId").getId();
			message.append("  Id des Absenders: ").append(senderId).append("\n");
			message.append("  SystemObjekt des Absenders: ").append(sender).append("\n");
			message.append(
					"  Eine m�gliche Ursache dieses Problems k�nnte sein, dass beim Start des Datenverteilers die im Aufrufparameter -datenverteilerId= "
					+ "angegebene Objekt-Id nicht korrekt ist.\n" + "  Folgende Datenverteiler sind der Konfiguration bekannt:\n"
			);
			final SystemObjectType davType = _connection.getDataModel().getType("typ.datenverteiler");
			final List<SystemObject> davs = davType.getElements();
			Formatter formatter = new Formatter();
			formatter.format("%40s %22s %s\n", "PID", "ID", "NAME");
			for(SystemObject dav : davs) {
				formatter.format("%40s %22d %s\n", dav.getPid(), dav.getId(), dav.getName());
			}
			message.append(formatter.toString());
			_debug.error(message.toString());
			throw new IllegalArgumentException("Ung�ltiges SystemObjekt des Absenders einer Konfigurationsanfrage: id " + senderId);
		}

		_debug.finer("ApplikationsID: " + sender.getId());

		final String senderReference = data.getTextValue("absenderZeichen").getText();
		_debug.finer(" Bezug: " + senderReference);

		byte requestType = data.getUnscaledValue("anfrageTyp").byteValue();
		_debug.finer(" AnfrageTyp: " + requestType);

		byte[] requestData = data.getUnscaledArray("anfrage").getByteArray();
		ConfigTelegram request = ConfigTelegram.getTelegram(requestType, null);
		request.read(new DataInputStream(new ByteArrayInputStream(requestData)));
		ConfigTelegram answer = null;
		ClientInfo clientInfo = (ClientInfo)_clientInfos.get(sender);
		if(clientInfo == null) {
			clientInfo = new ClientInfo(sender);
			_clientInfos.put(sender, clientInfo);
		}

		final SystemObject finalSender = sender;
		final ClientInfo finalClientInfo = clientInfo;

		boolean sendAnswerAsynchronously = false;
		switch(requestType) {
			case ConfigTelegram.META_DATA_REQUEST_TYPE: {
				_debug.fine("META_DATA_REQUEST_TYPE");
				answer = getMetaDataAnswer(_metaDataObjects);
				break;
			}
			case ConfigTelegram.OBJECT_REQUEST_TYPE: {
				SystemObjectsRequest r = (SystemObjectsRequest)request;
				_debug.finer("OBJECT_REQUEST_TYPE");
				SystemObjectRequestInfo info = r.getSystemObjectRequestInfo();
				SystemObjectAnswerInfo answerInfo = null;
				switch(info.getRequestType()) {
					case SystemObjectRequestInfo.IDS_TO_OBJECTS_TYPE: {
						_debug.fine(" IDS_TO_OBJECTS_TYPE:");
						IdsToObjectsRequest ir = (IdsToObjectsRequest)info;
						final long[] ids = ir.getIds();

						final AsyncIdsToObjectsRequest asyncIdsToObjectsRequest = new AsyncIdsToObjectsRequest(_dataModel, _foreignObjectManager, ids);
						asyncIdsToObjectsRequest.setCompletion(
								new AsyncRequestCompletion() {
									public void requestCompleted(AsyncRequest asyncRequest) {
//										System.out.println("ConfigurationRequesterCommunicator.requestCompleted");
										SystemObjectAnswerInfo asyncAnswerInfo = buildIdsToObjectsAnswerInfo(ids, asyncIdsToObjectsRequest.getObjects());
										final SystemObjectAnswer asyncAnswer = new SystemObjectAnswer(0, asyncAnswerInfo, null);
										try {
											buildAndSendReply(isWriteRequest, finalSender, senderReference, asyncAnswer, finalClientInfo);
										}
										catch(Exception e) {
											e.printStackTrace(System.out);
											_debug.error("Fehler beim asynchronen Versand einer Konfigurationsantwort: ", e);
										}
									}
								}
						);
						asyncIdsToObjectsRequest.enqueueTo(_asyncRequestQueue);
						sendAnswerAsynchronously = true;
						break;
					}
					case SystemObjectRequestInfo.PIDS_TO_OBJECTS_TYPE: {
						_debug.fine(" PIDS_TO_OBJECTS_TYPE:");
						PidsToObjectsRequest ir = (PidsToObjectsRequest)info;
						String[] pids = ir.getPids();
						DafSystemObject[] objects = new DafSystemObject[pids.length];
						for(int i = 0; i < pids.length; ++i) {
							SystemObject object = (SystemObject)_dataModel.getObject(pids[i]);
							if(object != null) {
								_debug.finer(" pid " + pids[i] + ": " + object.getNameOrPidOrId());
							}
							else {
								_debug.warning("Objekt mit pid " + pids[i] + " nicht gefunden");
							}
							objects[i] = getMetaObject(object);
						}
						answerInfo = new PidsToObjectsAnswer(objects, null);
						break;
					}
					case SystemObjectRequestInfo.TYPE_IDS_TO_OBJECTS_TYPE: {
						_debug.finer(" TYPE_IDS_TO_OBJECTS_TYPE:");
						TypeIdsToObjectsRequest ir = (TypeIdsToObjectsRequest)info;
						long[] ids = ir.getIds();
						ObjectsList[] objects = new ObjectsList[ids.length];
						for(int i = 0; i < ids.length; ++i) {
							SystemObject typeObject = (SystemObject)_dataModel.getObject(ids[i]);
							if(typeObject instanceof SystemObjectType) {
								SystemObjectType type = (SystemObjectType)typeObject;
								_debug.finer(" type: " + type.getNameOrPidOrId());
								List<SystemObject> elementList = type.getElements();
								Iterator elementIterator = elementList.iterator();
								int metaIterator = 0;
								DafSystemObject[] metaTypeElements = new DafSystemObject[elementList.size()];
								while(elementIterator.hasNext()) {
									metaTypeElements[metaIterator++] = getMetaObject((SystemObject)elementIterator.next());
								}
								objects[i] = new ObjectsList(ids[i], metaTypeElements, null);
							}
							else {
								if(typeObject == null) {
									_debug.finer("Typ-Objekt mit id " + ids[i] + " nicht gefunden.");
								}
								else {
									_debug.finer("Gefundenes Objekt mit id " + ids[i] + " ist kein Typ-Objekt.");
								}
							}
						}
						_debug.finer("objects.length = " + objects.length);
						answerInfo = new TypeIdsToObjectsAnswer(objects, null);
						break;
					}
				}
				if(answerInfo != null) {
					answer = new SystemObjectAnswer(0, answerInfo, null);
				}
				//Achtung Exception in parseToString
				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
			case ConfigTelegram.NEW_OBJECT_REQUEST_TYPE: {
				NewObjectRequest r = (NewObjectRequest)request;
				_debug.finer("NEW_OBJECT_REQUEST_TYPE:");
				SystemObjectType type = (SystemObjectType)_dataModel.getObject(r.getTypeId());
				SystemObject object;
				final DafSystemObject metaObject;
				if(type instanceof ConfigurationObjectType) {
					_debug.warning("Neue Konfigurationsobjekte k�nnen noch nicht online erzeugt werden");
					metaObject = null;
				}
				else {
					if(r.getPid() == null || _dataModel.getObject(r.getPid()) == null) {
						object = _defaultConfigArea.createDynamicObject((DynamicObjectType)type, r.getPid(), r.getName());
//						object = _dataModel.createDynamicObject(type, r.getPid(), r.getName());

						


						metaObject = getMetaObject(object);
						_debug.finer(" neues Objekt: " + metaObject.getId() + ":" + metaObject.getPid() + ":" + metaObject.getName());
					}
					else {
						object = null;
						metaObject = null;
						_debug.warning(
								"Neues dynamisches Objekt konnte nicht erzeugt werden, da bereits ein Objekt mit " + "der Pid " + r.getPid() + " existiert."
						);
					}
				}
				answer = new NewObjectAnswer(0, metaObject, null);
				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
			case ConfigTelegram.TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE: {
				TransmitterConnectionInfoRequest r = (TransmitterConnectionInfoRequest)request;
				long davId = r.getTransmitterId();
				_debug.finer("TRANSMITTER_CONNECTION_INFO_REQUEST_TYPE:");
				_debug.finer(" dav: " + davId);

				AttributeGroup connectionPropertiesAtg = _dataModel.getAttributeGroup("atg.datenverteilerTopologie");
				AttributeGroup davPropertiesAtg = _dataModel.getAttributeGroup("atg.datenverteilerEigenschaften");
				Iterator connectionIterator = _dataModel.getType("typ.datenverteilerVerbindung").getElements().iterator();
				List connectionInfoList = new LinkedList();
				while(connectionIterator.hasNext()) {
					ConfigurationObject connection = (ConfigurationObject)connectionIterator.next();
					try {
						Data connectionProperties = connection.getConfigurationData(connectionPropertiesAtg);
						if(connectionProperties == null) {
							_debug.warning("keine Topologie-Informationen f�r Verbindung " + connection.getNameOrPidOrId());
							continue;
						}
						//davEigenschaften der betroffenen Datenverteiler holen
						SystemObject dav1 = connectionProperties.getReferenceValue("datenverteilerA").getSystemObject();
						Data dav1Properties = dav1.getConfigurationData(davPropertiesAtg);
						if(dav1Properties == null) {
							_debug.warning("keine Eigenschaften f�r Datenverteiler " + dav1.getNameOrPidOrId());
							continue;
						}
						String dav1Address = dav1Properties.getTextValue("adresse").getText();
						int dav1SubAddress = dav1Properties.getScaledValue("subAdresse").intValue();
						SystemObject dav2 = connectionProperties.getReferenceValue("datenverteilerB").getSystemObject();
						Data dav2Properties = dav2.getConfigurationData(davPropertiesAtg);
						if(dav2Properties == null) {
							_debug.warning("keine Eigenschaften f�r Datenverteiler " + dav2.getNameOrPidOrId());
							continue;
						}
						String dav2Address = dav2Properties.getTextValue("adresse").getText();
						int dav2SubAddress = dav2Properties.getScaledValue("subAdresse").intValue();

						TransmitterInfo transmitterInfo1 = new TransmitterInfo(dav1.getId(), dav1Address, dav1SubAddress);
						TransmitterInfo transmitterInfo2 = new TransmitterInfo(dav2.getId(), dav2Address, dav2SubAddress);
						int direction = connectionProperties.getUnscaledValue("aktiverDatenverteiler").intValue();

						TransmitterInfo altTransmitterArray1[] = null;
						TransmitterInfo altTransmitterArray2[] = null;
						//	exchangeTransmitterList= new TransmitterInfo[altConnectionList.size()];
						ObjectSet altConnections = null;
						//if(dav1.getId()==davId || dav2.getId()==davId)
						altConnections = connection.getObjectSet("Ersatzverbindungen");
						if(altConnections != null) {
							List altTransmitterInfos1 = new LinkedList();
							List altTransmitterInfos2 = new LinkedList();
							List altConnectionList = altConnections.getElements();
							Iterator altConnectionIterator = altConnectionList.iterator();
							while(altConnectionIterator.hasNext()) {
								SystemObject altConnection = (SystemObject)altConnectionIterator.next();
								try {
									Data altConnectionProperties = altConnection.getConfigurationData(connectionPropertiesAtg);
									if(altConnectionProperties == null) {
										_debug.warning("keine Topologie-Informationen f�r Ersatz-Verbindung " + altConnection.getNameOrPidOrId());
										continue;
									}
									SystemObject altDavA = altConnectionProperties.getReferenceValue("datenverteilerA").getSystemObject();
									SystemObject altDavB = altConnectionProperties.getReferenceValue("datenverteilerB").getSystemObject();
									SystemObject altDav = null;
									List altTransmitterInfos = null;
									if(altDavA == dav1) {
										altDav = altDavB;
										altTransmitterInfos = altTransmitterInfos1;
									}
									else if(altDavB == dav1) {
										altDav = altDavA;
										altTransmitterInfos = altTransmitterInfos1;
									}
									else if(altDavA == dav2) {
										altDav = altDavB;
										altTransmitterInfos = altTransmitterInfos2;
									}
									else if(altDavB == dav2) {
										altDav = altDavA;
										altTransmitterInfos = altTransmitterInfos2;
									}
									if(altDav != null) {
										Data altDavProperties = altDav.getConfigurationData(davPropertiesAtg);
										if(altDavProperties == null) {
											_debug.warning("keine Eigenschaften f�r Datenverteiler " + dav2.getNameOrPidOrId());
											continue;
										}
										TransmitterInfo altTransmitterInfo = new TransmitterInfo(
												altDav.getId(),
												altDavProperties.getTextValue("adresse").getText(),
												altDavProperties.getScaledValue("subAdresse").intValue()
										);
										altTransmitterInfos.add(altTransmitterInfo);
									}
								}
								catch(Exception e) {
									_debug.warning(
											"Fehler beim Auslesen der Topologie-Information der Ersatz-Verbindung " + connection.getNameOrPidOrId() + ": "
											+ e.getMessage()
									);
								}
							}
							altTransmitterArray1 = (TransmitterInfo[])altTransmitterInfos1.toArray(
									new TransmitterInfo[altTransmitterInfos1.size()]
							);
							altTransmitterArray2 = (TransmitterInfo[])altTransmitterInfos2.toArray(
									new TransmitterInfo[altTransmitterInfos2.size()]
							);
						}
						if(direction == 1) {
							TransmitterConnectionInfo connectionInfo1 = new TransmitterConnectionInfo(
									transmitterInfo1,
									transmitterInfo2,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)1,
									// Normaleverbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray1,
									connectionProperties.getTextValue("benutzer1").getValueText(),
									connectionProperties.getTextValue("benutzer2").getValueText()
							);
							connectionInfoList.add(connectionInfo1);
						}
						else if(direction == 2) {
							TransmitterConnectionInfo connectionInfo2 = new TransmitterConnectionInfo(
									transmitterInfo2,
									transmitterInfo1,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)1,
									// Normaleverbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray2,
									connectionProperties.getTextValue("benutzer2").getValueText(),
									connectionProperties.getTextValue("benutzer1").getValueText()
							);
							connectionInfoList.add(connectionInfo2);
						}
						else {
							TransmitterConnectionInfo connectionInfo1 = new TransmitterConnectionInfo(
									transmitterInfo1,
									transmitterInfo2,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)((direction == 0) ? 0 : 2),
									// 0 hei�t Ersatzverbindung, 2 hei�t doppelte verbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray1,
									connectionProperties.getTextValue("benutzer1").getValueText(),
									connectionProperties.getTextValue("benutzer2").getValueText()
							);
							connectionInfoList.add(connectionInfo1);
							TransmitterConnectionInfo connectionInfo2 = new TransmitterConnectionInfo(
									transmitterInfo2,
									transmitterInfo1,
									connectionProperties.getScaledValue("wichtung").shortValue(),
									(byte)((direction == 0) ? 0 : 2),
									// 0 hei�t Ersatzverbindung, 2 hei�t doppelte verbindung
									connectionProperties.getTimeValue("ersatzverbindungsWartezeit").getMillis(),
									altConnections == null,
									altTransmitterArray2,
									connectionProperties.getTextValue("benutzer2").getValueText(),
									connectionProperties.getTextValue("benutzer1").getValueText()
							);
							connectionInfoList.add(connectionInfo2);
						}
					}
					catch(Exception e) {
						_debug.warning(
								"Fehler beim Auslesen der Topologie-Information der Verbindung " + connection.getNameOrPidOrId() + ": " + e.getMessage()
						);
					}
				}
				TransmitterConnectionInfo[] connectionInfoArray = (TransmitterConnectionInfo[])connectionInfoList.toArray(
						new TransmitterConnectionInfo[connectionInfoList.size()]
				);
				final long desiredReplyVersion = r.getDesiredReplyVersion();
				long replyVersion = 2;
				if(desiredReplyVersion < replyVersion) replyVersion = desiredReplyVersion;
				answer = new TransmitterConnectionInfoAnswer(replyVersion, davId, connectionInfoArray);
				//System.out.println(" ANSWER: " + answer.parseToString());
				break;
			}
//			case ConfigTelegram.OBJECT_SET_PID_REQUEST_TYPE: {
//				break;
//			}
//			case ConfigTelegram.SET_CHANGES_REQUEST_TYPE: {
//				break;
//			}
			case ConfigTelegram.AUTHENTIFICATION_REQUEST_TYPE: {
				// Bedingung 1: Der Benutzer muss als Objekt in der Konfiguration vorhanden sein
				// Bedingung 2: Der Benutzername muss mit dem gespeicherten Passwort �bereinstimmen
				AuthentificationRequest r = (AuthentificationRequest)request;

				_debug.finer("AUTHENTIFICATION_REQUEST_TYPE: " + r.getUserName() + ":");
				Iterator i = _dataModel.getType("typ.benutzer").getObjects().iterator();
				try {
					while(i.hasNext()) {
						SystemObject benutzer = (SystemObject)i.next();
						if(r.getUserName().equals(benutzer.getName())) {
							// answer = new AuthentificationAnswer(benutzer.getId());
							_debug.finer(" gefunden, id " + benutzer.getId());
							// Wenn der Benutzer nicht identifiziert werden kann, wird eine Exception geworfen
							_authentication.isValidUser(
									r.getUserName(), r.getEncriptedPasswort(), r.getAuthentificationText(), r.getAuthentificationProcessName()
							);
							answer = new AuthentificationAnswer(benutzer.getId());
							break;
						}
					} // while �ber alle Benutzer

					if(answer == null) {
						answer = new AuthentificationAnswer(-1);
						_debug.warning(
								"Authentifizierung fehlgeschlagen: Zum Benutzer '" + r.getUserName() + "' wurde kein Objekt in der Konfiguration gefunden"
						);
					}
				}
				catch(Exception e) {
					// Benutzer/Passwort Kombination pa�t nicht
					answer = new AuthentificationAnswer(-1);
					_debug.warning( "Authentifizierung fehlgeschlagen", e);
				}
				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
//			case ConfigTelegram.NEW_IDS_REQUEST_TYPE: {
//				break;
//			}
//			case ConfigTelegram.ARCHIVE_REQUEST_TYPE: {
//				break;
//			}

			case ConfigTelegram.OBJECT_INVALIDATE_REQUEST_TYPE: {
				_debug.fine("OBJECT_INVALIDATE_REQUEST_TYPE");
				ObjectInvalidateRequest r = (ObjectInvalidateRequest)request;
				SystemObject object = (SystemObject)_dataModel.getObject(r.getObjectId());
				try {
					object.invalidate();
					long notValidSince = 0;
					if(object instanceof DynamicObject) {
						final DynamicObject dynamicObject = (DynamicObject)object;
						notValidSince = dynamicObject.getNotValidSince();
					}
					answer = new ObjectInvalidateAnswer(notValidSince, r.getObjectId(), true);
				}
				catch(ConfigurationChangeException e) {
					answer = new ObjectInvalidateAnswer(0, r.getObjectId(), false);
				}
				break;
			}
			case ConfigTelegram.OBJECT_REVALIDATE_REQUEST_TYPE: {
				_debug.fine("OBJECT_REVALIDATE_REQUEST_TYPE");
				final ObjectRevalidateRequest r = (ObjectRevalidateRequest)request;
				final ConfigurationObject object = (ConfigurationObject)_dataModel.getObject(r.getObjectId());

				try {
					object.revalidate();
					answer = new ObjectRevalidateAnswer(0, r.getObjectId(), true);
				}
				catch(ConfigurationChangeException e) {
					answer = new ObjectRevalidateAnswer(0, r.getObjectId(), false);
				}
				break;
			}
			case ConfigTelegram.OBJECT_SET_NAME_REQUEST_TYPE: {
				_debug.fine("OBJECT_SET_NAME_REQUEST_TYPE");
				ObjectSetNameRequest r = (ObjectSetNameRequest)request;
				SystemObject object = (SystemObject)_dataModel.getObject(r.getObjectId());
				try {
					object.setName(r.getObjectName());
					answer = new ObjectSetNameAnswer(0, r.getObjectId(), true);
				}
				catch(Exception e) {
					_debug.warning("Objektname konnte nicht ge�ndert werden", e);
				}
				if(answer == null) answer = new ObjectSetNameAnswer(0, r.getObjectId(), false);

				//System.out.println("ANSWER: " + answer.parseToString());
				break;
			}
			default: {
				_debug.warning("Ung�ltige Anfrage: " + request.parseToString());
				throw new IllegalArgumentException(
						"Ung�ltige Konfigurationsanfrage:" + " TelegrammTyp:" + requestType + ", absender:" + finalSender + ", absenderZeichen:" + senderReference
				);
			}
		}
		if(answer != null) {
			buildAndSendReply(isWriteRequest, finalSender, senderReference, answer, finalClientInfo);
		}
		else {
			if(!sendAnswerAsynchronously) {
				_debug.warning("Zur Konfigurationsanfrage konnte keine Antwort erzeugt werden: " + request.parseToString());
			}
		}
	}

	private SystemObjectAnswerInfo buildIdsToObjectsAnswerInfo(final long[] ids, final SystemObject[] objects) {
		final SystemObjectAnswerInfo answerInfo;
		final DafSystemObject[] dafObjects = new DafSystemObject[objects.length];
		for(int i = 0; i < ids.length; ++i) {
			final SystemObject object = objects[i];
			if(object == null) {
				_debug.warning("Objekt nicht gefunden", ids[i]);
			}
			dafObjects[i] = getMetaObject(object);
		}
		answerInfo = new IdsToObjectsAnswer(dafObjects, null);
		return answerInfo;
	}

	private void buildAndSendReply(
			final boolean isWriteRequest, final SystemObject sender, final String senderReference, final ConfigTelegram answer, final ClientInfo clientInfo)
			throws IOException, SendSubscriptionNotConfirmed {
		//Anmelden wenn noch nicht passiert
		DataDescription resultDataDescription;
		List attributeValues = new ArrayList(4);
		if(isWriteRequest) {
			resultDataDescription = _writeAnswerDataDescription;
		}
		else {
			resultDataDescription = _answerDataDescription;
		}
		Iterator attributesIterator = resultDataDescription.getAttributeGroup().getAttributes().iterator();
		while(attributesIterator.hasNext()) {
			Attribute attribute = (Attribute)attributesIterator.next();
			AttributeValue attributeValue = new AttributeValue(null, attribute);
			if("absenderId".equals(attribute.getName())) {
				attributeValue.setValue(new LongAttribute(_configAuthority.getId()));
			}
			else if("absenderZeichen".equals(attribute.getName())) {
				attributeValue.setValue(new StringAttribute(senderReference));
			}
			else if("antwortTyp".equals(attribute.getName())) {
				attributeValue.setValue(new ByteAttribute(answer.getType()));
			}
			else if("antwort".equals(attribute.getName())) {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				DataOutputStream dataStream = new DataOutputStream(byteStream);
				answer.write(dataStream);
				dataStream.flush();
				attributeValue.setValue(new ByteArrayAttribute(byteStream.toByteArray()));
				//System.out.println("result byte array length: " + byteStream.toByteArray().length);
			}
			else {
				throw new RuntimeException("Unbekanntes Attribut in der Attributgruppe: " + attribute);
			}
			attributeValues.add(attributeValue);
		}
		ResultData result = new ResultData(
				sender, resultDataDescription, false, System.currentTimeMillis(), attributeValues
		);
		clientInfo.sendData(isWriteRequest, result);
	}

	private ConfigurationAuthority getConfigurationAuthority(long id) {
		ConfigurationAuthority authority = (ConfigurationAuthority)_code2AuthorityMap.get(new Short((short)(id >> 48)));
		return authority;
	}

	private void appendDataValues(List valueList, List dataValueList) {
		Iterator i = valueList.iterator();
		while(i.hasNext()) {
			AttributeBaseValue value = (AttributeBaseValue)i.next();
			if(value instanceof AttributeValue) {
				dataValueList.add(value.getValue());
			}
			else {
				if(value instanceof AttributeListValue) {
					try {
						appendDataValues(
								Arrays.asList((AttributeBaseValue[])((AttributeListValue)value).getAttributeBaseValues()), dataValueList
						);
					}
					catch(ConfigurationException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
		}
	}


	private static long[] getIds(List systemObjects) {
		long[] ids = new long[systemObjects.size()];
		int i = 0;
		Iterator iterator = systemObjects.iterator();
		while(iterator.hasNext()) {
			ids[i++] = ((SystemObject)iterator.next()).getId();
		}
		return ids;
	}

	private static ArrayList getIdsAsLongArrayList(List systemObjects) {
		ArrayList ids = new ArrayList(systemObjects.size());
		int i = 0;
		Iterator iterator = systemObjects.iterator();
		while(iterator.hasNext()) {
			SystemObject element = (SystemObject)iterator.next();
			//System.out.println("adding " + element.getId());
			ids.add(new Long(element.getId()));
		}
		return ids;
	}

	static DafSystemObject getMetaObject(SystemObject object) throws ConfigurationException {
		if(object == null) {
			return null;
		}
		if(object instanceof ConfigurationObject) {
			byte state = object.isValid() ? (byte)1 : (byte)2;
//			byte state = (byte)1;
			if(object instanceof Aspect) {
				Aspect o = (Aspect)object;
				return new DafAspect(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof Attribute) {
				Attribute o = (Attribute)object;
				AttributeType attributeType = o.getAttributeType();
				if(attributeType == null) {
					throw new IllegalStateException("Attributtyp des Attributs " + o + " nicht definiert");
				}
				DafAttribute metaAttribute = new DafAttribute(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						(short)o.getPosition(),
						o.getMaxCount(),
						o.isCountVariable(),
						attributeType.getId(),
						o.getDefaultAttributeValue()
				);
//			if(o.getId()==504) {
//				System.out.println("Attribute 504:");
//				System.out.println("   getMaxCount():" + metaAttribute.getMaxCount() );
//				System.out.println("   isCountVariable():" + metaAttribute.isCountVariable() );
//				System.out.println("   isArray():" + metaAttribute.isArray() );
//			}
				return metaAttribute;
			}
			else if(object instanceof AttributeGroup) {
				AttributeGroup o = (AttributeGroup)object;
				return new DafAttributeGroup(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof AttributeListDefinition) {
				AttributeListDefinition o = (AttributeListDefinition)object;
				return new DafAttributeListDefinition(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof DoubleAttributeType) {
				DoubleAttributeType o = (DoubleAttributeType)object;
				return new DafDoubleAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getAccuracy(),
						o.getUnit(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof IntegerAttributeType) {
				IntegerAttributeType o = (IntegerAttributeType)object;
				IntegerValueRange range = o.getRange();
				return new DafIntegerAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getByteCount(),
						range == null ? 0 : range.getId(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof ReferenceAttributeType) {
				ReferenceAttributeType o = (ReferenceAttributeType)object;
				SystemObjectType referencedType = o.getReferencedObjectType();
				return new DafReferenceAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						referencedType == null ? 0 : referencedType.getId(),
						o.getDefaultAttributeValue(),
						o.isUndefinedAllowed(),
						o.getReferenceType()
				);
			}
			else if(object instanceof StringAttributeType) {
				StringAttributeType o = (StringAttributeType)object;
				return new DafStringAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getMaxLength(),
						o.getEncodingName(),
						o.isLengthLimited(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof TimeAttributeType) {
				TimeAttributeType o = (TimeAttributeType)object;
				return new DafTimeAttributeType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getAccuracy(),
						o.isRelative(),
						o.getDefaultAttributeValue()
				);
			}
			else if(object instanceof ConfigurationAuthority) {
				ConfigurationAuthority o = (ConfigurationAuthority)object;
				return new DafConfigurationAuthority(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof ConfigurationArea) {
				ConfigurationArea o = (ConfigurationArea)object;
				return new DafConfigurationArea(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else if(object instanceof IntegerValueRange) {
				IntegerValueRange o = (IntegerValueRange)object;
				return new DafIntegerValueRange(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getConversionFactor(),
						o.getMaximum(),
						o.getMinimum(),
						o.getUnit()
				);
			}
			else if(object instanceof IntegerValueState) {
				IntegerValueState o = (IntegerValueState)object;
				return new DafIntegerValueState(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), o.getValue()
				);
			}
			else if(object instanceof MutableSet) {
				MutableSet o = (MutableSet)object;
				return new DafMutableSet(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), getIdsAsLongArrayList(o.getElements())
				);
			}
			else if(object instanceof NonMutableSet) {
				NonMutableSet o = (NonMutableSet)object;
				return new DafNonMutableSet(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), getIdsAsLongArrayList(o.getElements())
				);
			}
			else if(object instanceof ObjectSetUse) {
				ObjectSetUse o = (ObjectSetUse)object;
				return new DafObjectSetUse(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getObjectSetName(),
						o.getObjectSetType().getId(),
						o.isRequired()
				);
			}
			else if(object instanceof ObjectSetType) {
				ObjectSetType o = (ObjectSetType)object;
				return new DafObjectSetType(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						o.isNameOfObjectsPermanent(),
						getIds(o.getObjectSets()),
						o.getMinimumElementCount(),
						o.getMaximumElementCount(),
						o.isMutable()
				);
			}
			else if(object instanceof ConfigurationObjectType) {
				ConfigurationObjectType o = (ConfigurationObjectType)object;
				return new DafConfigurationObjectType(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), o.isNameOfObjectsPermanent()
				);
			}
			else if(object instanceof DynamicObjectType) {
				DynamicObjectType o = (DynamicObjectType)object;
				return new DafDynamicObjectType(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets()), o.isNameOfObjectsPermanent()
				);
			}
			else if(object instanceof SystemObjectType) {
				_debug.warning("Ung�ltiger Typ der weder konfigurierend noch dynamisch ist " + object);
				return null;
			}
			else if(object instanceof AttributeGroupUsage) {
				AttributeGroupUsage o = (AttributeGroupUsage)object;
				return new DafAttributeGroupUsage(
						o.getId(),
						o.getPid(),
						o.getName(),
						o.getType().getId(),
						state,
						//state 1=existent
						null,
						//error
						null,
						//DataModel
						o.getValidSince(),
						o.getNotValidSince(),
						o.getConfigurationArea().getId(),
						getIds(o.getObjectSets()),
						o.getAttributeGroup(),
						o.getAspect(),
						o.isExplicitDefined(),
						o.getUsage()
				);
			}
			else if(object instanceof DavApplication) {
				ConfigurationObject o = (ConfigurationObject)object;
				return new DafDavApplication(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}
			else {
				ConfigurationObject o = (ConfigurationObject)object;
				return new DafConfigurationObject(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), state, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), o.getConfigurationArea().getId(), getIds(o.getObjectSets())
				);
			}

		}
		else if(object instanceof DynamicObject) {
			final ConfigurationArea area = object.getConfigurationArea();
			if(object instanceof ClientApplication) {
				ClientApplication o = (ClientApplication)object;
				return new DafClientApplication(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), o.isValid() ? (byte)1 : (byte)0, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), area == null ? 0 : area.getId()
				);
			}
			else {
				DynamicObject o = (DynamicObject)object;
				return new DafDynamicObject(
						o.getId(), o.getPid(), o.getName(), o.getType().getId(), o.isValid() ? (byte)1 : (byte)0, //state 1=existent
						null, //error
						null, //DataModel
						o.getValidSince(), o.getNotValidSince(), area == null ? 0 : area.getId()
				);
			}

		}
		else {
			_debug.warning("Keine Objekt-Konvertierung m�glich: " + object);
			return null;
		}
	}
}
