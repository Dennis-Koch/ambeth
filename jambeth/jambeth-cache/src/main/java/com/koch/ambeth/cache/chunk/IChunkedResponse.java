package com.koch.ambeth.cache.chunk;

/*-
 * #%L
 * jambeth-cache
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

@XmlType
public interface IChunkedResponse {
	IChunkedRequest getReference();

	byte[] getPayload();

	/**
	 * Returns the inflated size of the payload in bytes. If <code>isDeflated()</code> is false the
	 * returned value is equal to the length of the byte array of <code>getPayload()</code>
	 *
	 * @return the inflated size of the payload in bytes
	 */
	int getPayloadSize();

	boolean isDeflated();
}
