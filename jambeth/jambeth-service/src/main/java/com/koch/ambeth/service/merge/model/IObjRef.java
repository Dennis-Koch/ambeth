package com.koch.ambeth.service.merge.model;

import com.koch.ambeth.service.metadata.IDTOType;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.util.annotation.XmlType;

/**
 * Contains reference information about an Object in the cache or not loaded yet. By use of these
 * information, a specific Object is uniquely defined and can be loaded from the cache.
 *
 * @see com.koch.ambeth.merge.cache.ICache#getObject(IObjRef, java.util.Set)
 */
@XmlType
public interface IObjRef extends IDTOType {
	public static final IObjRef[] EMPTY_ARRAY = new IObjRef[0];

	public static final IObjRef[][] EMPTY_ARRAY_ARRAY = new IObjRef[0][0];

	byte getIdNameIndex();

	void setIdNameIndex(byte idNameIndex);

	Object getId();

	void setId(Object id);

	Object getVersion();

	void setVersion(Object version);

	Class<?> getRealType();

	void setRealType(Class<?> realType);
}
