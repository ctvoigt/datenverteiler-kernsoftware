/*
 * Copyright 2007 by Kappich Systemberatung, Aachen
 * Copyright 2005 by Kappich+Kni� Systemberatung Aachen (K2S)
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

package de.bsvrz.dav.daf.communication.dataRepresentation;

import de.bsvrz.dav.daf.communication.dataRepresentation.datavalue.DataValue;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeListDefinition;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.DoubleAttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.StringAttributeType;
import de.bsvrz.dav.daf.main.config.TimeAttributeType;

import java.util.*;

/**
 * Diese Klasse stellt Methoden zur Verf�gung, um die Interfaces der Konfiguration zu entschlacken.
 *
 * @author Kappich Systemberatung
 * @version $Revision: 5084 $
 */
public class AttributeHelper {
	/**
	 * Gibt den Typ des korrespondierenden Datensatzes zur�ck. Gibt die Kodierung der folgenden Typen zur�ck:
	 * <ul> <li>{@link StringAttributeType} f�r Zeichenketten,</li> <li>{@link de.bsvrz.dav.daf.main.config.IntegerAttributeType} f�r Ganze
	 * Zahlen,</li> <li>{@link DoubleAttributeType} f�r Flie�kommazahlen,</li> <li>{@link TimeAttributeType} f�r
	 * Zeitstempel und</li> <li>{@link ReferenceAttributeType} f�r Objekt-Referenzen und</li> <li>{@link
	 * de.bsvrz.dav.daf.main.config.AttributeListDefinition} f�r Attributlisten in strukturierten Attributgruppen.</li> </ul>
	 *
	 * @param attributeType Der Attributtyp.
	 * @param isArray       ob es sich um ein Array handelt
	 * @return der Typ des korrespondierenden Datensatzes
	 */
	public static byte getDataValueType(AttributeType attributeType, boolean isArray) {
		try {
			if (attributeType instanceof StringAttributeType) {
				return (byte) (DataValue.STRING_TYPE + (isArray ? DataValue.ARRAY_OFFSET : 0));
			} else if (attributeType instanceof IntegerAttributeType) {
				IntegerAttributeType type = (IntegerAttributeType) attributeType;
				byte val = isArray ? DataValue.ARRAY_OFFSET : 0;
				switch (type.getByteCount()) {
					case 1:
						return (byte) (DataValue.BYTE_TYPE + val);
					case 2:
						return (byte) (DataValue.SHORT_TYPE + val);
					case 4:
						return (byte) (DataValue.INTEGER_TYPE + val);
					case 8:
						return (byte) (DataValue.LONG_TYPE + val);
					default:
						throw new RuntimeException("Falsche Anzahl von Bytes");
				}
			} else if (attributeType instanceof DoubleAttributeType) {
				DoubleAttributeType type = (DoubleAttributeType) attributeType;
				return (byte) ((isArray ? DataValue.ARRAY_OFFSET : 0) + (type.getAccuracy() == DoubleAttributeType.FLOAT ? DataValue.FLOAT_TYPE : DataValue.DOUBLE_TYPE));
			} else if (attributeType instanceof TimeAttributeType) {
				TimeAttributeType type = (TimeAttributeType) attributeType;
				return (byte) ((type.getAccuracy() == TimeAttributeType.SECONDS ? DataValue.INTEGER_TYPE : DataValue.LONG_TYPE) + (isArray ? DataValue.ARRAY_OFFSET : 0));
			} else if (attributeType instanceof ReferenceAttributeType) {
				return (byte) (DataValue.LONG_TYPE + (isArray ? DataValue.ARRAY_OFFSET : 0));
			} else if (attributeType instanceof AttributeListDefinition) {
				return (byte) (DataValue.ATTRIBUTE_LIST_TYPE + (isArray ? DataValue.ARRAY_OFFSET : 0));
			} else {
				throw new IllegalStateException("Dieser Attributtyp wird nicht behandelt.");
			}
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Liefert eine Liste Stellvertreterobjekte von allen Attributen und Attributlisten dieser Attributmenge
	 * zur�ck. Die Reihenfolge der Attribute in der Liste entspricht der durch die {@link Attribute#getPosition
	 * Position} der Attribute definierte Reihenfolge innerhalb der Attributgruppe bzw. Attributliste
	 *
	 * @return Liste von {@link Attribute Attributen} und {@link de.bsvrz.dav.daf.main.config.AttributeListDefinition AttributeListen}
	 */
	public static List<AttributeBaseValue> getAttributesValues(final AttributeSet attributeSet) {
		List<Attribute> attributes = attributeSet.getAttributes();
		List<AttributeBaseValue> attributeValues = new ArrayList<AttributeBaseValue>(attributes.size());
		final DataModel dataModel = attributeSet.getDataModel();
		for (Attribute attribute : attributes) {
			AttributeType attributeType = (AttributeType) attribute.getAttributeType();
			AttributeBaseValue attributeBaseValue;
			if (attributeType instanceof AttributeListDefinition) {
				attributeBaseValue = new AttributeListValue(dataModel, attribute);
			} else {
				attributeBaseValue = new AttributeValue(dataModel, attribute);
			}
			attributeValues.add(attributeBaseValue);
		}
		return attributeValues;
	}
}
