package com.koch.ambeth.merge.incremental;

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

import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ICUDResultHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.IdentityHashMap;

public class IncrementalMergeState implements IIncrementalMergeState {
	public static class StateEntry {
		public final Object entity;

		public final IObjRef objRef;

		public final int index;

		public StateEntry(Object entity, IObjRef objRef, int index) {
			this.entity = entity;
			this.objRef = objRef;
			this.index = index;
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected ICUDResultHelper cudResultHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Property
	protected ICache stateCache;

	public final IdentityHashMap<Object, StateEntry> entityToStateMap =
			new IdentityHashMap<>();

	public final HashMap<IObjRef, StateEntry> objRefToStateMap = new HashMap<>();

	private final HashMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap =
			new HashMap<>();

	private final HashMap<Class<?>, HashMap<String, Integer>> typeToPrimitiveMemberNameToIndexMap =
			new HashMap<>();

	private final Lock writeLock = new ReentrantLock();

	public final Comparator<IObjRef> objRefComparator = new Comparator<IObjRef>() {
		@Override
		public int compare(IObjRef o1, IObjRef o2) {
			int result = o1.getRealType().getName().compareTo(o2.getRealType().getName());
			if (result != 0) {
				return result;
			}
			String o1_id = conversionHelper.convertValueToType(String.class, o1.getId());
			String o2_id = conversionHelper.convertValueToType(String.class, o2.getId());
			if (o1_id != null && o2_id != null) {
				return o1_id.compareTo(o2_id);
			}
			if (o1_id == null && o2_id == null) {
				int o1Index = objRefToStateMap.get(o1).index;
				int o2Index = objRefToStateMap.get(o2).index;
				if (o1Index == o2Index) {
					return 0;
				}
				else if (o1Index < o2Index) {
					return -1;
				}
				return 1;
			}
			if (o1_id == null) {
				return 1;
			}
			return -1;
		}
	};

	@Override
	public ICache getStateCache() {
		return stateCache;
	}

	protected HashMap<String, Integer> getOrCreateRelationMemberNameToIndexMap(Class<?> entityType,
			IMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap) {
		HashMap<String, Integer> memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
		if (memberNameToIndexMap != null) {
			return memberNameToIndexMap;
		}
		writeLock.lock();
		try {
			memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
			if (memberNameToIndexMap != null) {
				// concurrent thread might have been faster
				return memberNameToIndexMap;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			RelationMember[] relationMembers = metaData.getRelationMembers();
			memberNameToIndexMap = HashMap.create(relationMembers.length);
			for (int a = relationMembers.length; a-- > 0;) {
				memberNameToIndexMap.put(relationMembers[a].getName(), Integer.valueOf(a));
			}
			typeToMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
			return memberNameToIndexMap;
		}
		finally {
			writeLock.unlock();
		}
	}

	protected HashMap<String, Integer> getOrCreatePrimitiveMemberNameToIndexMap(Class<?> entityType,
			IMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap) {
		HashMap<String, Integer> memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
		if (memberNameToIndexMap != null) {
			return memberNameToIndexMap;
		}
		writeLock.lock();
		try {
			memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
			if (memberNameToIndexMap != null) {
				// concurrent thread might have been faster
				return memberNameToIndexMap;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
			memberNameToIndexMap = HashMap.create(primitiveMembers.length);
			for (int a = primitiveMembers.length; a-- > 0;) {
				memberNameToIndexMap.put(primitiveMembers[a].getName(), Integer.valueOf(a));
			}
			typeToMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
			return memberNameToIndexMap;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public CreateOrUpdateContainerBuild newCreateContainer(Class<?> entityType) {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		entityType = metaData.getEntityType();
		return new CreateOrUpdateContainerBuild(metaData, true,
				getOrCreateRelationMemberNameToIndexMap(entityType, typeToMemberNameToIndexMap),
				getOrCreatePrimitiveMemberNameToIndexMap(entityType, typeToPrimitiveMemberNameToIndexMap),
				cudResultHelper);
	}

	@Override
	public CreateOrUpdateContainerBuild newUpdateContainer(Class<?> entityType) {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		entityType = metaData.getEntityType();
		return new CreateOrUpdateContainerBuild(metaData, false,
				getOrCreateRelationMemberNameToIndexMap(entityType, typeToMemberNameToIndexMap),
				getOrCreatePrimitiveMemberNameToIndexMap(entityType, typeToPrimitiveMemberNameToIndexMap),
				cudResultHelper);
	}
}
