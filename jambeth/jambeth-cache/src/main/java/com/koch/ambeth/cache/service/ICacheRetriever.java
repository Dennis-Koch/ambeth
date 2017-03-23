package com.koch.ambeth.cache.service;

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

import java.util.List;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.model.IObjRef;

/**
 * Extension to provide the payload for cache misses when called by a Root Cache
 * ({@link com.koch.ambeth.cache.RootCache}). In most cases the calling root cache is the 2nd level
 * cache of the application. This way any kind of data source can be used to fill fragments of the
 * federated information model:<br>
 * <ul>
 * <li>file system</li>
 * <li>web service</li>
 * <li>native library</li>
 * <li>ordinary JDBC connection</li>
 * <li>...be creative...</li>
 * </ul>
 *
 * An implementation of this extension can link very easily to the provided extension point with the
 * Link-API of the IoC container: <code><br><br>
 * IBeanContextFactory bcf = ...<br>
 * IBeanConfiguration myCacheRetriever = bcf.registerBean(MyCacheRetriever.class);<br>
 * bcf.link(myCacheRetriever).to(ICacheRetrieverExtendable.class).with(MyEntity.class);
 * </code><br>
 */
public interface ICacheRetriever extends IRelationRetriever {
	List<ILoadContainer> getEntities(List<IObjRef> orisToLoad);
}
