package com.koch.ambeth.merge.compositeid;

import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.PrimitiveMember;

public interface ICompositeIdFactory
{
	PrimitiveMember createCompositeIdMember(IEntityMetaData metaData, PrimitiveMember[] idMembers);

	PrimitiveMember createCompositeIdMember(Class<?> entityType, PrimitiveMember[] idMembers);

	Object createCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember, Object... ids);

	Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives);

	Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue);

	Object createIdFromEntity(IEntityMetaData metaData, int idIndex, Object entity);
}
