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

import com.koch.ambeth.util.collections.AbstractTuple2KeyHashMap;

public interface IXmlTypeRegistry {
	Class<?> getType(String name, String namespace);

	/**
	 * Provides a valid name if the given one is null, empty or "##default"
	 *
	 * @param type
	 * @param providedName
	 * @return
	 */
	String getXmlTypeName(Class<?> type, String providedName);

	/**
	 * Provides a valid namespace if the given one is empty or "##default"
	 *
	 * @param type
	 * @param providedNamespace
	 * @return
	 */
	String getXmlTypeNamespace(Class<?> type, String providedNamespace);

	IXmlTypeKey getXmlType(Class<?> type);

	IXmlTypeKey getXmlType(Class<?> type, boolean expectExisting);

	AbstractTuple2KeyHashMap<String, String, Class<?>> createSnapshot();
}
