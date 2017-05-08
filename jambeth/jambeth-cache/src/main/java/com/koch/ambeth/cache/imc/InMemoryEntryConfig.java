package com.koch.ambeth.cache.imc;

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

import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.HashSet;

public class InMemoryEntryConfig implements IInMemoryConfig {
	protected final IEntityMetaData metaData;

	protected final LoadContainer lc;

	protected final InMemoryCacheRetriever inMemoryCacheRetriever;

	public InMemoryEntryConfig(InMemoryCacheRetriever inMemoryCacheRetriever,
			IEntityMetaData metaData, LoadContainer lc) {
		this.inMemoryCacheRetriever = inMemoryCacheRetriever;
		this.metaData = metaData;
		this.lc = lc;
	}

	@Override
	public IInMemoryConfig primitive(String memberName, Object value) {
		int primitiveIndex = metaData.getIndexByPrimitiveName(memberName);
		lc.getPrimitives()[primitiveIndex] = value;

		if (metaData.isAlternateId(metaData.getMemberByName(memberName))) {
			inMemoryCacheRetriever.addWithKey(lc, memberName, value);
		}
		return this;
	}

	@Override
	public IInMemoryConfig relation(String memberName, IObjRef... objRefs) {
		int relationIndex = metaData.getIndexByRelationName(memberName);
		lc.getRelations()[relationIndex] = objRefs;
		return this;
	}

	@Override
	public IInMemoryConfig relation(String memberName, IInMemoryConfig... inMemoryConfigs) {
		IObjRef[] objRefs = new IObjRef[inMemoryConfigs.length];
		for (int a = inMemoryConfigs.length; a-- > 0;) {
			objRefs[a] = ((InMemoryEntryConfig) inMemoryConfigs[a]).lc.getReference();
		}
		return relation(memberName, objRefs);
	}

	@Override
	public IInMemoryConfig addRelation(String memberName, IObjRef... objRefs) {
		int relationIndex = metaData.getIndexByRelationName(memberName);
		IObjRef[] existingObjRefs = lc.getRelations()[relationIndex];
		HashSet<IObjRef> existingObjRefsSet =
				HashSet.create((existingObjRefs != null ? existingObjRefs.length : 0) + objRefs.length);
		if (existingObjRefs != null) {
			existingObjRefsSet.addAll(existingObjRefs);
		}
		existingObjRefsSet.addAll(objRefs);
		lc.getRelations()[relationIndex] = existingObjRefsSet.toArray(IObjRef.class);
		return this;
	}

	@Override
	public IInMemoryConfig addRelation(String memberName, IInMemoryConfig... inMemoryConfigs) {
		IObjRef[] objRefs = new IObjRef[inMemoryConfigs.length];
		for (int a = inMemoryConfigs.length; a-- > 0;) {
			objRefs[a] = ((InMemoryEntryConfig) inMemoryConfigs[a]).lc.getReference();
		}
		return addRelation(memberName, objRefs);
	}
}
