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

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;

/**
 * Extension to provide the value of a relational propertyd of an entity. This is used in cases
 * where a previous call to the corresponding {@link ICacheRetriever} did not provide the object
 * references (instances of {@link com.koch.ambeth.merge.model.IObjRef}) of this relation in the
 * {@link com.koch.ambeth.service.cache.model.ILoadContainer} for whatever reason. In most cases the
 * relational information has been omitted intentionally in order to provide a lazy-loading pattern
 * to address potential performance concerns.<br>
 *
 * An implementation of this extension can link very easily to the provided extension point with the
 * Link-API of the IoC container: <code><br><br>
 * IBeanContextFactory bcf = ...<br>
 * IBeanConfiguration myRelationRetriever = bcf.registerBean(MyRelationRetriever.class);<br>
 * bcf.link(myRelationRetriever).to(IRelationRetrieverExtendable.class).with(MyEntity.class, "MyLazilyLoadedProp");
 * </code><br>
 */
public interface IRelationRetriever {
	List<IObjRelationResult> getRelations(List<IObjRelation> objPropertyKeys);
}
