using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.CompositeId
{
    public interface ICompositeIdFactory
    {
        ITypeInfoItem CreateCompositeIdMember(IEntityMetaData metaData, ITypeInfoItem[] idMembers);

        ITypeInfoItem CreateCompositeIdMember(Type entityType, ITypeInfoItem[] idMembers);

        Object CreateCompositeId(IEntityMetaData metaData, ITypeInfoItem compositeIdMember, params Object[] ids);

        Object CreateIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives);

        Object CreateIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue);
    }
}