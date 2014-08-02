package de.osthus.ambeth.compositeid;

import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.PrimitiveMember;

public interface ICompositeIdFactory
{
	PrimitiveMember createCompositeIdMember(IEntityMetaData metaData, PrimitiveMember[] idMembers);

	PrimitiveMember createCompositeIdMember(Class<?> entityType, PrimitiveMember[] idMembers);

	Object createCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember, Object... ids);

	Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives);

	Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue);
}
