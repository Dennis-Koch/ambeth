using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.CompositeId
{
    public interface ICompositeIdFactory
    {
        PrimitiveMember CreateCompositeIdMember(IEntityMetaData metaData, PrimitiveMember[] idMembers);

        PrimitiveMember CreateCompositeIdMember(Type entityType, PrimitiveMember[] idMembers);

        Object CreateCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember, params Object[] ids);

        Object CreateIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives);

        Object CreateIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue);
    }
}