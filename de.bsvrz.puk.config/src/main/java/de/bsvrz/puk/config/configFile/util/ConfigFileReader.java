/*
 * Copyright 2011 by Kappich Systemberatung Aachen
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

package de.bsvrz.puk.config.configFile.util;

import de.bsvrz.puk.config.configFile.fileaccess.ConfigAreaFile;
import de.bsvrz.puk.config.configFile.fileaccess.ConfigFileHeaderInfo;
import de.bsvrz.sys.funclib.dataSerializer.Deserializer;
import de.bsvrz.sys.funclib.dataSerializer.NoSuchVersionException;
import de.bsvrz.sys.funclib.dataSerializer.SerializingFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

/**
 * @author Kappich Systemberatung
 * @version $Revision: 8953 $
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class ConfigFileReader {

	private static final boolean SHOW_GAP_INFO = false;

	private ConfigFileHeaderInfo _configFileHeaderInfo;

	private File _configAreaFile;

	private int _serializerVersion;

	final Map<Long, Long> _idMap = new HashMap<Long, Long>();

	final Map<Long, Integer> _pidMap = new HashMap<Long, Integer>();

	final List<String> _errorList = new ArrayList<String>();

	public ConfigFileReader(final File configAreaFile) throws IOException, NoSuchVersionException {
		System.out.println("==== Datei-Informationen ====");
		_configAreaFile = configAreaFile;
		System.out.println("Dateiname:  " + _configAreaFile.getName());
		System.out.println("Dateigr��e: " + _configAreaFile.length());
		System.out.println();
		System.out.println();
		System.out.println("==== Header ====");
		_configFileHeaderInfo = new ConfigFileHeaderInfo(_configAreaFile);
		_serializerVersion = _configFileHeaderInfo.getSerializerVersion();
		System.out.println("L�nge des Headers:              " + _configFileHeaderInfo.getHeaderSize());
		System.out.println("Ende des Headers:               " + _configFileHeaderInfo.getHeaderEnd());
		System.out.println("Pid:                            " + _configFileHeaderInfo.getConfigurationAreaPid());
		System.out.println("SerializerVersion:              " + _serializerVersion);
		System.out.println("Aktive Version (Header):        " + _configFileHeaderInfo.getActiveVersionFile());
		System.out.println("N�chste Version (Header):       " + _configFileHeaderInfo.getNextActiveVersionFile());
		System.out.println("N�chste Version (real):         " + (_configFileHeaderInfo.getNextInvalidBlockVersion()));
		System.out.println("Objektversion:                  " + _configFileHeaderInfo.getObjectVersion());
		System.out.println("Konfigurationsdaten ge�ndert:   " + _configFileHeaderInfo.getConfigurationDataChanged());
		System.out.println("Konfigurationsobjekte ge�ndert: " + _configFileHeaderInfo.getConfigurationObjectChanged());
		System.out.println();
		System.out.println("Offset alte dynamische Objekte: " + _configFileHeaderInfo.getStartOldDynamicObjects());
		System.out.println("Offset Id-Index:                " + _configFileHeaderInfo.getStartIdIndex());
		System.out.println("Offset Pid-Hashcode-Index:      " + _configFileHeaderInfo.getStartPidHashCodeIndex());
		System.out.println("Offset Mixed Objects:           " + _configFileHeaderInfo.getStartMixedSet());
		System.out.println();
		System.out.println();

		assertEquals(_configFileHeaderInfo.getHeaderEnd(), _configFileHeaderInfo.getHeaderSize() + 4, "Falsche Header-L�nge");

		assertEquals(_configFileHeaderInfo.getActiveVersionFile(), (_configFileHeaderInfo.getNextInvalidBlockVersion() - 1), "Falsche aktive Version");

		readOldConfigBlocks();
		readOldDynamicBlock();
		readIndex();
		readMixedObjectSetObjects();
		System.out.println("==== Gefundene Fehler ====");
		System.out.println("Anzahl: " + _errorList.size());
		for(final String s : _errorList) {
			System.out.println(s);
		}
	}

	private void readOldDynamicBlock() throws IOException, NoSuchVersionException {
		System.out.println("==== Block mit alten dynamischen Objekten ====");
		readOldObjectBlock(
				_configFileHeaderInfo.getStartOldDynamicObjects() + _configFileHeaderInfo.getHeaderEnd(),
				Integer.MAX_VALUE,
				(_configFileHeaderInfo.getStartIdIndex() + _configFileHeaderInfo.getHeaderEnd())
		);
		System.out.println();
		System.out.println();
	}

	private void readOldConfigBlocks() throws IOException, NoSuchVersionException {
		System.out.println("==== Bl�cke mit alten Konfigurationsobjekten ====");
		for(int i = 2; i < _configFileHeaderInfo.getNextInvalidBlockVersion(); i++) {
			ConfigAreaFile.OldBlockInformations block = _configFileHeaderInfo.getOldObjectBlocks().get((short)i);
			System.out.println("  == Objekte ung�ltig in Version " + i);
			if(block != null) {
				System.out.println(
						"  Aktivierungszeit:  " + new SimpleDateFormat().format(
								new Date(block.getTimeStamp())
						)
				);
				System.out.println("  Relative Position: " + block.getFilePosition());
				System.out.println();
				if(block.getFilePosition() >= 0) {
					readOldObjectBlock(
							block.getFilePosition() + _configFileHeaderInfo.getHeaderEnd(),
							i,
							(_configFileHeaderInfo.getStartOldDynamicObjects() + _configFileHeaderInfo.getHeaderEnd())
					);
				}
				else {
					System.out.println("  L�cke");
				}
			}
			else {
				System.out.println("Keine Informationen");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}

	private void readOldObjectBlock(final long filePosition, final int version, final long readEnd) throws IOException, NoSuchVersionException {
		final RandomAccessFile input = new RandomAccessFile(_configAreaFile, "r");
		try {
			input.seek(filePosition);
			// Es m�ssen solange Daten gelesen werden, bis der dynamische nGa-Bereich erreicht wird

			// Solange Daten aus den nGa-Bl�cken lesen, bis alle nGa gepr�ft wurden
			while(input.getFilePointer() < readEnd) {

				long pos = input.getFilePointer();
				final int len = input.readInt();
				final long id = input.readLong();
				final int pidHashCode = input.readInt();

				final long typeId = input.readLong();

				// 0 = Konfobjekt, 1 = dyn Objekt
				final byte objectType = input.readByte();

				final long firstInvalidVersion;
				final long firstValidVersion;

				if(objectType == 0) {
					firstInvalidVersion = input.readShort();
					firstValidVersion = input.readShort();
				}
				else {
					firstInvalidVersion = input.readLong();
					firstValidVersion = input.readLong();
				}

				if(firstInvalidVersion > version) return;

				_idMap.put(pos, id);
				_pidMap.put(pos, pidHashCode);

				System.out.println("L�nge:               " + len);
				System.out.println("Id:                  " + id);
				System.out.println("PidHashCode:         " + pidHashCode);
				System.out.println("Objekttyp-Id:        " + typeId);
				System.out.println("Objekttyp:           " + objectType + " (0 = Konfigurationsobjekt, 1 = Dynamisches Objekt)");
				System.out.println("G�ltige Version:     " + firstValidVersion);
				System.out.println("Ung�ltige Version:   " + firstInvalidVersion);
				readObjectFromFile(len, objectType, input);
				System.out.println();
			}
			assertEquals(input.getFilePointer(), readEnd, "Ende des Blocks stimmt nicht");
		}
		finally {
			input.close();
		}
	}

	private void readObjectFromFile(
			final int objectsize, final byte objecttype, RandomAccessFile file) throws IOException, NoSuchVersionException {

		if(objecttype == 0) {
			// Konfigurationsobjekt

			// Der vordere Teil ist konstant, also kann die L�nge der gepackten Daten berechnet werden.
			// id, pidHash, typeId, type(Konf oder dynamische), Version, Version abziehen
			final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 2 - 2;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			file.read(packedBytes);

			// Byte-Array, das die ungepackten Daten enth�lt
			final byte[] unpackedBytes = unzip(packedBytes);

			final InputStream in = new ByteArrayInputStream(unpackedBytes);

			//deserialisieren
			final Deserializer deserializer = SerializingFactory.createDeserializer(_serializerVersion, in);

			// Das serialisierte SystemObjektInfo einlesen

			final int pidSize = deserializer.readUnsignedByte();
			final String pid;
			if(pidSize > 0) {
				pid = deserializer.readString(255);
			}
			else {
				pid = deserializer.readString(0);
			}

//				final String pid = readString(deserializer);

			// Name einlesen
			final int nameSize = deserializer.readUnsignedByte();
			final String name;
			if(nameSize > 0) {
				name = deserializer.readString(255);
			}
			else {
				name = deserializer.readString(0);
			}
			System.out.println("Name:                " + name);
			System.out.println("Pid:                 " + pid);


			// Menge der konfigurierenden Datens�tze
			final int numberOfConfigurationData = deserializer.readInt();
			for(int nr = 0; nr < numberOfConfigurationData; nr++) {
				// ATG-Verwendung einlesen
				final long atgUseId = deserializer.readLong();
				// L�nge der Daten
				final int sizeOfData = deserializer.readInt();
				final byte[] data = deserializer.readBytes(sizeOfData);
				System.out.println("Konfigurationsdaten: " + data.length + " bytes, ATGU: " + atgUseId);
			}

			// alle Daten einlesen, die spezifisch f�r ein Konfigurationsobjekt sind und
			// direkt am Objekt hinzuf�gen

			// Anzahl Mengen am Objekt
			final short numberOfSets = deserializer.readShort();

			for(int nr = 0; nr < numberOfSets; nr++) {
				final long setId = deserializer.readLong();
				System.out.println("SetId:               " + setId);
				final int numberOfObjects = deserializer.readInt();

				for(int i = 0; i < numberOfObjects; i++) {
					// Alle Objekte der Menge einlesen

					// Id des Objekts, das sich in Menge befinden
					final long setObjectId = deserializer.readLong();
					System.out.println("SetObjectId:         " + setObjectId);
				}
			}
		}
		else if(objecttype == 1) {
			// Ein dynamisches Objekt einlesen, die Simulationsvariante wurde noch nicht eingelesen, aber der fileDesc.
			// steht sofort auf dem Wert
			final short simulationVariant = file.readShort();

			// Der vordere Teil ist konstant, also kann die L�nge der gepackten Daten berechnet werden.
			// id, pidHash, typeId, type(Konf oder dynamisch), Zeitstempel, Zeitstempel, Simulationsvariante abziehen
			final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 8 - 8 - 2;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			file.readFully(packedBytes);

			// Byte-Array, das die ungepackten Daten enth�lt
			final byte[] unpackedBytes = unzip(packedBytes);

			final InputStream in = new ByteArrayInputStream(unpackedBytes);

			//deserialisieren
			final Deserializer deserializer = SerializingFactory.createDeserializer(_serializerVersion, in);

			// Das serialisierte SystemObjektInfo einlesen

			// Pid einlesen
			final int pidSize = deserializer.readUnsignedByte();
			final String pid;
			if(pidSize > 0) {
				pid = deserializer.readString(255);
			}
			else {
				pid = deserializer.readString(0);
			}
//				final String pid = readString(deserializer);

			// Name einlesen
			final int nameSize = deserializer.readUnsignedByte();
			final String name;
			if(nameSize > 0) {
				name = deserializer.readString(255);
			}
			else {
				name = deserializer.readString(0);
			}

			System.out.println("Name:                " + name);
			System.out.println("Pid:                 " + pid);
			System.out.println("Simulationsvariante: " + simulationVariant);

			final int numberOfConfigurationData = deserializer.readInt();
			for(int nr = 0; nr < numberOfConfigurationData; nr++) {
				// ATG-Verwendung einlesen
				final long atgUseId = deserializer.readLong();
				// L�nge der Daten
				final int sizeOfData = deserializer.readInt();
				final byte[] data = deserializer.readBytes(sizeOfData);
				System.out.println("Konfigurationsdaten: " + data.length + " bytes, ATGU: " + atgUseId);
			}
		}
		else {
			final int sizeOfPackedData = objectsize - 8 - 4 - 8 - 1 - 8 - 8 - 2;
			final byte packedBytes[] = new byte[sizeOfPackedData];
			file.readFully(packedBytes);
		}
	}

	private byte[] unzip(byte[] zippedData) {

		ByteArrayInputStream inputStream = new ByteArrayInputStream(zippedData);
		InflaterInputStream unzip = new InflaterInputStream(inputStream);
		// In diesem Stream werden die entpackten Daten gespeichert
		ByteArrayOutputStream unzippedData = new ByteArrayOutputStream();

		try {
			// Die ungepackten Daten
			int unpackedData = unzip.read();

			while(unpackedData != -1) {
				unzippedData.write(unpackedData);
				unpackedData = unzip.read();
			}
			unzip.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return unzippedData.toByteArray();
	}


	private void readIndex() throws IOException {
		System.out.println("==== Id-Index ====");
		RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");
		try {
			file.seek(_configFileHeaderInfo.getStartIdIndex() + _configFileHeaderInfo.getHeaderEnd());
			while(file.getFilePointer() < (_configFileHeaderInfo.getStartPidHashCodeIndex() + _configFileHeaderInfo.getHeaderEnd())) {
				final long id = file.readLong();
				final long pos = getAbsoluteFilePositionForInvalidObjects(file.readLong());

				System.out.println("Id:       " + id);
				System.out.println("Position: " + pos);
				if(id != _idMap.get(pos)) {
					appendError("Id-Index verweise auf falsches Objekt. Erwartet: " + id + ". Ist: " + _idMap.get(pos));
				}

				System.out.println();
			}

			System.out.println();
			System.out.println();
			System.out.println("==== Pid-Index ====");

			// Pid Index einlesen
			file.seek(_configFileHeaderInfo.getStartPidHashCodeIndex() + _configFileHeaderInfo.getHeaderEnd());

			while(file.getFilePointer() < (_configFileHeaderInfo.getStartMixedSet() + _configFileHeaderInfo.getHeaderEnd())) {
				final long pid = file.readInt();
				final long pos = getAbsoluteFilePositionForInvalidObjects(file.readLong());

				System.out.println("Pid:      " + pid);
				System.out.println("Position: " + pos);
				if(pid != _pidMap.get(pos)) {
					appendError("Pid-Index verweise auf falsches Objekt. Erwartet: " + pid + ". Ist: " + _pidMap.get(pos));
				}
				System.out.println();
			}
		}
		finally {
			file.close();
		}
		System.out.println();
		System.out.println();
	}

	private void appendError(final String error) {
		_errorList.add(error);
	}

	private void assertEquals(final long a, final long b, final String error) {
		if(a != b) _errorList.add(error + ": " + a + " != " + b);
	}

	private long getAbsoluteFilePositionForInvalidObjects(long relativeFilePosition) {
		if(relativeFilePosition > 0) {
			// Es handelt sich um dynamisches Objekt, das sich in der dyn. nGa Menge befindet.
			// Die relative Positionsangabe bezieht sich auf den Beginn des dyn. nGa Bereichs.
			// Die relative Position ist immer um +1 erh�ht worden, damit wurde eine "doppelte 0" verhindert.
			// Die "0" geh�rt zu den Konfigurationsobjekten.
			return ((_configFileHeaderInfo.getStartOldDynamicObjects()) + relativeFilePosition + _configFileHeaderInfo.getHeaderEnd()) - 1;
		}
		else {
			// Es handelt sich um ein Konfigurationsobjekt. Die relative Position bezieht sich auf das
			// Headerende.
			return (relativeFilePosition * (-1) + _configFileHeaderInfo.getHeaderEnd());
		}
	}

	private void readMixedObjectSetObjects() throws IOException, NoSuchVersionException {

		System.out.println("==== Block mit aktuellen und zuk�nftigen Objekten ====");

		final long startingPosition;

		startingPosition = (_configFileHeaderInfo.getStartMixedSet() + _configFileHeaderInfo.getHeaderEnd());


		// Datei �ffnen
		final RandomAccessFile file = new RandomAccessFile(_configAreaFile, "r");

		try {

			// Datei auf den Anfang der Mischmenge postieren
			file.seek(startingPosition);

			// Wie gross ist die Datei
			final long fileSize = file.length();

			// Wird true, wenn das Objekt, das den Konfigurationsbereich wiederspiegelt, gefunden wurde
			while(file.getFilePointer() < fileSize) {

				// speichert die Dateiposition des Objekts. Diese Position wird sp�ter
				// am Objekt gespeichert
				final long startObjectFileDescriptor = file.getFilePointer();

				// L�nge des Blocks einlesen
				final int sizeOfObject = file.readInt();

				// Id des Objekts einlesen
				final long objectId = file.readLong();

				if(objectId > 0) {
					// Es ist ein Objekt und keine L�cke

					final int pidHashCode = file.readInt();

					final long typeId = file.readLong();

					// 0 = Konfobjekt, 1 = dyn Objekt
					final byte objectType = file.readByte();

					// Das kann entweder ein Zeitpunkt oder eine Version sein
					final long firstInvalid;
					final long firstValid;

					if(objectType == 0) {
						firstInvalid = file.readShort();
						firstValid = file.readShort();
					}
					else {
						firstInvalid = file.readLong();
						firstValid = file.readLong();
					}

					System.out.println("L�nge:               " + sizeOfObject);
					System.out.println("Id:                  " + objectId);
					System.out.println("PidHashCode:         " + pidHashCode);
					System.out.println("Objekttyp-Id:        " + typeId);
						System.out.println(
								"Objekttyp:           " + (objectType == 0
								                           ? "Konfigurationsobjekt"
								                           : objectType == 1 ? "Dynamisches Objekt" : "Unbekannter Objekttyp: " + objectType)
						);
					System.out.println("G�ltige Version:     " + firstValid);
					System.out.println("Ung�ltige Version:   " + firstInvalid);
					readObjectFromFile(
							sizeOfObject, objectType, file
					);
					System.out.println();
				}
				else {
					System.out.println("Gel�schtes Objekt:   " + sizeOfObject + " bytes");
					if(!SHOW_GAP_INFO) {
						// Eine L�cke, der filePointer muss verschoben werden.
						// Die L�nge bezieht sich auf das gesamte Objekt, ohne die L�nge selber.
						// Also ist die n�chste L�nge bei "aktuelle Position + L�nge - 8.
						// - 8, weil die Id bereits gelesen wurde und das ist ein Long.
						file.seek(file.getFilePointer() + sizeOfObject - 8);
					}
					else {
						final int pidHashCode = file.readInt();

						final long typeId = file.readLong();

						// 0 = Konfobjekt, 1 = dyn Objekt
						final byte objectType = file.readByte();

						// Das kann entweder ein Zeitpunkt oder eine Version sein
						final long firstInvalid;
						final long firstValid;

						if(objectType == 0) {
							firstInvalid = file.readShort();
							firstValid = file.readShort();
						}
						else {
							firstInvalid = file.readLong();
							firstValid = file.readLong();
						}
						System.out.println("PidHashCode:         " + pidHashCode);
						System.out.println("Objekttyp-Id:        " + typeId);
						System.out.println(
								"Objekttyp:           " + (objectType == 0
								                           ? "Konfigurationsobjekt"
								                           : objectType == 1 ? "Dynamisches Objekt" : "Unbekannter Objekttyp: " + objectType)
						);
						System.out.println("G�ltige Version:     " + firstValid);
						System.out.println("Ung�ltige Version:   " + firstInvalid);
						readObjectFromFile(
								sizeOfObject, objectType, file
						);
					}
					System.out.println();
				}
			}// while
		}
		finally {
			file.close();
		}
	}

	public static void main(String[] args) throws Exception {
		new ConfigFileReader(new File(args[0]));
	}
}
