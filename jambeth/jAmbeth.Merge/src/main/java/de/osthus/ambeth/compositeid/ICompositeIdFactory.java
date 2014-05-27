package de.osthus.ambeth.compositeid;

import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public interface ICompositeIdFactory
{
	ITypeInfoItem createCompositeIdMember(IEntityMetaData metaData, ITypeInfoItem[] idMembers);

	ITypeInfoItem createCompositeIdMember(Class<?> entityType, ITypeInfoItem[] idMembers);

	Object createCompositeId(IEntityMetaData metaData, ITypeInfoItem compositeIdMember, Object... ids);

	Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives);

	Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue);
}
