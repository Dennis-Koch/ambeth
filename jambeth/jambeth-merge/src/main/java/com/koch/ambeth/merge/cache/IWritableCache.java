package com.koch.ambeth.merge.cache;

/*-
 * #%L
 * jambeth-merge
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

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IWritableCache extends ICache
{
	int getCacheId();

	void clear();

	void setCacheId(int cacheId);

	List<Object> put(Object objectToCache);

	void remove(List<IObjRef> oris);

	void remove(IObjRef ori);

	void remove(Class<?> type, Object id);

	void removePriorVersions(List<IObjRef> oris);

	void removePriorVersions(IObjRef ori);
}
