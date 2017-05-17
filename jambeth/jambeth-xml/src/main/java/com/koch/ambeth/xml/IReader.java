package com.koch.ambeth.xml;

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

import com.koch.ambeth.xml.pending.ICommandTypeExtendable;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;

public interface IReader {
	boolean isEmptyElement();

	String getAttributeValue(String attributeName);

	Object readObject();

	Object readObject(Class<?> returnType);

	String getElementName();

	String getElementValue();

	boolean nextTag();

	boolean nextToken();

	boolean isStartTag();

	void moveOverElementEnd();

	Object getObjectById(int id);

	Object getObjectById(int id, boolean checkExistence);

	void putObjectWithId(Object obj, int id);

	void putMembersOfType(Class<?> type, SpecifiedMember[] members);

	SpecifiedMember[] getMembersOfType(Class<?> type);

	void addObjectCommand(IObjectCommand pendingSetter);

	// IReader contains the registry because the reader in fact is the deserialization state.
	ICommandTypeRegistry getCommandTypeRegistry();

	ICommandTypeExtendable getCommandTypeExtendable();

	boolean isSkipClassNotFoundOnRead();
}
