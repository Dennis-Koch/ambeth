package com.koch.ambeth.persistence.util;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;

public class AlreadyLinkedCache implements IAlreadyLinkedCache, IDisposableBean {
	protected final Tuple3KeyHashMap<ILink, Object, Object, Boolean> keyToObjectMap =
			new Tuple3KeyHashMap<>();

	@Override
	public void destroy() throws Throwable {
		clear();
	}

	@Override
	public void clear() {
		keyToObjectMap.clear();
	}

	@Override
	public boolean containsKey(ILink link, Object leftRecId, Object rightRecId) {
		return keyToObjectMap.containsKey(link, leftRecId, rightRecId);
	}

	@Override
	public boolean removeKey(ILink link, Object leftRecId, Object rightRecId) {
		Boolean value = keyToObjectMap.remove(link, leftRecId, rightRecId);
		return value != null;
	}

	@Override
	public boolean put(ILink link, Object[] recIdRecord) {
		return put(link, recIdRecord[0], recIdRecord[1]);
	}

	@Override
	public boolean put(ILink link, Object leftRecId, Object rightRecId) {
		return keyToObjectMap.putIfNotExists(link, leftRecId, rightRecId, Boolean.TRUE);
	}
}
