package com.koch.ambeth.xml.typehandler;

/*-
 * #%L
 * jambeth-xml
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.ITypeBasedHandler;
import com.koch.ambeth.xml.IWriter;

public class NumberTypeHandler extends AbstractHandler implements ITypeBasedHandler {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void writeObject(Object obj, Class<?> type, IWriter writer) {
		writer.writeAttribute(xmlDictionary.getValueAttribute(), obj.toString());
	}

	@Override
	public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader) {
		String value = reader.getAttributeValue(xmlDictionary.getValueAttribute());
		if (Double.class.equals(objType) || Double.TYPE.equals(objType)) {
			return Double.valueOf(value);
		}
		else if (Long.class.equals(objType) || Long.TYPE.equals(objType)) {
			return Long.valueOf(value);
		}
		else if (Float.class.equals(objType) || Float.TYPE.equals(objType)) {
			return Float.valueOf(value);
		}
		else if (Integer.class.equals(objType) || Integer.TYPE.equals(objType)) {
			return Integer.valueOf(value);
		}
		else if (Short.class.equals(objType) || Short.TYPE.equals(objType)) {
			return Short.valueOf(value);
		}
		else if (Byte.class.equals(objType) || Byte.TYPE.equals(objType)) {
			return Byte.valueOf(value);
		}
		else if (Boolean.class.equals(objType) || Boolean.TYPE.equals(objType)) {
			return Boolean.valueOf(value);
		}
		else if (Character.class.equals(objType) || Character.TYPE.equals(objType)) {
			if (value.length() == 0) {
				return Character.valueOf('\0');
			}
			return Character.valueOf(value.charAt(0));
		}
		throw new IllegalStateException("Type not supported: " + objType);
	}
}
