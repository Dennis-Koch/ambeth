package com.koch.ambeth.persistence;

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

import java.util.List;

import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.util.IParamHolder;

public interface IEntityHandler {
	Class<?> getEntityType();

	void queueDelete(Object id, Object version);

	Object queueInsert(Object id, IParamHolder<Object> newId, List<IPrimitiveUpdateItem> puis,
			List<IRelationUpdateItem> ruis);

	Object queueUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis,
			List<IRelationUpdateItem> ruis);

	void postProcessInsertAndUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis,
			List<IRelationUpdateItem> ruis, IAlreadyLinkedCache alreadyLinkedCache);

	Object[] acquireIds(int count);

	Object read(Object id, Object version);
}
