package com.koch.ambeth.cache.rootcachevalue;

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

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;

public class DefaultRootCacheValue extends RootCacheValue {
	protected Object[] primitives;

	protected IObjRef[][] relations;

	protected Object id;

	protected Object version;

	protected IEntityMetaData metaData;

	public DefaultRootCacheValue(IEntityMetaData metaData) {
		super(metaData);
		this.metaData = metaData;
	}

	@Override
	public IEntityMetaData get__EntityMetaData() {
		return metaData;
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setId(Object id) {
		this.id = id;
	}

	@Override
	public Object getVersion() {
		return version;
	}

	@Override
	public void setVersion(Object version) {
		this.version = version;
	}

	@Override
	public void setPrimitives(Object[] primitives) {
		this.primitives = primitives;
	}

	@Override
	public Object[] getPrimitives() {
		return primitives;
	}

	@Override
	public Object getPrimitive(int primitiveIndex) {
		return primitives[primitiveIndex];
	}

	@Override
	public IObjRef[][] getRelations() {
		return relations;
	}

	@Override
	public void setRelations(IObjRef[][] relations) {
		this.relations = relations;
	}

	@Override
	public IObjRef[] getRelation(int relationIndex) {
		return relations[relationIndex];
	}

	@Override
	public void setRelation(int relationIndex, IObjRef[] relationsOfMember) {
		relations[relationIndex] = relationsOfMember;
	}

	@Override
	public Class<?> getEntityType() {
		return metaData.getEntityType();
	}
}
