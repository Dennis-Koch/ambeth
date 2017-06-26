package com.koch.ambeth.merge.bytecode.compositeid;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class CompositeIdFactory implements ICompositeIdFactory, IInitializingBean {
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Override
	public void afterPropertiesSet() throws Throwable {
		if (bytecodeEnhancer == null) {
			log.debug("No bytecodeEnhancer specified: Composite ID feature deactivated");
		}
	}

	@Override
	public PrimitiveMember createCompositeIdMember(IEntityMetaData metaData,
			PrimitiveMember[] idMembers) {
		return createCompositeIdMember(metaData.getEntityType(), idMembers);
	}

	@Override
	public PrimitiveMember createCompositeIdMember(Class<?> entityType, PrimitiveMember[] idMembers) {
		if (bytecodeEnhancer == null) {
			throw new UnsupportedOperationException("No bytecodeEnhancer specified");
		}
		StringBuilder nameSB = new StringBuilder();
		// order does matter here
		for (int a = 0, size = idMembers.length; a < size; a++) {
			String name = idMembers[a].getName();
			if (a > 0) {
				nameSB.append('&');
			}
			nameSB.append(name);
		}
		Class<?> compositeIdType =
				bytecodeEnhancer.getEnhancedType(Object.class, new CompositeIdEnhancementHint(idMembers));
		try {
			return new CompositeIdMember(entityType, compositeIdType, nameSB.toString(), idMembers,
					memberTypeProvider);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object createCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember,
			Object... ids) {
		IConversionHelper conversionHelper = this.conversionHelper;
		CompositeIdMember cIdTypeInfoItem = (CompositeIdMember) compositeIdMember;
		Member[] members = cIdTypeInfoItem.getMembers();
		for (int a = ids.length; a-- > 0;) {
			Object id = ids[a];
			Object convertedId = conversionHelper.convertValueToType(members[a].getRealType(), id);
			if (convertedId != id) {
				ids[a] = convertedId;
			}
		}
		try {
			return cIdTypeInfoItem.getRealTypeConstructorAccess().newInstance(ids);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives) {
		int[][] alternateIdMemberIndicesInPrimitives =
				metaData.getAlternateIdMemberIndicesInPrimitives();
		int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

		if (compositeIndex.length == 1) {
			return primitives[compositeIndex[0]];
		}
		PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
		Object[] ids = new Object[compositeIndex.length];
		for (int a = compositeIndex.length; a-- > 0;) {
			ids[a] = primitives[compositeIndex[a]];
		}
		return createCompositeId(metaData, compositeIdMember, ids);
	}

	@Override
	public Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex,
			AbstractCacheValue cacheValue) {
		int[][] alternateIdMemberIndicesInPrimitives =
				metaData.getAlternateIdMemberIndicesInPrimitives();
		int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

		if (compositeIndex.length == 1) {
			return cacheValue.getPrimitive(compositeIndex[0]);
		}
		PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
		Object[] ids = new Object[compositeIndex.length];
		for (int a = compositeIndex.length; a-- > 0;) {
			ids[a] = cacheValue.getPrimitive(compositeIndex[a]);
		}
		return createCompositeId(metaData, compositeIdMember, ids);
	}

	@Override
	public Object createIdFromEntity(IEntityMetaData metaData, int idIndex, Object entity) {
		int[][] alternateIdMemberIndicesInPrimitives =
				metaData.getAlternateIdMemberIndicesInPrimitives();
		int[] compositeIndex = alternateIdMemberIndicesInPrimitives[idIndex];

		if (compositeIndex.length == 1) {
			return metaData.getPrimitiveMembers()[compositeIndex[0]].getValue(entity);
		}
		PrimitiveMember compositeIdMember = metaData.getAlternateIdMembers()[idIndex];
		PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
		Object[] ids = new Object[compositeIndex.length];
		for (int a = compositeIndex.length; a-- > 0;) {
			ids[a] = primitiveMembers[compositeIndex[a]].getValue(entity);
		}
		return createCompositeId(metaData, compositeIdMember, ids);
	}
}
