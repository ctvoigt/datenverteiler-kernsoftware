/*
 * Copyright 2004 by Kappich+Kni� Systemberatung Aachen (K2S)
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
package de.bsvrz.dav.daf.communication.dataRepresentation.data.info.version1;

import de.bsvrz.dav.daf.communication.dataRepresentation.data.info.AttributeInfo;
import de.bsvrz.dav.daf.main.config.Attribute;
import de.bsvrz.dav.daf.main.config.AttributeSet;
import de.bsvrz.dav.daf.main.config.ConfigurationException;
import de.bsvrz.dav.daf.main.config.AttributeType;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

/**
 * Klasse, die noch zu dokumentieren ist.
 *
 * @author Kappich+Kni� Systemberatung Aachen (K2S)
 * @author Roland Schmitz (rs)
 * @version $Revision: 5052 $ / $Date: 2007-08-31 20:02:55 +0200 (Fri, 31 Aug 2007) $ / ($Author: rs $)
 */
public class AttributeSetDefinitionInfo extends AbstractAttributeDefinitionInfo {

	private final AttributeSet _attributeSet;
	private final AttributeInfo[] _subAttributeInfoArray;
	private final Map _subAttributeInfoMap;
	protected final boolean _sizeFixed;
	protected final int _fixedSize;


	public AttributeSetDefinitionInfo(final AttributeSet attributeSet) {
		_attributeSet = attributeSet;
		boolean hasFixedSize = true;
		int fixedSize = 0;
		try {
			List attributes = attributeSet.getAttributes();
			int attributeCount = attributes.size();
			_subAttributeInfoArray = new AttributeInfo[attributeCount];
			_subAttributeInfoMap = new HashMap(attributeCount);
			int i = 0;
			int offset = 0;
			AttributeInfo offsetReferral = null;
			for(Iterator iterator = attributes.iterator(); iterator.hasNext();) {
				Attribute attribute = (Attribute)iterator.next();
				final AttributeInfo attributeInfo = AbstractAttributeInfo.forAttribute(attribute, offset, offsetReferral);
				_subAttributeInfoArray[i++] = attributeInfo;
				_subAttributeInfoMap.put(attribute.getName(), attributeInfo);
				if(attributeInfo.isSizeFixed()) {
					offset += attributeInfo.getFixedSize();
				}
				else {
					offset = 0;
					offsetReferral = attributeInfo;
				}
				if(hasFixedSize) {
					if(attributeInfo.isSizeFixed()) {
						fixedSize += attributeInfo.getFixedSize();
					}
					else {
						hasFixedSize = false;
						fixedSize = 0;
					}
				}
			}
			_sizeFixed = hasFixedSize;
			_fixedSize = fixedSize;
		}
		catch(ConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public void dump(int indent) {
		//for(int i=0; i <indent; ++i) System.out.print(" ");
		System.out.println(getClass().getName() + "(" + _attributeSet.getPid() + ")" + (isSizeFixed() ? " fixedSize: " + getFixedSize() : "variableSize"));
		for(int i = 0; i < _subAttributeInfoArray.length; i++) {
			AttributeInfo attributeInfo = _subAttributeInfoArray[i];
			attributeInfo.dump(indent + 1);
		}
	}

	public boolean isSizeFixed() {
		return _sizeFixed;
	}

	public int getFixedSize() {
		return _fixedSize;
	}

	public AttributeType getAttributeType() {
		if(_attributeSet instanceof AttributeType) {
			return (AttributeType)_attributeSet;
		}
		return null;
	}

	public boolean isList() {
		return true;
	}

	public AttributeInfo getItem(String name) {
		AttributeInfo subInfo = (AttributeInfo)_subAttributeInfoMap.get(name);
		if(subInfo != null) return subInfo;
		throw new IllegalArgumentException("getItem(\"" + name + "\"): Es gibt kein Sub-Attribut mit diesem Namen unterhalb von " + _attributeSet.getPid());
	}

	public AttributeInfo getItem(int itemIndex) {
		return _subAttributeInfoArray[itemIndex];
	}

	public int getItemCount() {
		return _subAttributeInfoArray.length;
	}

	public int getSize(byte[] bytes, int offset) {
		if(_sizeFixed) return _fixedSize;
		final AttributeInfo lastSubInfo = _subAttributeInfoArray[_subAttributeInfoArray.length-1];
		final int lastSubInfoOffset = lastSubInfo.getAbsoluteOffset(bytes, offset);
		final int lastSubInfoSize = lastSubInfo.getSize(bytes, lastSubInfoOffset);
//		System.out.println("AttributeSetDefinitionInfo.getSize(..," + offset);
//		System.out.println("lastSubInfo.getName() = " + lastSubInfo.getName());
//		System.out.println("lastSubInfoOffset = " + lastSubInfoOffset);
//		System.out.println("lastSubInfoSize = " + lastSubInfoSize);
		return lastSubInfoOffset + lastSubInfoSize - offset;

	}

	public String getValueText(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("AttributeSetDefinitionInfo.getValueText()");
	}

	public String getSuffixText(byte[] bytes, int offset) {
		throw new UnsupportedOperationException("AttributeSetDefinitionInfo.getSuffixText()");
	}
 }
